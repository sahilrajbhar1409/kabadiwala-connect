const Transaction = require('../models/Transaction');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const listTransactions = asyncHandler(async (req, res) => {
  const filter = {};
  if (req.user.role === 'collector') filter.collector = req.user._id;
  if (req.user.role === 'recycler') filter.recycler = req.user._id;
  const rows = await Transaction.find(filter)
    .populate('lot')
    .populate('offer')
    .populate('collector', 'name phone')
    .populate('recycler', 'name phone')
    .sort({ createdAt: -1 });
  return success(res, { message: 'Transactions', data: rows });
});

const getTransaction = asyncHandler(async (req, res) => {
  const row = await Transaction.findById(req.params.id)
    .populate('lot')
    .populate('offer')
    .populate('collector', 'name phone')
    .populate('recycler', 'name phone');
  if (!row) throw new ApiError(404, 'Transaction not found');
  return success(res, { message: 'Transaction', data: row });
});

const scheduleTransaction = asyncHandler(async (req, res) => {
  const row = await Transaction.findById(req.params.id).populate('lot');
  if (!row) throw new ApiError(404, 'Transaction not found');
  const involved = [row.collector.toString(), row.recycler.toString()];
  if (req.user.role !== 'admin' && !involved.includes(req.user._id.toString())) {
    throw new ApiError(403, 'Not allowed');
  }
  row.scheduledAt = req.body.scheduledAt;
  row.status = 'SCHEDULED';
  await row.save();
  if (row.lot) {
    row.lot.status = 'SCHEDULED';
    row.lot.scheduledAt = row.scheduledAt;
    await row.lot.save();
  }
  return success(res, { message: 'Pickup scheduled', data: row });
});

module.exports = { listTransactions, getTransaction, scheduleTransaction };
