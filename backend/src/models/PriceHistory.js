const mongoose = require('mongoose');
const { CATEGORIES } = require('./Material');

const priceHistorySchema = new mongoose.Schema(
  {
    material: { type: mongoose.Schema.Types.ObjectId, ref: 'Material' },
    category: { type: String, enum: CATEGORIES, required: true },
    location: { type: String, default: 'India' },
    price: { type: Number, required: true },
    minPrice: { type: Number, required: true },
    maxPrice: { type: Number, required: true },
    unit: { type: String, default: 'kg' },
    source: { type: String, default: 'demo-seed' },
    recordedAt: { type: Date, default: Date.now },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

priceHistorySchema.index({ category: 1, recordedAt: -1 });

module.exports = mongoose.model('PriceHistory', priceHistorySchema);
