const PriceHistory = require('../models/PriceHistory');
const Material = require('../models/Material');
const { getLatestPrice, estimateLotValue, getTrends } = require('../services/priceService');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const listPrices = asyncHandler(async (req, res) => {
  const { category, location, weight } = req.query;
  if (category && weight) {
    const estimate = await estimateLotValue({
      category,
      weight: Number(weight),
      location,
    });
    return success(res, { message: 'Estimated lot value (rule-based)', data: estimate });
  }
  if (category) {
    const quote = await getLatestPrice(category, location);
    return success(res, { message: 'Current price', data: quote });
  }
  const latest = await PriceHistory.aggregate([
    { $sort: { recordedAt: -1 } },
    { $group: { _id: '$category', doc: { $first: '$$ROOT' } } },
  ]);
  return success(res, {
    message: 'Latest prices by category',
    data: latest.map((row) => row.doc),
  });
});

const getMaterialPrice = asyncHandler(async (req, res) => {
  const material = await Material.findById(req.params.materialId);
  if (!material) throw new ApiError(404, 'Material not found');
  const quote = await getLatestPrice(material.category, req.query.location);
  return success(res, { message: 'Material price', data: { material, ...quote } });
});

const getTrendsHandler = asyncHandler(async (req, res) => {
  const trends = await getTrends(req.query.category, Number(req.query.limit) || 12);
  return success(res, { message: 'Price trends (historical table, not an ML forecast)', data: trends });
});

const createPrice = asyncHandler(async (req, res) => {
  const row = await PriceHistory.create(req.body);
  return success(res, { status: 201, message: 'Price recorded', data: row });
});

const updatePrice = asyncHandler(async (req, res) => {
  const row = await PriceHistory.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
  if (!row) throw new ApiError(404, 'Price record not found');
  return success(res, { message: 'Price updated', data: row });
});

module.exports = { listPrices, getMaterialPrice, getTrendsHandler, createPrice, updatePrice };
