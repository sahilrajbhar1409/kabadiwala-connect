const mongoose = require('mongoose');

const handoverSchema = new mongoose.Schema(
  {
    handoverReference: { type: String, required: true, unique: true },
    lot: { type: mongoose.Schema.Types.ObjectId, ref: 'Lot', required: true },
    transaction: { type: mongoose.Schema.Types.ObjectId, ref: 'Transaction', required: true },
    materialPhotos: [{ type: String }],
    weight: { type: Number, required: true, min: 0 },
    timestamp: { type: Date, default: Date.now },
    location: {
      address: { type: String, default: '' },
      latitude: { type: Number, default: null },
      longitude: { type: Number, default: null },
    },
    collectorConfirmation: { type: Boolean, default: false },
    recyclerConfirmation: { type: Boolean, default: false },
    verificationStatus: {
      type: String,
      enum: ['PENDING', 'PARTIAL', 'VERIFIED'],
      default: 'PENDING',
    },
    clientGeneratedId: { type: String, default: null, index: true },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Handover', handoverSchema);
