const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const {
  getOverview,
  getMaterialBreakdown,
  getSummary,
  getRecycling,
  getMaterials,
  getCollectors,
  getRecyclers,
  getTraceabilityFunnel,
  getEpr
} = require('../controllers/adminAnalyticsController');

const router = express.Router();

router.use(protect);
router.use(authorize('admin'));

// Existing endpoints (compatibility)
router.get('/overview', getOverview);
router.get('/material-breakdown', getMaterialBreakdown);

// New endpoints
router.get('/summary', getSummary);
router.get('/recycling', getRecycling);
router.get('/materials', getMaterials);
router.get('/collectors', getCollectors);
router.get('/recyclers', getRecyclers);
router.get('/traceability/funnel', getTraceabilityFunnel);
router.get('/epr', getEpr);

module.exports = router;