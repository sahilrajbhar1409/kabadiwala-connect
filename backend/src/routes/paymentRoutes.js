const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { createPayment, listPayments, getPayment } = require('../controllers/paymentController');

const router = express.Router();

router.use(protect);
router.post('/', createPayment);
router.get('/', listPayments);
router.get('/:id', getPayment);

module.exports = router;
