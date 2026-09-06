const Offer = require('../models/Offer');

const sanitizeForRecycler = async (lot, recycler) => {
  if (!recycler) return lot;

  const acceptedOffer = await Offer.findOne({
    lot: lot._id,
    recycler,
    status: 'ACCEPTED',
  });

  if (acceptedOffer) return lot;

  const sanitized = lot.toObject ? lot.toObject() : { ...lot };
  if (sanitized.location) {
    sanitized.location = {
      address: sanitized.location.address || '',
      city: sanitized.location.city || '',
    };
  }
  return sanitized;
};

module.exports = { sanitizeForRecycler };