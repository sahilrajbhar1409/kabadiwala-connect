const mongoose = require('mongoose');

const offerSchema = new mongoose.Schema(
  {
    offerNumber: { type: String, required: true, unique: true },
    lot: { type: mongoose.Schema.Types.ObjectId, ref: 'Lot', required: true },
    recycler: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    quotedPrice: { type: Number, required: true, min: 0 },
    estimatedPickupDate: { type: Date, default: null },
    pickupAvailable: { type: Boolean, default: true },
    message: { type: String, default: '' },
    status: {
      type: String,
      enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'],
      default: 'PENDING',
    },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

offerSchema.index({ lot: 1, recycler: 1 }, { unique: true });

module.exports = mongoose.model('Offer', offerSchema);
