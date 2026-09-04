const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const {
  listRecyclers,
  getRecycler,
  nearbyRecyclers,
  updateMyProfile,
  verifyRecycler,
} = require('../controllers/recyclerController');

const router = express.Router();

router.use(protect);
router.get('/', listRecyclers);
router.get('/nearby', nearbyRecyclers);
router.patch('/me', authorize('recycler'), updateMyProfile);
router.patch('/:id/verify', authorize('admin'), verifyRecycler);
router.get('/:id', getRecycler);

module.exports = router;
