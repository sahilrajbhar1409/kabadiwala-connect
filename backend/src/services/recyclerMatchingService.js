const RecyclerProfile = require('../models/RecyclerProfile');

const haversineKm = (a, b) => {
  if (
    a?.latitude == null ||
    a?.longitude == null ||
    b?.latitude == null ||
    b?.longitude == null
  ) {
    return null;
  }
  const toRad = (deg) => (deg * Math.PI) / 180;
  const R = 6371;
  const dLat = toRad(b.latitude - a.latitude);
  const dLon = toRad(b.longitude - a.longitude);
  const lat1 = toRad(a.latitude);
  const lat2 = toRad(b.latitude);
  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
};

const scoreRecycler = (profile, lot) => {
  const reasons = [];
  let score = 0;

  const authorized = profile.authorizationStatus === 'AUTHORIZED' && profile.isVerified;
  if (authorized) {
    score += 30;
    reasons.push('Authorized and verified recycler');
  } else if (profile.authorizationStatus === 'AUTHORIZED') {
    score += 18;
    reasons.push('Authorization on file (pending platform verification)');
  } else {
    reasons.push('Authorization not active');
  }

  const accepts = (profile.acceptedMaterials || []).includes(lot.materialCategory);
  if (accepts) {
    score += 25;
    reasons.push(`Accepts ${lot.materialCategory}`);
  } else {
    reasons.push(`Does not list ${lot.materialCategory}`);
  }

  const lotCity = (lot.location?.city || lot.location?.address || '').toLowerCase();
  const areas = (profile.serviceAreas || []).map((s) => s.toLowerCase());
  const areaMatch = lotCity && areas.some((area) => lotCity.includes(area) || area.includes(lotCity));
  const distanceKm = haversineKm(lot.location, profile.facilityLocation);
  if (areaMatch) {
    score += 20;
    reasons.push('Service area matches lot location');
  } else if (distanceKm != null && distanceKm <= 40) {
    score += 16;
    reasons.push(`Facility within ${distanceKm.toFixed(1)} km`);
  } else if (distanceKm != null && distanceKm <= 120) {
    score += 8;
    reasons.push(`Facility ${distanceKm.toFixed(1)} km away`);
  } else {
    reasons.push('Location match is weak');
  }

  const rate = (profile.offeredRates || []).find((r) => r.category === lot.materialCategory);
  if (rate?.ratePerKg) {
    const expected = lot.estimatedValue && lot.approximateWeight
      ? lot.estimatedValue / lot.approximateWeight
      : 0;
    if (expected > 0 && rate.ratePerKg >= expected * 0.9) {
      score += 15;
      reasons.push(`Offered rate ₹${rate.ratePerKg}/kg is competitive`);
    } else {
      score += 8;
      reasons.push(`Has a listed rate of ₹${rate.ratePerKg}/kg`);
    }
  }

  if (profile.pickupAvailable) {
    score += 10;
    reasons.push('Pickup available');
  } else {
    reasons.push('Direct handover only');
  }

  return {
    score: Math.min(100, score),
    reasons,
    distanceKm,
    offeredRate: rate?.ratePerKg || null,
    pickupAvailable: Boolean(profile.pickupAvailable),
    authorized,
    acceptsMaterial: accepts,
  };
};

const matchRecyclersForLot = async (lot) => {
  const profiles = await RecyclerProfile.find({
    authorizationStatus: { $in: ['AUTHORIZED', 'PENDING'] },
  }).populate('user', 'name phone email isActive generalLocation');

  return profiles
    .filter((p) => p.user && p.user.isActive)
    .map((profile) => {
      const ranking = scoreRecycler(profile, lot);
      return {
        recyclerId: profile.user._id,
        profileId: profile._id,
        companyName: profile.companyName,
        contactPhone: profile.contactPhone || profile.user.phone,
        contactEmail: profile.contactEmail || profile.user.email,
        facilityLocation: profile.facilityLocation,
        acceptedMaterials: profile.acceptedMaterials,
        authorizationNumber: profile.authorizationNumber,
        authorizationStatus: profile.authorizationStatus,
        isVerified: profile.isVerified,
        pickupAvailable: profile.pickupAvailable,
        serviceAreas: profile.serviceAreas,
        matchScore: ranking.score,
        matchReasons: ranking.reasons,
        supportedMaterial: lot.materialCategory,
        offeredRate: ranking.offeredRate,
        distanceKm: ranking.distanceKm,
      };
    })
    .sort((a, b) => b.matchScore - a.matchScore);
};

module.exports = { matchRecyclersForLot, scoreRecycler };
