const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { listTransactions, getTransaction, scheduleTransaction } = require('../controllers/transactionController');

const router = express.Router();

router.use(protect);
router.get('/', listTransactions);
router.get('/:id', getTransaction);
router.post('/:id/schedule', scheduleTransaction);

module.exports = router;
