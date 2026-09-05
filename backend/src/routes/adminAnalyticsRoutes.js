const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const {
  getOverview,
  getMaterialBreakdown,
} = require('../controllers/adminAnalyticsController');

const router = express.Router();

router.use(protect);
router.use(authorize('admin'));

router.get('/overview', getOverview);
router.get('/material-breakdown', getMaterialBreakdown);

module.exports = router;