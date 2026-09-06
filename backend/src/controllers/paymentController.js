const Payment = require('../models/Payment');
const Transaction = require('../models/Transaction');
const Lot = require('../models/Lot');
const generateReferenceId = require('../utils/generateReferenceId');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');
const { notify } = require('../utils/notify');

const createPayment = asyncHandler(async (req, res) => {
  const transaction = await Transaction.findById(req.body.transactionId || req.body.transaction);
  if (!transaction) throw new ApiError(404, 'Transaction not found');
  const involved = [transaction.collector.toString(), transaction.recycler.toString()];
  if (req.user.role !== 'admin' && !involved.includes(req.user._id.toString())) {
    throw new ApiError(403, 'Not allowed');
  }

  const amount = Number(req.body.amount ?? transaction.finalAmount ?? transaction.agreedAmount);
  const payment = await Payment.create({
    paymentReference: generateReferenceId('payment'),
    transaction: transaction._id,
    collector: transaction.collector,
    recycler: transaction.recycler,
    amount,
    paymentMethod: req.body.paymentMethod || 'CASH',
    paymentStatus: req.body.paymentStatus || 'PAID',
    paidAt: req.body.paymentStatus === 'FAILED' ? null : new Date(),
    referenceNumber: req.body.referenceNumber || '',
  });

  if (payment.paymentStatus === 'PAID') {
    transaction.status = 'COMPLETED';
    transaction.finalAmount = amount;
    await transaction.save();
    await Lot.findByIdAndUpdate(transaction.lot, { status: 'COMPLETED' });
  }

  await Promise.all([
    notify({
      user: transaction.collector,
      title: 'Payment updated',
      message: `₹${amount} ${payment.paymentStatus.toLowerCase()} for transaction ${transaction.transactionReference}.`,
      type: payment.paymentStatus === 'PAID' ? 'PAYMENT_RECEIVED' : 'PAYMENT_UPDATED',
      relatedEntityId: payment._id.toString(),
    }),
    notify({
      user: transaction.recycler,
      title: 'Payment updated',
      message: `₹${amount} ${payment.paymentStatus.toLowerCase()} for transaction ${transaction.transactionReference}.`,
      type: payment.paymentStatus === 'PAID' ? 'PAYMENT_RECEIVED' : 'PAYMENT_UPDATED',
      relatedEntityId: payment._id.toString(),
    }),
  ]);

  return success(res, { status: 201, message: 'Payment recorded', data: payment });
});

const listPayments = asyncHandler(async (req, res) => {
  const filter = {};
  if (req.user.role === 'collector') filter.collector = req.user._id;
  if (req.user.role === 'recycler') filter.recycler = req.user._id;
  const rows = await Payment.find(filter).populate('transaction').sort({ createdAt: -1 });
  return success(res, { message: 'Payments', data: rows });
});

const getPayment = asyncHandler(async (req, res) => {
  const row = await Payment.findById(req.params.id).populate('transaction');
  if (!row) throw new ApiError(404, 'Payment not found');
<<<<<<< HEAD
  if (req.user.role !== 'admin' && ![row.collector.toString(), row.recycler.toString()].includes(req.user._id.toString())) {
=======
  const involved = [row.collector.toString(), row.recycler.toString()];
  if (req.user.role !== 'admin' && !involved.includes(req.user._id.toString())) {
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
    throw new ApiError(403, 'Not allowed');
  }
  return success(res, { message: 'Payment', data: row });
});

module.exports = { createPayment, listPayments, getPayment };
