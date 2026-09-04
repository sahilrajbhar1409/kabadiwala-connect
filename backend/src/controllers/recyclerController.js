const RecyclerProfile = require('../models/RecyclerProfile');
const { matchRecyclersForLot } = require('../services/recyclerMatchingService');
const asyncHandler = require('../utils/asyncHandler');
const { success } = require('../utils/apiResponse');
const { ApiError } = require('../middleware/errorMiddleware');

const listRecyclers = asyncHandler(async (req, res) => {
  const filter = {};
  if (req.query.verified === 'true') filter.isVerified = true;
  if (req.query.category) filter.acceptedMaterials = req.query.category;
  const recyclers = await RecyclerProfile.find(filter).populate('user', 'name phone email generalLocation isActive');
  return success(res, { message: 'Recyclers', data: recyclers });
});

const getRecycler = asyncHandler(async (req, res) => {
  const profile = await RecyclerProfile.findById(req.params.id).populate('user', 'name phone email generalLocation');
  if (!profile) {
    const byUser = await RecyclerProfile.findOne({ user: req.params.id }).populate('user', 'name phone email generalLocation');
    if (!byUser) throw new ApiError(404, 'Recycler not found');
    return success(res, { message: 'Recycler', data: byUser });
  }
  return success(res, { message: 'Recycler', data: profile });
});

const nearbyRecyclers = asyncHandler(async (req, res) => {
  const mockLot = {
    materialCategory: req.query.category || 'PCB',
    approximateWeight: Number(req.query.weight) || 1,
    estimatedValue: 0,
    location: {
      city: req.query.city || '',
      address: req.query.address || '',
      latitude: req.query.lat ? Number(req.query.lat) : null,
      longitude: req.query.lng ? Number(req.query.lng) : null,
    },
  };
  const matches = await matchRecyclersForLot(mockLot);
  return success(res, { message: 'Nearby recyclers', data: matches });
});

const updateMyProfile = asyncHandler(async (req, res) => {
  const profile = await RecyclerProfile.findOne({ user: req.user._id });
  if (!profile) throw new ApiError(404, 'Recycler profile not found');
  const fields = [
    'companyName',
    'facilityLocation',
    'acceptedMaterials',
    'authorizationNumber',
    'contactPhone',
    'contactEmail',
    'offeredRates',
    'pickupAvailable',
    'serviceAreas',
  ];
  fields.forEach((field) => {
    if (req.body[field] !== undefined) profile[field] = req.body[field];
  });
  await profile.save();
  return success(res, { message: 'Recycler profile updated', data: profile });
});

const verifyRecycler = asyncHandler(async (req, res) => {
  const profile = await RecyclerProfile.findById(req.params.id);
  if (!profile) throw new ApiError(404, 'Recycler not found');
  profile.isVerified = true;
  profile.authorizationStatus = 'AUTHORIZED';
  if (req.body.authorizationExpiryDate) {
    profile.authorizationExpiryDate = req.body.authorizationExpiryDate;
  }
  await profile.save();
  return success(res, { message: 'Recycler marked verified (platform verification, not a government certificate)', data: profile });
});

module.exports = { listRecyclers, getRecycler, nearbyRecyclers, updateMyProfile, verifyRecycler };
