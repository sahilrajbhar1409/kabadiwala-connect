const PriceHistory = require('../models/PriceHistory');

const DEFAULTS = {
  MOBILE_PCB: { price: 450, min: 380, max: 520 },
  COPPER_WIRE: { price: 420, min: 360, max: 480 },
  COMPUTER_PCB: { price: 320, min: 260, max: 380 },
  BATTERY_LEAD: { price: 110, min: 90, max: 130 },
  FAN_MOTOR: { price: 160, min: 130, max: 190 },
  COMPRESSOR: { price: 85, min: 70, max: 105 },
  LCD_PANEL: { price: 80, min: 60, max: 110 },
  CRT_TUBE: { price: 25, min: 18, max: 35 },
  CRT: { price: 35, min: 25, max: 45 },
  PCB: { price: 220, min: 160, max: 280 },
  CABLE: { price: 95, min: 70, max: 120 },
  BATTERY: { price: 140, min: 90, max: 190 },
  MOTOR: { price: 55, min: 40, max: 75 },
  MAGNET_ASSEMBLY: { price: 180, min: 130, max: 230 },
  MIXED_PLASTIC: { price: 18, min: 12, max: 28 },
  OTHER: { price: 15, min: 8, max: 25 },
};

const getLatestPrice = async (category, location) => {
  const filter = { category };
  if (location) filter.location = location;
  const latest = await PriceHistory.findOne(filter).sort({ recordedAt: -1 });
  if (latest) {
    return {
      category,
      location: latest.location,
      currentPrice: latest.price,
      minPrice: latest.minPrice,
      maxPrice: latest.maxPrice,
      unit: latest.unit,
      source: latest.source,
      recordedAt: latest.recordedAt,
      method: 'reference_table',
    };
  }
  const fallback = DEFAULTS[category] || DEFAULTS.OTHER;
  return {
    category,
    location: location || 'India',
    currentPrice: fallback.price,
    minPrice: fallback.min,
    maxPrice: fallback.max,
    unit: 'kg',
    source: 'mvp-default-table',
    recordedAt: new Date(),
    method: 'rule_based_default',
  };
};

const estimateLotValue = async ({ category, weight, location }) => {
  const quote = await getLatestPrice(category, location);
  const estimatedValue = Number((weight * quote.currentPrice).toFixed(2));
  return {
    ...quote,
    approximateWeight: weight,
    estimatedValue,
    estimatedPriceRange: {
      min: Number((weight * quote.minPrice).toFixed(2)),
      max: Number((weight * quote.maxPrice).toFixed(2)),
    },
  };
};

const getTrends = async (category, limit = 12) => {
  const filter = category ? { category } : {};
  const rows = await PriceHistory.find(filter).sort({ recordedAt: -1 }).limit(limit);
  return rows.reverse().map((row) => ({
    id: row._id,
    category: row.category,
    location: row.location,
    price: row.price,
    minPrice: row.minPrice,
    maxPrice: row.maxPrice,
    unit: row.unit,
    source: row.source,
    recordedAt: row.recordedAt,
  }));
};

module.exports = { getLatestPrice, estimateLotValue, getTrends, DEFAULTS };
