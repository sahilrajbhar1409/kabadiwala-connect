const asyncHandler = require('../utils/asyncHandler');
const { persistUploadedFiles } = require('../middleware/uploadMiddleware');
const { success } = require('../utils/apiResponse');

const uploadImages = asyncHandler(async (req, res) => {
  const urls = await persistUploadedFiles(req.files || []);
  return success(res, { status: 201, message: 'Images uploaded', data: { urls } });
});

module.exports = { uploadImages };