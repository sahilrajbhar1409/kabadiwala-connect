const User = require('../src/models/User');
const Material = require('../src/models/Material');
const PriceHistory = require('../src/models/PriceHistory');
const Lot = require('../src/models/Lot');
const RecyclerProfile = require('../src/models/RecyclerProfile');
const Offer = require('../src/models/Offer');
const generateReferenceId = require('../src/utils/generateReferenceId');
const { estimateLotValue } = require('../src/services/priceService');

const DEMO_PASSWORD = 'Demo@12345';

const ALL_CATEGORIES = [
  'MOBILE_PCB',
  'COPPER_WIRE',
  'COMPUTER_PCB',
  'BATTERY_LEAD',
  'FAN_MOTOR',
  'COMPRESSOR',
  'LCD_PANEL',
  'CRT_TUBE',
  'PCB',
  'CABLE',
  'BATTERY',
  'MOTOR',
  'MAGNET_ASSEMBLY',
  'MIXED_PLASTIC',
  'OTHER',
];

const materialsSeed = [
  { name: 'Smartphone & Feature Phone PCBs', category: 'MOBILE_PCB', subCategory: 'Electronics', description: 'Populated mobile motherboard IC scrap', unit: 'kg' },
  { name: 'Copper Wire Harness & Cables', category: 'COPPER_WIRE', subCategory: 'Wiring', description: 'Insulated & bare copper wiring', unit: 'kg' },
  { name: 'Computer Motherboards & RAM', category: 'COMPUTER_PCB', subCategory: 'Electronics', description: 'PC & laptop motherboard scrap', unit: 'kg' },
  { name: 'Inverter & Lead-Acid Batteries', category: 'BATTERY_LEAD', subCategory: 'Energy', description: 'Lead-acid inverter & automotive cells', unit: 'kg' },
  { name: 'Fan & Cooler Copper Winding', category: 'FAN_MOTOR', subCategory: 'Mechanical', description: 'Copper winding from ceiling fans & motors', unit: 'kg' },
  { name: 'AC & Fridge Compressor Motors', category: 'COMPRESSOR', subCategory: 'Mechanical', description: 'Sealed compressor motors', unit: 'kg' },
  { name: 'LCD & LED TV Display Panels', category: 'LCD_PANEL', subCategory: 'Display', description: 'Flat panel monitor and TV screens', unit: 'kg' },
  { name: 'CRT Picture Tube TV Glass', category: 'CRT_TUBE', subCategory: 'Display', description: 'Cathode ray tubes from legacy TVs', unit: 'kg' },
];

const priceRows = [
  { category: 'MOBILE_PCB', price: 580, minPrice: 500, maxPrice: 650 },
  { category: 'COPPER_WIRE', price: 460, minPrice: 400, maxPrice: 520 },
  { category: 'COMPUTER_PCB', price: 360, minPrice: 300, maxPrice: 420 },
  { category: 'BATTERY_LEAD', price: 125, minPrice: 100, maxPrice: 145 },
  { category: 'FAN_MOTOR', price: 185, minPrice: 150, maxPrice: 220 },
  { category: 'COMPRESSOR', price: 95, minPrice: 80, maxPrice: 115 },
  { category: 'LCD_PANEL', price: 90, minPrice: 70, maxPrice: 120 },
  { category: 'CRT_TUBE', price: 30, minPrice: 20, maxPrice: 40 },
];

async function seedInMemory() {
  const existingUsers = await User.countDocuments();
  if (existingUsers > 0) return;

  console.log('Seeding in-memory database with authentic 2026 Indian scrap market data...');

  const admin = await User.create({
    name: 'Platform Admin',
    phone: '9990000001',
    email: 'admin@kabadiwala.demo',
    password: DEMO_PASSWORD,
    role: 'admin',
    generalLocation: 'Delhi',
    isDemo: true,
  });

  const collectorA = await User.create({
    name: 'Ramesh Kabadi',
    phone: '9876500001',
    email: 'ramesh.collector@kabadiwala.demo',
    password: DEMO_PASSWORD,
    role: 'collector',
    preferredLanguage: 'hindi',
    generalLocation: 'Seelampur, Delhi',
    isDemo: true,
  });

  const collectorB = await User.create({
    name: 'Suresh Scrap',
    phone: '9876500002',
    email: 'suresh.collector@kabadiwala.demo',
    password: DEMO_PASSWORD,
    role: 'collector',
    preferredLanguage: 'marathi',
    generalLocation: 'Kurla, Mumbai',
    isDemo: true,
  });

  const recyclerUsers = await User.create([
    {
      name: 'GreenCycle Recyclers',
      phone: '9811100001',
      email: 'greencycle@kabadiwala.demo',
      password: DEMO_PASSWORD,
      role: 'recycler',
      generalLocation: 'Delhi',
      isDemo: true,
    },
    {
      name: 'EcoBoard Recovery',
      phone: '9811100002',
      email: 'ecoboard@kabadiwala.demo',
      password: DEMO_PASSWORD,
      role: 'recycler',
      generalLocation: 'Noida',
      isDemo: true,
    },
  ]);

  await RecyclerProfile.create([
    {
      user: recyclerUsers[0]._id,
      companyName: 'GreenCycle Recyclers Pvt Ltd (DEMO)',
      facilityLocation: { address: 'Okhla Industrial Area', city: 'Delhi', latitude: 28.527, longitude: 77.275 },
      acceptedMaterials: ALL_CATEGORIES,
      authorizationNumber: 'DEMO-CPCB-DEL-001',
      authorizationStatus: 'AUTHORIZED',
      authorizationExpiryDate: new Date('2027-12-31'),
      contactPhone: '9811100001',
      contactEmail: 'greencycle@kabadiwala.demo',
      offeredRates: [
        { category: 'MOBILE_PCB', ratePerKg: 600 },
        { category: 'COPPER_WIRE', ratePerKg: 470 },
      ],
      pickupAvailable: true,
      serviceAreas: ['Delhi', 'Noida', 'Mumbai', 'Pune'],
      isVerified: true,
      isDemo: true,
    },
    {
      user: recyclerUsers[1]._id,
      companyName: 'EcoBoard Recovery (DEMO)',
      facilityLocation: { address: 'Sector 63', city: 'Noida', latitude: 28.627, longitude: 77.375 },
      acceptedMaterials: ALL_CATEGORIES,
      authorizationNumber: 'DEMO-CPCB-NOIDA-002',
      authorizationStatus: 'AUTHORIZED',
      authorizationExpiryDate: new Date('2027-06-30'),
      contactPhone: '9811100002',
      contactEmail: 'ecoboard@kabadiwala.demo',
      offeredRates: [
        { category: 'COMPUTER_PCB', ratePerKg: 375 },
      ],
      pickupAvailable: true,
      serviceAreas: ['Noida', 'Delhi'],
      isVerified: true,
      isDemo: true,
    },
  ]);

  await Material.insertMany(materialsSeed.map((m) => ({ ...m, isDemo: true })));

  const now = Date.now();
  const history = [];
  for (const row of priceRows) {
    for (let i = 3; i >= 0; i -= 1) {
      history.push({
        ...row,
        location: 'India',
        unit: 'kg',
        source: 'spot-market-live-benchmarks',
        recordedAt: new Date(now - i * 7 * 24 * 60 * 60 * 1000),
        isDemo: true,
      });
    }
  }
  await PriceHistory.insertMany(history);

  const pcbQuote = await estimateLotValue({ category: 'MOBILE_PCB', weight: 10, location: 'Delhi' });
  const cableQuote = await estimateLotValue({ category: 'COPPER_WIRE', weight: 15, location: 'Delhi' });

  const lots = await Lot.create([
    {
      lotNumber: generateReferenceId('lot'),
      collector: collectorA._id,
      materialCategory: 'MOBILE_PCB',
      materialDescription: 'Clean smartphone IC motherboards collected from Seelampur scrap market',
      photos: ['https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60'],
      approximateWeight: 10,
      estimatedValue: pcbQuote.estimatedValue,
      estimatedPriceRange: pcbQuote.estimatedPriceRange,
      location: { address: 'Seelampur Scrap Market', city: 'Delhi', latitude: 28.664, longitude: 77.266 },
      status: 'OFFER_RECEIVED',
      isDemo: true,
    },
    {
      lotNumber: generateReferenceId('lot'),
      collector: collectorA._id,
      materialCategory: 'COPPER_WIRE',
      materialDescription: 'Heavy electrical copper wire cables bundle',
      photos: ['https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=60'],
      approximateWeight: 15,
      estimatedValue: cableQuote.estimatedValue,
      estimatedPriceRange: cableQuote.estimatedPriceRange,
      location: { address: 'Mayapuri Phase 2', city: 'Delhi', latitude: 28.633, longitude: 77.129 },
      status: 'OPEN',
      isDemo: true,
    },
  ]);

  await Offer.create({
    offerNumber: generateReferenceId('offer'),
    lot: lots[0]._id,
    recycler: recyclerUsers[0]._id,
    quotedPrice: 6000,
    estimatedPickupDate: new Date(Date.now() + 24 * 60 * 60 * 1000),
    pickupAvailable: true,
    message: 'CPCB authorized pickup truck available for pickup tomorrow at 11:00 AM.',
    status: 'PENDING',
    isDemo: true,
  });

  console.log('In-memory database seeded successfully with live spot prices!');
}

module.exports = seedInMemory;
