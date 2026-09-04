const Offer = require('../models/Offer');
const Lot = require('../models/Lot');
const Transaction = require('../models/Transaction');
const RecyclerProfile = require('../models/RecyclerProfile');
const generateReferenceId = require('../utils/generateReferenceId');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');
const { notify } = require('../utils/notify');

const createOffer = asyncHandler(async (req, res) => {
  const lot = await Lot.findById(req.body.lotId || req.body.lot);
  if (!lot) throw new ApiError(404, 'Lot not found');
  if (!['OPEN', 'MATCHED', 'OFFER_RECEIVED'].includes(lot.status)) {
    throw new ApiError(400, 'Lot is not accepting offers');
  }

  const profile = await RecyclerProfile.findOne({ user: req.user._id });
  if (!profile) throw new ApiError(403, 'Recycler profile required');
  if (!(profile.acceptedMaterials || []).includes(lot.materialCategory)) {
    throw new ApiError(403, 'This lot material is not in your accepted list');
  }

  const existing = await Offer.findOne({ lot: lot._id, recycler: req.user._id });
  if (existing) throw new ApiError(409, 'You already made an offer on this lot');

  const offer = await Offer.create({
    offerNumber: generateReferenceId('offer'),
    lot: lot._id,
    recycler: req.user._id,
    quotedPrice: Number(req.body.quotedPrice),
    estimatedPickupDate: req.body.estimatedPickupDate || null,
    pickupAvailable: req.body.pickupAvailable !== false,
    message: req.body.message || '',
  });

  lot.status = 'OFFER_RECEIVED';
  await lot.save();

  await notify({
    user: lot.collector,
    title: 'New recycler offer',
    message: `${profile.companyName} offered ₹${offer.quotedPrice} for lot ${lot.lotNumber}.`,
    type: 'NEW_OFFER',
    relatedEntityId: offer._id.toString(),
  });

  return success(res, { status: 201, message: 'Offer submitted', data: offer });
});

const listOffers = asyncHandler(async (req, res) => {
  const filter = {};
  if (req.query.lotId) filter.lot = req.query.lotId;
  if (req.user.role === 'recycler') filter.recycler = req.user._id;
  if (req.user.role === 'collector') {
    const lots = await Lot.find({ collector: req.user._id }).select('_id');
    filter.lot = { $in: lots.map((l) => l._id) };
  }
  const offers = await Offer.find(filter)
    .populate('lot')
    .populate('recycler', 'name phone')
    .sort({ createdAt: -1 });
  return success(res, { message: 'Offers', data: offers });
});

const getOffer = asyncHandler(async (req, res) => {
  const offer = await Offer.findById(req.params.id).populate('lot').populate('recycler', 'name phone');
  if (!offer) throw new ApiError(404, 'Offer not found');
  return success(res, { message: 'Offer', data: offer });
});

const updateOffer = asyncHandler(async (req, res) => {
  const offer = await Offer.findById(req.params.id);
  if (!offer) throw new ApiError(404, 'Offer not found');
  if (offer.recycler.toString() !== req.user._id.toString() && req.user.role !== 'admin') {
    throw new ApiError(403, 'Not your offer');
  }
  if (offer.status !== 'PENDING') throw new ApiError(400, 'Only pending offers can be updated');
  ['quotedPrice', 'estimatedPickupDate', 'pickupAvailable', 'message'].forEach((field) => {
    if (req.body[field] !== undefined) offer[field] = req.body[field];
  });
  await offer.save();
  return success(res, { message: 'Offer updated', data: offer });
});

const acceptOffer = asyncHandler(async (req, res) => {
  const offer = await Offer.findById(req.params.id);
  if (!offer) throw new ApiError(404, 'Offer not found');
  const lot = await Lot.findById(offer.lot);
  if (req.user.role !== 'admin' && lot.collector.toString() !== req.user._id.toString()) {
    throw new ApiError(403, 'Only the lot owner can accept an offer');
  }
  if (offer.status !== 'PENDING') throw new ApiError(400, 'Offer is not pending');

  offer.status = 'ACCEPTED';
  await offer.save();
  await Offer.updateMany(
    { lot: lot._id, _id: { $ne: offer._id }, status: 'PENDING' },
    { $set: { status: 'REJECTED' } }
  );

  lot.status = req.body.scheduledAt ? 'SCHEDULED' : 'OFFER_ACCEPTED';
  lot.acceptedOffer = offer._id;
  lot.scheduledAt = req.body.scheduledAt || offer.estimatedPickupDate || null;
  await lot.save();

  const transaction = await Transaction.create({
    transactionReference: generateReferenceId('txn'),
    lot: lot._id,
    offer: offer._id,
    collector: lot.collector,
    recycler: offer.recycler,
    agreedAmount: offer.quotedPrice,
    finalAmount: offer.quotedPrice,
    status: lot.scheduledAt ? 'SCHEDULED' : 'CREATED',
    scheduledAt: lot.scheduledAt,
  });

  await notify({
    user: offer.recycler,
    title: 'Offer accepted',
    message: `Your offer on ${lot.lotNumber} was accepted.`,
    type: 'OFFER_ACCEPTED',
    relatedEntityId: offer._id.toString(),
  });
  if (lot.scheduledAt) {
    await notify({
      user: lot.collector,
      title: 'Pickup scheduled',
      message: `Pickup for ${lot.lotNumber} is scheduled.`,
      type: 'PICKUP_SCHEDULED',
      relatedEntityId: lot._id.toString(),
    });
  }

  return success(res, {
    message: 'Offer accepted',
    data: { offer, lot, transaction },
  });
});

const rejectOffer = asyncHandler(async (req, res) => {
  const offer = await Offer.findById(req.params.id);
  if (!offer) throw new ApiError(404, 'Offer not found');
  const lot = await Lot.findById(offer.lot);
  const isOwner = lot.collector.toString() === req.user._id.toString();
  const isRecycler = offer.recycler.toString() === req.user._id.toString();
  if (!isOwner && !isRecycler && req.user.role !== 'admin') {
    throw new ApiError(403, 'Not allowed');
  }
  if (offer.status !== 'PENDING') throw new ApiError(400, 'Offer is not pending');
  offer.status = 'REJECTED';
  await offer.save();
  await notify({
    user: offer.recycler,
    title: 'Offer closed',
    message: `Offer on ${lot.lotNumber} was rejected.`,
    type: 'OFFER_REJECTED',
    relatedEntityId: offer._id.toString(),
  });
  return success(res, { message: 'Offer rejected', data: offer });
});

module.exports = { createOffer, listOffers, getOffer, updateOffer, acceptOffer, rejectOffer };
