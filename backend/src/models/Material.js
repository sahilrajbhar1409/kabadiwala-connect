const mongoose = require('mongoose');

const CATEGORIES = [
  'MOBILE_PCB',
  'COPPER_WIRE',
  'COMPUTER_PCB',
  'BATTERY_LEAD',
  'FAN_MOTOR',
  'COMPRESSOR',
  'LCD_PANEL',
  'CRT_TUBE',
  'CRT',
  'PCB',
  'CABLE',
  'BATTERY',
  'MOTOR',
  'MAGNET_ASSEMBLY',
  'MIXED_PLASTIC',
  'OTHER',
];

const materialSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    category: { type: String, enum: CATEGORIES, required: true },
    subCategory: { type: String, default: '', trim: true },
    description: { type: String, default: '' },
    unit: { type: String, default: 'kg' },
    isActive: { type: Boolean, default: true },
    isDemo: { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Material', materialSchema);
module.exports.CATEGORIES = CATEGORIES;
