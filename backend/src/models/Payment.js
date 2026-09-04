const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema(
  {
    paymentReference: { type: String, required: true, unique: true },
    transaction: { type: mongoose.Schema.Types.ObjectId, ref: 'Transaction', required: true },
    collector: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    recycler: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    amount: { type: Number, required: true, min: 0 },
    paymentMethod: {
      type: String,
      enum: ['CASH', 'UPI', 'BANK_TRANSFER', 'OTHER'],
      default: 'CASH',
    },
    paymentStatus: {
      type: String,
      enum: ['PENDING', 'PAID', 'FAILED'],
      default: 'PENDING',
    },
    paidAt: { type: Date, default: null },
    referenceNumber: { type: String, default: '' },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Payment', paymentSchema);
