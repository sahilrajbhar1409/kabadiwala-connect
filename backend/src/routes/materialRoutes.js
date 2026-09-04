const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { authorize } = require('../middleware/roleMiddleware');
const { listMaterials, createMaterial, updateMaterial } = require('../controllers/materialController');

const router = express.Router();

router.get('/', protect, listMaterials);
router.post('/', protect, authorize('admin'), createMaterial);
router.patch('/:id', protect, authorize('admin'), updateMaterial);

module.exports = router;
