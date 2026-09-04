const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const { upload } = require('../middleware/uploadMiddleware');
const { getProfile, updateProfile, listUsers, setUserActive } = require('../controllers/userController');

const router = express.Router();

router.use(protect);
router.get('/me', getProfile);
router.patch('/me', upload.array('photos', 1), updateProfile);
router.get('/', authorize('admin'), listUsers);
router.patch('/:id/active', authorize('admin'), setUserActive);

module.exports = router;
