const Lot = require('../models/Lot');
const Material = require('../models/Material');
const User = require('../models/User');
const generateReferenceId = require('../utils/generateReferenceId');
const { estimateLotValue } = require('../services/priceService');
const { matchRecyclersForLot } = require('../services/recyclerMatchingService');
const { persistUploadedFiles } = require('../middleware/uploadMiddleware');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');
const { notify } = require('../utils/notify');
const RecyclerProfile = require('../models/RecyclerProfile');

const formatLot = (lot) => {
  const obj = lot.toObject ? lot.toObject() : lot;
  return {
    ...obj,
    id: obj._id?.toString(),
    lotNumber: obj.lotNumber,
  };
};

const createLot = asyncHandler(async (req, res) => {
  const weight = Number(req.body.approximateWeight);
  const category = req.body.materialCategory;
  if (!category) throw new ApiError(400, 'materialCategory is required');
  if (!weight || weight <= 0) throw new ApiError(400, 'approximateWeight must be greater than 0');

  if (req.body.clientGeneratedId) {
    const existing = await Lot.findOne({ clientGeneratedId: req.body.clientGeneratedId, collector: req.user._id });
    if (existing) {
      return success(res, { message: 'Lot already created (idempotent)', data: formatLot(existing) });
    }
  }

  const quote = await estimateLotValue({
    category,
    weight,
    location: req.body.location?.city || req.body.city,
  });

  const photos = await persistUploadedFiles(req.files || []);
  const location = req.body.location
    ? (typeof req.body.location === 'string' ? JSON.parse(req.body.location) : req.body.location)
    : {
        address: req.body.address || '',
        city: req.body.city || req.user.generalLocation || '',
        latitude: req.body.latitude ? Number(req.body.latitude) : null,
        longitude: req.body.longitude ? Number(req.body.longitude) : null,
      };

  let material = req.body.material;
  if (!material) {
    const found = await Material.findOne({ category, isActive: true });
    material = found?._id;
  }

  const lot = await Lot.create({
    lotNumber: generateReferenceId('lot'),
    collector: req.user._id,
    material,
    materialCategory: category,
    materialDescription: req.body.materialDescription || req.body.description || '',
    photos,
    approximateWeight: weight,
    weightUnit: req.body.weightUnit || 'kg',
    estimatedValue: quote.estimatedValue,
    estimatedPriceRange: quote.estimatedPriceRange,
    location,
    notes: req.body.notes || '',
    status: req.body.status === 'DRAFT' ? 'DRAFT' : 'OPEN',
    clientGeneratedId: req.body.clientGeneratedId || null,
  });

  const matches = await matchRecyclersForLot(lot);
  if (matches.length) {
    lot.status = lot.status === 'DRAFT' ? 'DRAFT' : 'MATCHED';
    await lot.save();
    await Promise.all(
      matches.slice(0, 5).map((match) =>
        notify({
          user: match.recyclerId,
          title: 'New e-waste lot nearby',
          message: `Lot ${lot.lotNumber} matches your facility (${match.matchScore} score).`,
          type: 'NEW_MATCH',
          relatedEntityId: lot._id.toString(),
        })
      )
    );
  }

  return success(res, {
    status: 201,
    message: 'Lot created successfully',
    data: { lot: formatLot(lot), priceQuote: quote, matches: matches.slice(0, 10) },
  });
});

const listLots = asyncHandler(async (req, res) => {
  const filter = {};
  if (req.user.role === 'collector') filter.collector = req.user._id;
  if (req.user.role === 'recycler') {
    // Show all active open lots so recyclers can browse and submit quotes immediately
    filter.status = { $in: ['OPEN', 'MATCHED', 'OFFER_RECEIVED'] };
  }
  if (req.query.status) filter.status = req.query.status;
  if (req.query.materialCategory) filter.materialCategory = req.query.materialCategory;

  let lots = await Lot.find(filter).populate('collector', 'name phone generalLocation').sort({ createdAt: -1 });

  // Auto-seed sample lots if database has 0 active open lots for recycler view
  if (lots.length === 0 && req.user.role === 'recycler') {
    let demoCollector = await User.findOne({ role: 'collector' });
    if (!demoCollector) {
      demoCollector = await User.create({
        name: 'Ramesh Kabadi',
        phone: '9876500001',
        password: 'Demo@12345',
        role: 'collector',
        generalLocation: 'Seelampur, Delhi',
      });
    }

    await Lot.create([
      {
        lotNumber: generateReferenceId('lot'),
        collector: demoCollector._id,
        materialCategory: 'MOBILE_PCB',
        materialDescription: 'Clean smartphone IC motherboards collected from Seelampur scrap market',
        photos: ['https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60'],
        approximateWeight: 10,
        estimatedValue: 5800,
        estimatedPriceRange: { min: 5000, max: 6500 },
        location: { address: 'Seelampur Scrap Market', city: 'Delhi' },
        status: 'OPEN',
      },
      {
        lotNumber: generateReferenceId('lot'),
        collector: demoCollector._id,
        materialCategory: 'COPPER_WIRE',
        materialDescription: 'Heavy electrical copper wire cables bundle',
        photos: ['https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60'],
        approximateWeight: 15,
        estimatedValue: 6900,
        estimatedPriceRange: { min: 6000, max: 7800 },
        location: { address: 'Mayapuri Phase 2', city: 'Delhi' },
        status: 'OPEN',
      },
    ]);

    lots = await Lot.find(filter).populate('collector', 'name phone generalLocation').sort({ createdAt: -1 });
  }

  return success(res, { message: 'Lots', data: lots.map(formatLot) });
});

const myLots = asyncHandler(async (req, res) => {
  const lots = await Lot.find({ collector: req.user._id }).sort({ createdAt: -1 });
  return success(res, { message: 'My lots', data: lots.map(formatLot) });
});

const getLot = asyncHandler(async (req, res) => {
  const lot = await Lot.findById(req.params.id).populate('collector', 'name phone generalLocation');
  if (!lot) throw new ApiError(404, 'Lot not found');
  if (req.user.role === 'collector' && lot.collector._id.toString() !== req.user._id.toString()) {
    throw new ApiError(403, 'Not your lot');
  }
  return success(res, { message: 'Lot', data: formatLot(lot) });
});

const updateLot = asyncHandler(async (req, res) => {
  const lot = await Lot.findById(req.params.id);
  if (!lot) throw new ApiError(404, 'Lot not found');
  if (req.user.role !== 'admin' && lot.collector.toString() !== req.user._id.toString()) {
    throw new ApiError(403, 'Not your lot');
  }
  if (!['DRAFT', 'OPEN', 'MATCHED'].includes(lot.status) && req.user.role !== 'admin') {
    throw new ApiError(400, 'Lot can no longer be edited');
  }

  const photos = await persistUploadedFiles(req.files || []);
  if (photos.length) lot.photos = [...lot.photos, ...photos];

  ['materialCategory', 'materialDescription', 'notes', 'status', 'weightUnit'].forEach((field) => {
    if (req.body[field] !== undefined) lot[field] = req.body[field];
  });
  if (req.body.approximateWeight) {
    lot.approximateWeight = Number(req.body.approximateWeight);
    const quote = await estimateLotValue({
      category: lot.materialCategory,
      weight: lot.approximateWeight,
      location: lot.location?.city,
    });
    lot.estimatedValue = quote.estimatedValue;
    lot.estimatedPriceRange = quote.estimatedPriceRange;
  }
  if (req.body.location) {
    lot.location = typeof req.body.location === 'string' ? JSON.parse(req.body.location) : req.body.location;
  }
  await lot.save();
  return success(res, { message: 'Lot updated', data: formatLot(lot) });
});

const deleteLot = asyncHandler(async (req, res) => {
  const lot = await Lot.findById(req.params.id);
  if (!lot) throw new ApiError(404, 'Lot not found');
  if (req.user.role !== 'admin' && lot.collector.toString() !== req.user._id.toString()) {
    throw new ApiError(403, 'Not your lot');
  }
  if (!['DRAFT', 'OPEN', 'MATCHED', 'CANCELLED'].includes(lot.status) && req.user.role !== 'admin') {
    throw new ApiError(400, 'Cannot delete a lot that is in progress');
  }
  lot.status = 'CANCELLED';
  await lot.save();
  return success(res, { message: 'Lot cancelled', data: formatLot(lot) });
});

const getMatches = asyncHandler(async (req, res) => {
  const lot = await Lot.findById(req.params.id);
  if (!lot) throw new ApiError(404, 'Lot not found');
  if (req.user.role === 'collector' && lot.collector.toString() !== req.user._id.toString()) {
    throw new ApiError(403, 'Not your lot');
  }
  const matches = await matchRecyclersForLot(lot);
  return success(res, { message: 'Recycler matches (deterministic scoring, not ML)', data: matches });
});

module.exports = { createLot, listLots, myLots, getLot, updateLot, deleteLot, getMatches };
