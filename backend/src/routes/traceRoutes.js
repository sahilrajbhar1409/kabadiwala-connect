const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { getTrace, syncHandler } = require('../controllers/traceController');

const router = express.Router();

router.get('/trace/:referenceId', protect, getTrace);
router.post('/sync', protect, syncHandler);

module.exports = router;
