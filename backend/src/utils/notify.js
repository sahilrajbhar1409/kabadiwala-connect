const Notification = require('../models/Notification');

const notify = async ({ user, title, message, type = 'SYSTEM', relatedEntityId = '' }) => {
  if (!user) return null;
  return Notification.create({ user, title, message, type, relatedEntityId });
};

module.exports = { notify };
