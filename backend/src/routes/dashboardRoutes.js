const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const { collectorDashboard, recyclerDashboard, adminDashboard } = require('../controllers/dashboardController');

const router = express.Router();

router.use(protect);
router.get('/collector', authorize('collector', 'admin'), collectorDashboard);
router.get('/recycler', authorize('recycler', 'admin'), recyclerDashboard);
router.get('/admin', authorize('admin'), adminDashboard);

module.exports = router;
