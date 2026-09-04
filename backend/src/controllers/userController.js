const User = require('../models/User');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');
const { persistUploadedFiles } = require('../middleware/uploadMiddleware');

const getProfile = asyncHandler(async (req, res) => {
  return success(res, { message: 'Profile', data: req.user.toSafeObject() });
});

const updateProfile = asyncHandler(async (req, res) => {
  const allowed = ['name', 'preferredLanguage', 'generalLocation'];
  allowed.forEach((field) => {
    if (req.body[field] !== undefined) req.user[field] = req.body[field];
  });
  if (req.files?.length) {
    const urls = await persistUploadedFiles(req.files);
    req.user.profileImage = urls[0];
  }
  await req.user.save();
  return success(res, { message: 'Profile updated', data: req.user.toSafeObject() });
});

const listUsers = asyncHandler(async (req, res) => {
  const users = await User.find(req.query.role ? { role: req.query.role } : {}).sort({ createdAt: -1 });
  return success(res, { message: 'Users', data: users.map((u) => u.toSafeObject()) });
});

const setUserActive = asyncHandler(async (req, res) => {
  const user = await User.findById(req.params.id);
  if (!user) throw new ApiError(404, 'User not found');
  user.isActive = req.body.isActive !== false;
  await user.save();
  return success(res, { message: 'User updated', data: user.toSafeObject() });
});

module.exports = { getProfile, updateProfile, listUsers, setUserActive };
