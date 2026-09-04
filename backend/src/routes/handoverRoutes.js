const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { upload } = require('../middleware/uploadMiddleware');
const { createHandoverHandler, getHandover, confirmHandoverHandler } = require('../controllers/handoverController');

const router = express.Router();

router.use(protect);
router.post('/', upload.array('photos', 6), createHandoverHandler);
router.get('/:id', getHandover);
router.post('/:id/confirm', confirmHandoverHandler);

module.exports = router;
