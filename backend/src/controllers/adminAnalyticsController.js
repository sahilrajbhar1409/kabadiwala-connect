const Handover = require('../models/Handover');

function parseDateRange(req) {
  const { from, to } = req.query;

  const toDate = to ? new Date(to) : new Date();
  const fromDate = from ? new Date(from) : new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

  // include full days
  fromDate.setHours(0, 0, 0, 0);
  toDate.setHours(23, 59, 59, 999);

  return { fromDate, toDate };
}

// GET /api/admin/analytics/overview
exports.getOverview = async (req, res, next) => {
  try {
    const { fromDate, toDate } = parseDateRange(req);

    const pipeline = [
      { $match: { createdAt: { $gte: fromDate, $lte: toDate } } },

      // join transaction for collector/recycler + amounts
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

// GET /api/admin/analytics/material-breakdown
exports.getMaterialBreakdown = async (req, res, next) => {
  try {
    const { fromDate, toDate } = parseDateRange(req);

    const pipeline = [
      { $match: { createdAt: { $gte: fromDate, $lte: toDate } } },

      // join lot to get materialCategory
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