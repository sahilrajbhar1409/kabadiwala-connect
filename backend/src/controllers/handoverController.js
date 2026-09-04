const Handover = require('../models/Handover');
const Lot = require('../models/Lot');
const Transaction = require('../models/Transaction');
const { createHandover, confirmHandover } = require('../services/handoverService');
const { persistUploadedFiles } = require('../middleware/uploadMiddleware');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const createHandoverHandler = asyncHandler(async (req, res) => {
  const transaction = await Transaction.findById(req.body.transactionId || req.body.transaction);
  if (!transaction) throw new ApiError(404, 'Transaction not found');
  const involved = [transaction.collector.toString(), transaction.recycler.toString()];
  if (req.user.role !== 'admin' && !involved.includes(req.user._id.toString())) {
    throw new ApiError(403, 'Not allowed');
  }

  const lot = await Lot.findById(transaction.lot);
  const photos = await persistUploadedFiles(req.files || []);
  const location = req.body.location
    ? (typeof req.body.location === 'string' ? JSON.parse(req.body.location) : req.body.location)
    : {
        address: req.body.address || '',
        latitude: req.body.latitude ? Number(req.body.latitude) : null,
        longitude: req.body.longitude ? Number(req.body.longitude) : null,
      };

  const handover = await createHandover({
    lot,
    transaction,
    weight: Number(req.body.weight),
    photos,
    location,
    actorRole: req.user.role,
  });

  return success(res, { status: 201, message: 'Handover recorded', data: handover });
});

const getHandover = asyncHandler(async (req, res) => {
  const handover = await Handover.findById(req.params.id).populate('lot').populate('transaction');
  if (!handover) throw new ApiError(404, 'Handover not found');
  return success(res, { message: 'Handover', data: handover });
});

const confirmHandoverHandler = asyncHandler(async (req, res) => {
  const handover = await Handover.findById(req.params.id);
  if (!handover) throw new ApiError(404, 'Handover not found');
  const transaction = await Transaction.findById(handover.transaction);
  const involved = [transaction.collector.toString(), transaction.recycler.toString()];
  if (req.user.role !== 'admin' && !involved.includes(req.user._id.toString())) {
    throw new ApiError(403, 'Not allowed');
  }
  const updated = await confirmHandover(handover, req.user.role);
  return success(res, { message: 'Handover confirmation saved', data: updated });
});

module.exports = { createHandoverHandler, getHandover, confirmHandoverHandler };
