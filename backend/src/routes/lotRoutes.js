const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const { upload } = require('../middleware/uploadMiddleware');
const {
  createLot,
  listLots,
  myLots,
  getLot,
  updateLot,
  deleteLot,
  getMatches,
} = require('../controllers/lotController');

const router = express.Router();

router.use(protect);
// Strictly enforce collector-only lot creation and management
router.post('/', authorize('collector'), upload.array('photos', 6), createLot);
router.get('/', listLots);
router.get('/my-lots', authorize('collector'), myLots);
router.get('/:id/matches', getMatches);
router.get('/:id', getLot);
router.patch('/:id', authorize('collector'), upload.array('photos', 6), updateLot);
router.delete('/:id', authorize('collector'), deleteLot);

module.exports = router;
