const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const { listPrices, getMaterialPrice, getTrendsHandler, createPrice, updatePrice } = require('../controllers/priceController');

const router = express.Router();

router.get('/', protect, listPrices);
router.get('/trends', protect, getTrendsHandler);
router.get('/material/:materialId', protect, getMaterialPrice);
router.post('/', protect, authorize('admin'), createPrice);
router.patch('/:id', protect, authorize('admin'), updatePrice);

module.exports = router;
