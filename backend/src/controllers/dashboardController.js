const Lot = require('../models/Lot');
const Offer = require('../models/Offer');
const Transaction = require('../models/Transaction');
const Payment = require('../models/Payment');
const User = require('../models/User');
const RecyclerProfile = require('../models/RecyclerProfile');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');

const collectorDashboard = asyncHandler(async (req, res) => {
  const lots = await Lot.find({ collector: req.user._id }).sort({ createdAt: -1 });
  const transactions = await Transaction.find({ collector: req.user._id }).sort({ createdAt: -1 });
  const payments = await Payment.find({ collector: req.user._id, paymentStatus: 'PAID' });
  const pendingPayments = await Payment.countDocuments({ collector: req.user._id, paymentStatus: 'PENDING' });
  const totalEarnings = payments.reduce((sum, p) => sum + p.amount, 0);

  return success(res, {
    message: 'Collector dashboard',
    data: {
      totalEarnings,
      totalLots: lots.length,
      completedTransactions: transactions.filter((t) => t.status === 'COMPLETED').length,
      pendingPayments,
      recentTransactions: transactions.slice(0, 10),
      earningsHistory: payments.map((p) => ({
        id: p._id,
        amount: p.amount,
        method: p.paymentMethod,
        paidAt: p.paidAt,
        reference: p.paymentReference,
      })),
      lots: lots.slice(0, 10),
    },
  });
});

const recyclerDashboard = asyncHandler(async (req, res) => {
  const offers = await Offer.find({ recycler: req.user._id }).populate('lot');
  const transactions = await Transaction.find({ recycler: req.user._id }).sort({ createdAt: -1 });
  const payments = await Payment.find({ recycler: req.user._id, paymentStatus: 'PAID' });
  return success(res, {
    message: 'Recycler dashboard',
    data: {
      activeOffers: offers.filter((o) => o.status === 'PENDING').length,
      acceptedLots: transactions.filter((t) => ['CREATED', 'SCHEDULED', 'IN_PROGRESS', 'HANDED_OVER'].includes(t.status)).length,
      scheduledPickups: transactions.filter((t) => t.status === 'SCHEDULED').length,
      completedTransactions: transactions.filter((t) => t.status === 'COMPLETED').length,
      totalPurchaseAmount: payments.reduce((sum, p) => sum + p.amount, 0),
      offers: offers.slice(0, 15),
      transactions: transactions.slice(0, 15),
    },
  });
});

const adminDashboard = asyncHandler(async (_req, res) => {
  const [users, lots, offers, transactions, payments, recyclers] = await Promise.all([
    User.countDocuments(),
    Lot.countDocuments(),
    Offer.countDocuments(),
    Transaction.countDocuments(),
    Payment.countDocuments({ paymentStatus: 'PAID' }),
    RecyclerProfile.countDocuments({ isVerified: true }),
  ]);
  const collectors = await User.countDocuments({ role: 'collector' });
  const recyclerUsers = await User.countDocuments({ role: 'recycler' });
  return success(res, {
    message: 'Admin dashboard',
    data: {
      users,
      collectors,
      recyclers: recyclerUsers,
      verifiedRecyclerProfiles: recyclers,
      lots,
      offers,
      transactions,
      paidPayments: payments,
    },
  });
});

module.exports = { collectorDashboard, recyclerDashboard, adminDashboard };
