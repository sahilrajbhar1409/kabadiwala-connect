const Material = require('../models/Material');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const listMaterials = asyncHandler(async (_req, res) => {
  const materials = await Material.find({ isActive: true }).sort({ category: 1, name: 1 });
  return success(res, { message: 'Materials', data: materials });
});

const createMaterial = asyncHandler(async (req, res) => {
  const material = await Material.create(req.body);
  return success(res, { status: 201, message: 'Material created', data: material });
});

const updateMaterial = asyncHandler(async (req, res) => {
  const material = await Material.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
  if (!material) throw new ApiError(404, 'Material not found');
  return success(res, { message: 'Material updated', data: material });
});

module.exports = { listMaterials, createMaterial, updateMaterial };
