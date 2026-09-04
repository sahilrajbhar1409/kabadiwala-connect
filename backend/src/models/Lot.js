const mongoose = require('mongoose');
const { CATEGORIES } = require('./Material');

const LOT_STATUSES = [
  'DRAFT',
  'OPEN',
  'MATCHED',
  'OFFER_RECEIVED',
  'OFFER_ACCEPTED',
  'SCHEDULED',
  'HANDED_OVER',
  'COMPLETED',
  'CANCELLED',
];

const locationSchema = new mongoose.Schema(
  {
    address: { type: String, default: '' },
    city: { type: String, default: '' },
    latitude: { type: Number, default: null },
    longitude: { type: Number, default: null },
  },
  { _id: false }
);

const lotSchema = new mongoose.Schema(
  {
    lotNumber: { type: String, required: true, unique: true },
    collector: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    material: { type: mongoose.Schema.Types.ObjectId, ref: 'Material' },
    materialCategory: { type: String, enum: CATEGORIES, required: true },
    materialDescription: { type: String, default: '' },
    photos: [{ type: String }],
    approximateWeight: { type: Number, required: true, min: 0.1 },
    weightUnit: { type: String, default: 'kg' },
    estimatedValue: { type: Number, default: 0 },
    estimatedPriceRange: {
      min: { type: Number, default: 0 },
      max: { type: Number, default: 0 },
    },
    location: { type: locationSchema, default: () => ({}) },
    notes: { type: String, default: '' },
    status: { type: String, enum: LOT_STATUSES, default: 'OPEN' },
    acceptedOffer: { type: mongoose.Schema.Types.ObjectId, ref: 'Offer', default: null },
    scheduledAt: { type: Date, default: null },
    clientGeneratedId: { type: String, default: null, index: true },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

lotSchema.index({ collector: 1, createdAt: -1 });
lotSchema.index({ status: 1, materialCategory: 1 });

module.exports = mongoose.model('Lot', lotSchema);
module.exports.LOT_STATUSES = LOT_STATUSES;
