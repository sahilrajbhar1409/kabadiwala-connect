const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { listNotifications, markRead } = require('../controllers/notificationController');

const router = express.Router();

router.use(protect);
router.get('/', listNotifications);
router.patch('/:id/read', markRead);

module.exports = router;
