const mongoose = require('mongoose');
const { CATEGORIES } = require('./Material');

const recyclerProfileSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, unique: true },
    companyName: { type: String, required: true, trim: true },
    facilityLocation: {
      address: { type: String, default: '' },
      city: { type: String, default: '' },
      latitude: { type: Number, default: null },
      longitude: { type: Number, default: null },
    },
    acceptedMaterials: [{ type: String, enum: CATEGORIES }],
    authorizationNumber: { type: String, default: '' },
    authorizationStatus: {
      type: String,
      enum: ['PENDING', 'AUTHORIZED', 'EXPIRED', 'REVOKED'],
      default: 'PENDING',
    },
    authorizationExpiryDate: { type: Date, default: null },
    contactPhone: { type: String, default: '' },
    contactEmail: { type: String, default: '' },
    offeredRates: [
      {
        category: { type: String, enum: CATEGORIES },
        ratePerKg: { type: Number, default: 0 },
      },
    ],
    pickupAvailable: { type: Boolean, default: true },
    serviceAreas: [{ type: String }],
    isVerified: { type: Boolean, default: false },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('RecyclerProfile', recyclerProfileSchema);
