const Handover = require('../models/Handover');
const Transaction = require('../models/Transaction');
const Payment = require('../models/Payment');
const Lot = require('../models/Lot');
const User = require('../models/User');
const RecyclerProfile = require('../models/RecyclerProfile');
const Offer = require('../models/Offer');
const { ApiError } = require('../middleware/errorMiddleware');

function parseQueryParams(req) {
  const { from, to, includeDemo } = req.query;

  let fromDate = null;
  let toDate = null;

  if (from) {
    fromDate = new Date(from);
    if (isNaN(fromDate.getTime())) throw new ApiError(400, 'Invalid from date format');
  }

  if (to) {
    toDate = new Date(to);
    if (isNaN(toDate.getTime())) throw new ApiError(400, 'Invalid to date format');
  }

  if (fromDate && toDate && fromDate > toDate) {
      throw new ApiError(400, 'from date cannot be after to date');
  }

  const isDemoInc = includeDemo === 'true';

  return { fromDate, toDate, includeDemo: isDemoInc };
}

function buildMatchStage(fromDate, toDate, includeDemo, customDateKey = 'createdAt') {
  const matchObj = {};

  if (!includeDemo) {
    matchObj.isDemo = { $ne: true };
  }

  if (fromDate || toDate) {
    matchObj[customDateKey] = {};
    if (fromDate) matchObj[customDateKey].$gte = fromDate;
    if (toDate) matchObj[customDateKey].$lte = toDate;
  }
  return { $match: matchObj };
}

function formatResponse(data, meta, notes = undefined) {
  const res = {
    success: true,
    meta: {
      from: meta.fromDate ? meta.fromDate.toISOString() : null,
      to: meta.toDate ? meta.toDate.toISOString() : null,
      includeDemo: meta.includeDemo,
    },
    data,
  };
  if (notes) {
    res.notes = notes;
  }
  return res;
}

// ========================
// Existing endpoints APIs keeping the same responses (as required by prompt)
// ========================

// GET /api/admin/analytics/overview (Compatibility layer)
exports.getOverview = async (req, res, next) => {
  try {
    const { from, to } = req.query;
    const toDate = to ? new Date(to) : new Date();
    const fromDate = from ? new Date(from) : new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    fromDate.setHours(0, 0, 0, 0);
    toDate.setHours(23, 59, 59, 999);

    const pipeline = [
      { $match: { createdAt: { $gte: fromDate, $lte: toDate } } },
      {
        $lookup: {
          from: 'transactions',
          localField: 'transaction',
          foreignField: '_id',
          as: 'tx',
        },
      },
      { $unwind: '$tx' },
      {
        $group: {
          _id: null,
          totalWeightKg: { $sum: '$weight' },
          handoversCount: { $sum: 1 },
          collectors: { $addToSet: '$tx.collector' },
          recyclers: { $addToSet: '$tx.recycler' },
          totalAgreedAmount: { $sum: '$tx.agreedAmount' },
        },
      },
      {
        $project: {
          _id: 0,
          totalWeightKg: 1,
          handoversCount: 1,
          uniqueCollectors: { $size: '$collectors' },
          uniqueRecyclers: { $size: '$recyclers' },
          totalAgreedAmount: 1,
        },
      },
    ];

    const result = await Handover.aggregate(pipeline);

    res.json({
      success: true,
      data:
        result[0] || {
          totalWeightKg: 0,
          handoversCount: 0,
          uniqueCollectors: 0,
          uniqueRecyclers: 0,
          totalAgreedAmount: 0,
        },
    });
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/material-breakdown (Compatibility layer)
exports.getMaterialBreakdown = async (req, res, next) => {
  try {
    const { from, to } = req.query;
    const toDate = to ? new Date(to) : new Date();
    const fromDate = from ? new Date(from) : new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    fromDate.setHours(0, 0, 0, 0);
    toDate.setHours(23, 59, 59, 999);

    const pipeline = [
      { $match: { createdAt: { $gte: fromDate, $lte: toDate } } },
      {
        $lookup: {
          from: 'lots',
          localField: 'lot',
          foreignField: '_id',
          as: 'lotDoc',
        },
      },
      { $unwind: '$lotDoc' },
      {
        $group: {
          _id: '$lotDoc.materialCategory',
          totalWeightKg: { $sum: '$weight' },
          handoversCount: { $sum: 1 },
        },
      },
      { $sort: { totalWeightKg: -1 } },
      {
        $project: {
          _id: 0,
          materialCategory: '$_id',
          totalWeightKg: 1,
          handoversCount: 1,
        },
      },
    ];

    const data = await Handover.aggregate(pipeline);
    res.json({ success: true, data });
  } catch (err) {
    next(err);
  }
};

// ========================
// New Endpoints with specific JSON format
// ========================

// GET /api/admin/analytics/summary
exports.getSummary = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);
    const matchStage = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo);
    const handoverMatchStage = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo, 'timestamp');

    // Calculate total verified weight & handovers
    const handoverMatch = { ...handoverMatchStage.$match, verificationStatus: 'VERIFIED' };
    const handoverPipeline = [
      { $match: handoverMatch },
      {
        $group: {
          _id: null,
          totalWeightKg: { $sum: '$weight' },
          verifiedHandovers: { $sum: 1 },
        }
      }
    ];

    // Financial completion: PAID Payment records and COMPLETED/PAID Transactions only
    const txMatch = { ...matchStage.$match, status: { $in: ['PAID', 'COMPLETED'] } };
    const txPipeline = [
      { $match: txMatch },
      {
        $group: {
          _id: null,
          totalCompletedTransactions: { $sum: 1 }
        }
      }
    ];

    // Financial amounts: Payment records with paymentStatus: 'PAID'
    const paymentMatch = { ...matchStage.$match, paymentStatus: 'PAID' };
    const paymentPipeline = [
      { $match: paymentMatch },
      {
        $group: {
          _id: null,
          totalAmount: { $sum: '$amount' }
        }
      }
    ];

    const userMatchStage = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo);
    const usersPipeline = [
      { $match: userMatchStage.$match },
      {
         $group: {
           _id: '$role',
           count: { $sum: 1 }
         }
      }
    ];

    const [handoverRes, txRes, paymentRes, userRes] = await Promise.all([
      Handover.aggregate(handoverPipeline),
      Transaction.aggregate(txPipeline),
      Payment.aggregate(paymentPipeline),
      User.aggregate(usersPipeline)
    ]);

    const hResult = handoverRes[0] || { totalWeightKg: 0, verifiedHandovers: 0 };
    const tResult = txRes[0] || { totalCompletedTransactions: 0 };
    const pResult = paymentRes[0] || { totalAmount: 0 };
    let activeCollectors = 0;
    let activeRecyclers = 0;
    userRes.forEach(u => {
        if (u._id === 'collector') activeCollectors = u.count;
        if (u._id === 'recycler') activeRecyclers = u.count;
    });

    res.json(formatResponse({
      totalWeightKg: hResult.totalWeightKg,
      totalCompletedTransactions: tResult.totalCompletedTransactions,
      totalAmount: pResult.totalAmount,
      activeCollectors,
      activeRecyclers
    }, meta));
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/recycling
exports.getRecycling = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    // Total Volume & Handovers:
    const handoverMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, verificationStatus: 'VERIFIED' };

    // Financial value: Payment PAID
    const paymentMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, paymentStatus: 'PAID' };

    const txMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, status: { $in: ['COMPLETED', 'PAID'] } };

    const [handoverAggr, paymentAggr, txAggr, allTxAggr] = await Promise.all([
      Handover.aggregate([
        { $match: handoverMatch },
        {
          $group: {
            _id: null,
            completedVolumeKg: { $sum: '$weight' }
          }
        }
      ]),
      Payment.aggregate([
        { $match: paymentMatch },
        {
          $group: {
            _id: null,
            totalValue: { $sum: '$amount' }
          }
        }
      ]),
      Transaction.countDocuments(txMatch),
      Transaction.countDocuments(buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match)
    ]);

    const completedVolumeKg = handoverAggr[0]?.completedVolumeKg || 0;
    const totalValue = paymentAggr[0]?.totalValue || 0;
    const completionRate = allTxAggr > 0 ? (txAggr / allTxAggr) * 100 : 0;

    res.json(formatResponse({
      completedVolumeKg,
      totalValue,
      completedTransactions: txAggr,
      totalTransactions: allTxAggr,
      completionRate
    }, meta));
  } catch(err) {
    next(err);
  }
};


// GET /api/admin/analytics/materials
exports.getMaterials = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    // Breakdown by material type using verified Handovers
    const handoverMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, verificationStatus: 'VERIFIED' };

    const pipeline = [
      { $match: handoverMatch },
      {
        $lookup: {
          from: 'lots',
          localField: 'lot',
          foreignField: '_id',
          as: 'lotDoc',
        },
      },
      { $unwind: '$lotDoc' },
      {
        $lookup: {
          from: 'payments',
          localField: 'transaction',
          foreignField: 'transaction',
          as: 'payments'
        }
      },
      {
          $addFields: {
              paidPayments: {
                  $filter: {
                      input: "$payments",
                      as: "payment",
                      cond: { $eq: ["$$payment.paymentStatus", "PAID"] }
                  }
              }
          }
      },
      {
        $group: {
          _id: '$lotDoc.materialCategory',
          totalWeightKg: { $sum: '$weight' },
          handoversCount: { $sum: 1 },
          totalAmount: { $sum: { $sum: "$paidPayments.amount" } }
        },
      },
      {
        $project: {
          _id: 0,
          category: '$_id',
          totalWeightKg: 1,
          handoversCount: 1,
          totalAmount: 1,
          avgRatePerKg: {
            $cond: [
              { $gt: ['$totalWeightKg', 0] },
              { $divide: ['$totalAmount', '$totalWeightKg'] },
              0
            ]
          }
        },
      },
      { $sort: { totalWeightKg: -1 } }
    ];

    const data = await Handover.aggregate(pipeline);
    res.json(formatResponse(data, meta));
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/collectors
exports.getCollectors = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    const handoverMatch = {
        ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match,
        verificationStatus: 'VERIFIED'
    };

    const pipeline = [
      { $match: handoverMatch },
      {
        $lookup: {
          from: 'transactions',
          localField: 'transaction',
          foreignField: '_id',
          as: 'txDoc',
        },
      },
      { $unwind: '$txDoc' },
      {
        $lookup: {
          from: 'users',
          localField: 'txDoc.collector',
          foreignField: '_id',
          as: 'collectorDoc',
        }
      },
      { $unwind: '$collectorDoc' },
      {
        $lookup: {
          from: 'payments',
          localField: 'transaction',
          foreignField: 'transaction',
          as: 'payments'
        }
      },
      {
          $addFields: {
              paidPayments: {
                  $filter: {
                      input: "$payments",
                      as: "payment",
                      cond: { $eq: ["$$payment.paymentStatus", "PAID"] }
                  }
              }
          }
      },
      {
        $group: {
          _id: '$txDoc.collector',
          name: { $first: '$collectorDoc.name' },
          weightCollectedKg: { $sum: '$weight' },
          totalEarnings: { $sum: { $sum: '$paidPayments.amount' } },
        }
      },
      {
        $project: {
          _id: 0,
          id: '$_id',
          name: 1,
          weightCollectedKg: 1,
          totalEarnings: 1
        }
      },
      { $sort: { totalEarnings: -1 } }
    ];

    const collectorsAggr = await Handover.aggregate(pipeline);

    // lots created per collector
    const lotsMatch = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match;
    const lotsAggr = await Lot.aggregate([
        { $match: lotsMatch },
        {
            $group: {
                _id: '$collector',
                lotsCreated: { $sum: 1 }
            }
        }
    ]);

    const lotsMap = {};
    lotsAggr.forEach(l => {
        lotsMap[l._id.toString()] = l.lotsCreated;
    });

    collectorsAggr.forEach(c => {
        c.lotsCreated = lotsMap[c.id.toString()] || 0;
    });

    const activeMatch = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match;
    activeMatch.role = 'collector';
    activeMatch.isActive = true;
    const activeCount = await User.countDocuments(activeMatch);

    res.json(formatResponse({
      activeCollectorsCount: activeCount,
      collectors: collectorsAggr
    }, meta));
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/recyclers
exports.getRecyclers = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    const handoverMatch = {
        ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match,
        verificationStatus: 'VERIFIED'
    };

    const pipeline = [
      { $match: handoverMatch },
      {
        $lookup: {
          from: 'transactions',
          localField: 'transaction',
          foreignField: '_id',
          as: 'txDoc',
        },
      },
      { $unwind: '$txDoc' },
      {
        $lookup: {
          from: 'recyclerprofiles',
          localField: 'txDoc.recycler',
          foreignField: 'user',
          as: 'profileDoc',
        }
      },
      { $unwind: { path: '$profileDoc', preserveNullAndEmptyArrays: true } },
      {
        $lookup: {
          from: 'payments',
          localField: 'transaction',
          foreignField: 'transaction',
          as: 'payments'
        }
      },
      {
          $addFields: {
              paidPayments: {
                  $filter: {
                      input: "$payments",
                      as: "payment",
                      cond: { $eq: ["$$payment.paymentStatus", "PAID"] }
                  }
              }
          }
      },
      {
        $group: {
          _id: '$txDoc.recycler',
          companyName: { $first: '$profileDoc.companyName' },
          cpcbStatus: { $first: '$profileDoc.authorizationStatus' },
          purchasedWeightKg: { $sum: '$weight' },
          totalSpent: { $sum: { $sum: '$paidPayments.amount' } },
        }
      },
      {
        $project: {
          _id: 0,
          id: '$_id',
          companyName: { $ifNull: ['$companyName', 'Unknown'] },
          cpcbStatus: { $ifNull: ['$cpcbStatus', 'UNKNOWN'] },
          purchasedWeightKg: 1,
          totalSpent: 1
        }
      },
      { $sort: { totalSpent: -1 } }
    ];

    const recyclersAggr = await Handover.aggregate(pipeline);

    const activeMatch = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match;
    activeMatch.role = 'recycler';
    activeMatch.isActive = true;
    const activeCount = await User.countDocuments(activeMatch);

    res.json(formatResponse({
      activeRecyclersCount: activeCount,
      recyclers: recyclersAggr
    }, meta));
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/traceability/funnel
exports.getTraceabilityFunnel = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    const matchBase = buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match;

    const [lotsTotal, offersAggr, handoversVerified, paymentsPaid] = await Promise.all([
      Lot.countDocuments(matchBase),
      Offer.aggregate([
          { $match: { ...matchBase, status: 'ACCEPTED' } },
          { $group: { _id: '$lot', count: { $sum: 1 } } }
      ]),
      Handover.countDocuments({ ...matchBase, verificationStatus: 'VERIFIED' }),
      Payment.countDocuments({ ...matchBase, paymentStatus: 'PAID' })
    ]);

    const offersAccepted = offersAggr.length;

    res.json(formatResponse({
      funnel: {
        lotsCreated: lotsTotal,
        offersAccepted,
        handoversVerified,
        paymentsSettled: paymentsPaid
      }
    }, meta));
  } catch (err) {
    next(err);
  }
};

// GET /api/admin/analytics/epr
exports.getEpr = async (req, res, next) => {
  try {
    const meta = parseQueryParams(req);

    const handoverMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, verificationStatus: 'VERIFIED' };

    const pipeline = [
      { $match: handoverMatch },
      {
        $lookup: {
          from: 'lots',
          localField: 'lot',
          foreignField: '_id',
          as: 'lotDoc',
        },
      },
      { $unwind: '$lotDoc' },
      {
        $facet: {
          categories: [
            {
              $group: {
                _id: '$lotDoc.materialCategory',
                weightKg: { $sum: '$weight' },
              }
            },
            {
              $project: {
                _id: 0,
                category: '$_id',
                weightKg: 1,
                co2SavedKg: { $literal: null }
              }
            }
          ],
          total: [
            {
              $group: {
                _id: null,
                totalDivertedKg: { $sum: '$weight' }
              }
            }
          ]
        }
      }
    ];

    const aggrResult = await Handover.aggregate(pipeline);

    const categoryEpr = aggrResult[0]?.categories || [];
    const totalDivertedKg = aggrResult[0]?.total[0]?.totalDivertedKg || 0;

    const recyclerMatch = { ...buildMatchStage(meta.fromDate, meta.toDate, meta.includeDemo).$match, isVerified: true };
    const verifiedRecyclers = await RecyclerProfile.countDocuments(recyclerMatch);

    res.json(formatResponse({
      totalDivertedKg,
      co2SavedKg: null,
      verifiedRecyclers,
      categoryEpr
    }, meta, { co2Impact: "Estimate not available" }));

  } catch (err) {
    next(err);
  }
};