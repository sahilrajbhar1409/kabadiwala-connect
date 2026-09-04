const express = require('express');
const { protect } = require('../middleware/authMiddleware');
const { registerValidators, loginValidators, register, login, me, logout } = require('../controllers/authController');

const router = express.Router();

router.post('/register', registerValidators, register);
router.post('/login', loginValidators, login);
router.get('/me', protect, me);
router.post('/logout', protect, logout);

module.exports = router;
