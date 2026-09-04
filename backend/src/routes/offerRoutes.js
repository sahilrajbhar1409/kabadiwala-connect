const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const {
  createOffer,
  listOffers,
  getOffer,
  updateOffer,
  acceptOffer,
  rejectOffer,
} = require('../controllers/offerController');

const router = express.Router();

router.use(protect);
router.post('/', authorize('recycler', 'admin'), createOffer);
router.get('/', listOffers);
router.get('/:id', getOffer);
router.patch('/:id', authorize('recycler', 'admin'), updateOffer);
router.post('/:id/accept', authorize('collector', 'admin'), acceptOffer);
router.post('/:id/reject', authorize('collector', 'recycler', 'admin'), rejectOffer);

module.exports = router;
