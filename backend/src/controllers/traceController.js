const Lot = require('../models/Lot');
const Offer = require('../models/Offer');
const Transaction = require('../models/Transaction');
const Handover = require('../models/Handover');
const Payment = require('../models/Payment');
const SyncIdempotency = require('../models/SyncIdempotency');
const generateReferenceId = require('../utils/generateReferenceId');
const { estimateLotValue } = require('../services/priceService');
const { createHandover } = require('../services/handoverService');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const findByAnyReference = async (referenceId) => {
  const lot = await Lot.findOne({ lotNumber: referenceId });
  if (lot) return { type: 'lot', lot };
  const offer = await Offer.findOne({ offerNumber: referenceId });
  if (offer) return { type: 'offer', offer, lot: await Lot.findById(offer.lot) };
  const transaction = await Transaction.findOne({ transactionReference: referenceId });
  if (transaction) return { type: 'transaction', transaction, lot: await Lot.findById(transaction.lot) };
  const handover = await Handover.findOne({ handoverReference: referenceId });
  if (handover) return { type: 'handover', handover, lot: await Lot.findById(handover.lot) };
  const payment = await Payment.findOne({ paymentReference: referenceId });
  if (payment) {
    const txn = await Transaction.findById(payment.transaction);
    return { type: 'payment', payment, transaction: txn, lot: txn ? await Lot.findById(txn.lot) : null };
  }
  return null;
};

const getTrace = asyncHandler(async (req, res) => {
  const found = await findByAnyReference(req.params.referenceId);
  if (!found?.lot) throw new ApiError(404, 'Reference not found');

  const lot = found.lot;
  const offers = await Offer.find({ lot: lot._id }).populate('recycler', 'name phone');
  const transaction = await Transaction.findOne({ lot: lot._id });
  const handover = transaction ? await Handover.findOne({ transaction: transaction._id }) : null;
  const payment = transaction ? await Payment.findOne({ transaction: transaction._id }) : null;

  const timeline = [
    {
      step: 'LOT',
      at: lot.createdAt,
      reference: lot.lotNumber,
      status: lot.status,
      detail: `${lot.materialCategory} · ${lot.approximateWeight}${lot.weightUnit}`,
    },
  ];
  offers.forEach((offer) => {
    timeline.push({
      step: 'OFFER',
      at: offer.createdAt,
      reference: offer.offerNumber,
      status: offer.status,
      detail: `₹${offer.quotedPrice} by ${offer.recycler?.name || 'recycler'}`,
    });
  });
  if (transaction) {
    timeline.push({
      step: 'TRANSACTION',
      at: transaction.createdAt,
      reference: transaction.transactionReference,
      status: transaction.status,
      detail: `Agreed ₹${transaction.agreedAmount}`,
    });
  }
  if (handover) {
    timeline.push({
      step: 'HANDOVER',
      at: handover.timestamp || handover.createdAt,
      reference: handover.handoverReference,
      status: handover.verificationStatus,
      detail: `${handover.weight} kg · collector ${handover.collectorConfirmation ? 'yes' : 'no'} / recycler ${handover.recyclerConfirmation ? 'yes' : 'no'}`,
    });
  }
  if (payment) {
    timeline.push({
      step: 'PAYMENT',
      at: payment.paidAt || payment.createdAt,
      reference: payment.paymentReference,
      status: payment.paymentStatus,
      detail: `₹${payment.amount} via ${payment.paymentMethod}`,
    });
  }

  timeline.sort((a, b) => new Date(a.at) - new Date(b.at));

  return success(res, {
    message: 'Traceability chain',
    data: { lot, offers, transaction, handover, payment, timeline },
  });
});

const processSyncItem = async (user, item) => {
  const { clientGeneratedId, action, payload = {} } = item;
  if (!clientGeneratedId || !action) {
    return { clientGeneratedId, success: false, message: 'clientGeneratedId and action are required' };
  }

  const existing = await SyncIdempotency.findOne({ clientGeneratedId });
  if (existing) {
    return { clientGeneratedId, success: true, duplicate: true, data: existing.result };
  }

  let result;
  if (action === 'create_lot') {
    const quote = await estimateLotValue({
      category: payload.materialCategory,
      weight: Number(payload.approximateWeight),
      location: payload.location?.city,
    });
    const lot = await Lot.create({
      lotNumber: generateReferenceId('lot'),
      collector: user._id,
      materialCategory: payload.materialCategory,
      materialDescription: payload.materialDescription || '',
      photos: payload.photos || [],
      approximateWeight: Number(payload.approximateWeight),
      weightUnit: payload.weightUnit || 'kg',
      estimatedValue: quote.estimatedValue,
      estimatedPriceRange: quote.estimatedPriceRange,
      location: payload.location || {},
      notes: payload.notes || '',
      status: payload.status || 'OPEN',
      clientGeneratedId,
    });
    result = lot;
  } else if (action === 'update_lot') {
    const lot = await Lot.findOne({ _id: payload.lotId, collector: user._id });
    if (!lot) throw new ApiError(404, 'Lot not found for sync update');
    Object.assign(lot, payload.updates || {});
    await lot.save();
    result = lot;
  } else if (action === 'create_handover') {
    const transaction = await Transaction.findById(payload.transactionId);
    if (!transaction) throw new ApiError(404, 'Transaction not found');
    const lot = await Lot.findById(transaction.lot);
    result = await createHandover({
      lot,
      transaction,
      weight: Number(payload.weight),
      photos: payload.photos || [],
      location: payload.location || {},
      actorRole: user.role,
    });
  } else {
    throw new ApiError(400, `Unsupported sync action: ${action}`);
  }

  await SyncIdempotency.create({ clientGeneratedId, user: user._id, action, result });
  return { clientGeneratedId, success: true, duplicate: false, data: result };
};

const syncHandler = asyncHandler(async (req, res) => {
  const items = req.body.items || req.body.actions || [];
  const results = [];
  for (const item of items) {
    try {
      results.push(await processSyncItem(req.user, item));
    } catch (err) {
      results.push({
        clientGeneratedId: item.clientGeneratedId,
        success: false,
        message: err.message,
      });
    }
  }
  return success(res, { message: 'Sync processed', data: { results } });
});

module.exports = { getTrace, syncHandler };
