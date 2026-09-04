const Notification = require('../models/Notification');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const listNotifications = asyncHandler(async (req, res) => {
  const rows = await Notification.find({ user: req.user._id }).sort({ createdAt: -1 }).limit(50);
  return success(res, { message: 'Notifications', data: rows });
});

const markRead = asyncHandler(async (req, res) => {
  const row = await Notification.findById(req.params.id);
  if (!row) throw new ApiError(404, 'Notification not found');
  if (row.user.toString() !== req.user._id.toString()) throw new ApiError(403, 'Not allowed');
  row.isRead = true;
  await row.save();
  return success(res, { message: 'Marked as read', data: row });
});

module.exports = { listNotifications, markRead };
