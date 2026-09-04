const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema(
  {
    transactionReference: { type: String, required: true, unique: true },
    lot: { type: mongoose.Schema.Types.ObjectId, ref: 'Lot', required: true },
    offer: { type: mongoose.Schema.Types.ObjectId, ref: 'Offer', required: true },
    collector: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    recycler: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    agreedAmount: { type: Number, required: true },
    finalAmount: { type: Number, default: null },
    status: {
      type: String,
      enum: ['CREATED', 'SCHEDULED', 'IN_PROGRESS', 'HANDED_OVER', 'PAID', 'COMPLETED', 'CANCELLED'],
      default: 'CREATED',
    },
    scheduledAt: { type: Date, default: null },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Transaction', transactionSchema);
