const { body, validationResult } = require('express-validator');
const User = require('../models/User');
const RecyclerProfile = require('../models/RecyclerProfile');
const generateToken = require('../utils/generateToken');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const handleValidation = (req) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    throw new ApiError(
      400,
      'Validation failed',
      errors.array().map((e) => ({ field: e.path, message: e.msg }))
    );
  }
};

const registerValidators = [
  body('name').trim().isLength({ min: 2 }).withMessage('Full name must be at least 2 characters'),
  body('phone').trim().isLength({ min: 10 }).withMessage('Phone number must be at least 10 digits (e.g. 9876500001)'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  body('role').isIn(['collector', 'recycler']).withMessage('Invalid user role'),
  body('email').optional({ checkFalsy: true }).isEmail().withMessage('Invalid email address format'),
];

const loginValidators = [
  body('password').notEmpty().withMessage('Password is required'),
  body('identifier').optional(),
  body('phone').optional(),
  body('email').optional(),
];

const register = asyncHandler(async (req, res) => {
  handleValidation(req);
  const {
    name,
    phone,
    email,
    password,
    role,
    preferredLanguage,
    generalLocation,
    companyName,
    authorizationNumber,
    acceptedMaterials,
    serviceAreas,
    pickupAvailable,
  } = req.body;

  if (role === 'admin') {
    throw new ApiError(403, 'Public admin registration is strictly disabled. Admin accounts are seeded securely.');
  }

  const existingPhone = await User.findOne({ phone });
  if (existingPhone) throw new ApiError(409, 'Phone number is already registered. Please sign in instead.');
  if (email) {
    const existingEmail = await User.findOne({ email: email.toLowerCase() });
    if (existingEmail) throw new ApiError(409, 'Email address is already registered.');
  }

  const user = await User.create({
    name,
    phone,
    email: email || undefined,
    password,
    role,
    preferredLanguage: preferredLanguage || 'english',
    generalLocation: generalLocation || '',
  });

  if (role === 'recycler') {
    await RecyclerProfile.create({
      user: user._id,
      companyName: companyName || `${name} Recycling Facility`,
      facilityLocation: { address: generalLocation || '', city: generalLocation || '' },
      acceptedMaterials: acceptedMaterials || [],
      authorizationNumber: authorizationNumber || '',
      authorizationStatus: 'PENDING',
      contactPhone: phone,
      contactEmail: email || '',
      pickupAvailable: pickupAvailable !== false,
      serviceAreas: serviceAreas || (generalLocation ? [generalLocation] : []),
      isVerified: false,
    });
  }

  const token = generateToken(user._id, user.role);
  return success(res, {
    status: 201,
    message: 'Registered successfully',
    data: { token, user: user.toSafeObject() },
  });
});

const login = asyncHandler(async (req, res) => {
  handleValidation(req);
  const identifier = (req.body.identifier || req.body.email || req.body.phone || '').trim();
  if (!identifier) throw new ApiError(400, 'Phone number or email is required');

  const user = await User.findOne({
    $or: [{ phone: identifier }, { email: identifier.toLowerCase() }],
  }).select('+password');

  if (!user || !(await user.matchPassword(req.body.password))) {
    throw new ApiError(401, 'Invalid phone/email or password');
  }
  if (!user.isActive) throw new ApiError(403, 'Account is inactive');

  const token = generateToken(user._id, user.role);
  return success(res, {
    message: 'Logged in successfully',
    data: { token, user: user.toSafeObject() },
  });
});

const me = asyncHandler(async (req, res) => {
  let recyclerProfile = null;
  if (req.user.role === 'recycler') {
    recyclerProfile = await RecyclerProfile.findOne({ user: req.user._id });
  }
  return success(res, {
    message: 'Current user',
    data: { user: req.user.toSafeObject(), recyclerProfile },
  });
});

const logout = asyncHandler(async (_req, res) => {
  return success(res, { message: 'Logged out.' });
});

module.exports = {
  registerValidators,
  loginValidators,
  register,
  login,
  me,
  logout,
};
