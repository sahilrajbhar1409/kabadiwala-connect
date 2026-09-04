require('dotenv').config();
const mongoose = require('mongoose');
const connectDB = require('../src/config/db');
const User = require('../src/models/User');
const Material = require('../src/models/Material');
const PriceHistory = require('../src/models/PriceHistory');
const Lot = require('../src/models/Lot');
const RecyclerProfile = require('../src/models/RecyclerProfile');
const Offer = require('../src/models/Offer');
const generateReferenceId = require('../src/utils/generateReferenceId');
const { estimateLotValue } = require('../src/services/priceService');

const DEMO_PASSWORD = 'Demo@12345';

const materialsSeed = [
  { name: 'CRT monitors / TV tubes', category: 'CRT', subCategory: 'Display', description: 'Cathode ray tubes from TVs and monitors', unit: 'kg' },
  { name: 'LCD / LED panels', category: 'LCD_PANEL', subCategory: 'Display', description: 'Flat panel displays', unit: 'kg' },
  { name: 'Printed circuit boards', category: 'PCB', subCategory: 'Electronics', description: 'Motherboards and populated PCBs', unit: 'kg' },
  { name: 'Cables and wires', category: 'CABLE', subCategory: 'Wiring', description: 'Power and data cables', unit: 'kg' },
  { name: 'Batteries', category: 'BATTERY', subCategory: 'Energy', description: 'Li-ion, NiMH and lead-acid cells', unit: 'kg' },
  { name: 'Electric motors', category: 'MOTOR', subCategory: 'Mechanical', description: 'Small appliance and fan motors', unit: 'kg' },
  { name: 'Magnet assemblies', category: 'MAGNET_ASSEMBLY', subCategory: 'Mechanical', description: 'Speaker and HDD magnet assemblies', unit: 'kg' },
  { name: 'Mixed e-waste plastic', category: 'MIXED_PLASTIC', subCategory: 'Polymer', description: 'Housings and mixed plastic fractions', unit: 'kg' },
  { name: 'Other e-waste', category: 'OTHER', subCategory: 'Mixed', description: 'Unsorted residual e-waste', unit: 'kg' },
];

const priceRows = [
  { category: 'CRT', price: 35, minPrice: 25, maxPrice: 45 },
  { category: 'LCD_PANEL', price: 80, minPrice: 60, maxPrice: 110 },
  { category: 'PCB', price: 220, minPrice: 160, maxPrice: 280 },
  { category: 'CABLE', price: 95, minPrice: 70, maxPrice: 120 },
  { category: 'BATTERY', price: 140, minPrice: 90, maxPrice: 190 },
  { category: 'MOTOR', price: 55, minPrice: 40, maxPrice: 75 },
  { category: 'MAGNET_ASSEMBLY', price: 180, minPrice: 130, maxPrice: 230 },
  { category: 'MIXED_PLASTIC', price: 18, minPrice: 12, maxPrice: 28 },
  { category: 'OTHER', price: 15, minPrice: 8, maxPrice: 25 },
];

async function seed() {
  await connectDB();
  console.log('Seeding DEMO data (not government-verified)...');

  await Promise.all([
    User.deleteMany({ isDemo: true }),
    Material.deleteMany({ isDemo: true }),
    PriceHistory.deleteMany({ isDemo: true }),
    Lot.deleteMany({ isDemo: true }),
    RecyclerProfile.deleteMany({ isDemo: true }),
    Offer.deleteMany({ isDemo: true }),
  ]);

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
    generalLocation: 'Delhi',
    isDemo: true,
  });

  const collectorB = await User.create({
    name: 'Suresh Scrap',
    phone: '9876500002',
    email: 'suresh.collector@kabadiwala.demo',
    password: DEMO_PASSWORD,
    role: 'collector',
    preferredLanguage: 'marathi',
    generalLocation: 'Pune',
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
    {
      name: 'Pune E-Waste Hub',
      phone: '9811100003',
      email: 'punehub@kabadiwala.demo',
      password: DEMO_PASSWORD,
      role: 'recycler',
      generalLocation: 'Pune',
      isDemo: true,
    },
  ]);

  await RecyclerProfile.create([
    {
      user: recyclerUsers[0]._id,
      companyName: 'GreenCycle Recyclers Pvt Ltd (DEMO)',
      facilityLocation: { address: 'Okhla Industrial Area', city: 'Delhi', latitude: 28.527, longitude: 77.275 },
      acceptedMaterials: ['PCB', 'CABLE', 'BATTERY', 'LCD_PANEL'],
      authorizationNumber: 'DEMO-CPCB-DEL-001',
      authorizationStatus: 'AUTHORIZED',
      authorizationExpiryDate: new Date('2027-12-31'),
      contactPhone: '9811100001',
      contactEmail: 'greencycle@kabadiwala.demo',
      offeredRates: [
        { category: 'PCB', ratePerKg: 230 },
        { category: 'CABLE', ratePerKg: 100 },
        { category: 'BATTERY', ratePerKg: 150 },
      ],
      pickupAvailable: true,
      serviceAreas: ['Delhi', 'Noida', 'Ghaziabad'],
      isVerified: true,
      isDemo: true,
    },
    {
      user: recyclerUsers[1]._id,
      companyName: 'EcoBoard Recovery (DEMO)',
      facilityLocation: { address: 'Sector 63', city: 'Noida', latitude: 28.627, longitude: 77.375 },
      acceptedMaterials: ['CRT', 'LCD_PANEL', 'MIXED_PLASTIC', 'OTHER'],
      authorizationNumber: 'DEMO-CPCB-NOIDA-002',
      authorizationStatus: 'AUTHORIZED',
      authorizationExpiryDate: new Date('2027-06-30'),
      contactPhone: '9811100002',
      contactEmail: 'ecoboard@kabadiwala.demo',
      offeredRates: [
        { category: 'CRT', ratePerKg: 38 },
        { category: 'LCD_PANEL', ratePerKg: 85 },
      ],
      pickupAvailable: true,
      serviceAreas: ['Noida', 'Delhi'],
      isVerified: true,
      isDemo: true,
    },
    {
      user: recyclerUsers[2]._id,
      companyName: 'Pune E-Waste Hub (DEMO)',
      facilityLocation: { address: 'Bhosari MIDC', city: 'Pune', latitude: 18.629, longitude: 73.813 },
      acceptedMaterials: ['MOTOR', 'MAGNET_ASSEMBLY', 'PCB', 'CABLE'],
      authorizationNumber: 'DEMO-MPCB-PUNE-003',
      authorizationStatus: 'AUTHORIZED',
      authorizationExpiryDate: new Date('2026-12-31'),
      contactPhone: '9811100003',
      contactEmail: 'punehub@kabadiwala.demo',
      offeredRates: [
        { category: 'MOTOR', ratePerKg: 58 },
        { category: 'PCB', ratePerKg: 210 },
      ],
      pickupAvailable: false,
      serviceAreas: ['Pune', 'Pimpri-Chinchwad'],
      isVerified: true,
      isDemo: true,
    },
  ]);

  await Material.insertMany(materialsSeed.map((m) => ({ ...m, isDemo: true })));

  const now = Date.now();
  const history = [];
  for (const row of priceRows) {
    for (let i = 5; i >= 0; i -= 1) {
      history.push({
        ...row,
        location: 'India',
        unit: 'kg',
        source: 'demo-seed-reference-table',
        recordedAt: new Date(now - i * 7 * 24 * 60 * 60 * 1000),
        isDemo: true,
      });
    }
  }
  await PriceHistory.insertMany(history);

  const pcbQuote = await estimateLotValue({ category: 'PCB', weight: 12, location: 'Delhi' });
  const cableQuote = await estimateLotValue({ category: 'CABLE', weight: 20, location: 'Delhi' });
  const motorQuote = await estimateLotValue({ category: 'MOTOR', weight: 40, location: 'Pune' });

  const lots = await Lot.create([
    {
      lotNumber: generateReferenceId('lot'),
      collector: collectorA._id,
      materialCategory: 'PCB',
      materialDescription: 'Motherboard mix from PC dismantling (DEMO)',
      photos: [],
      approximateWeight: 12,
      estimatedValue: pcbQuote.estimatedValue,
      estimatedPriceRange: pcbQuote.estimatedPriceRange,
      location: { address: 'Seelampur', city: 'Delhi', latitude: 28.664, longitude: 77.266 },
      status: 'OFFER_RECEIVED',
      isDemo: true,
    },
    {
      lotNumber: generateReferenceId('lot'),
      collector: collectorA._id,
      materialCategory: 'CABLE',
      materialDescription: 'Mixed copper cables (DEMO)',
      photos: [],
      approximateWeight: 20,
      estimatedValue: cableQuote.estimatedValue,
      estimatedPriceRange: cableQuote.estimatedPriceRange,
      location: { address: 'Mayapuri', city: 'Delhi', latitude: 28.633, longitude: 77.129 },
      status: 'OPEN',
      isDemo: true,
    },
    {
      lotNumber: generateReferenceId('lot'),
      collector: collectorB._id,
      materialCategory: 'MOTOR',
      materialDescription: 'Fan and pump motors (DEMO)',
      photos: [],
      approximateWeight: 40,
      estimatedValue: motorQuote.estimatedValue,
      estimatedPriceRange: motorQuote.estimatedPriceRange,
      location: { address: 'Hadapsar', city: 'Pune', latitude: 18.508, longitude: 73.925 },
      status: 'MATCHED',
      isDemo: true,
    },
  ]);

  await Offer.create({
    offerNumber: generateReferenceId('offer'),
    lot: lots[0]._id,
    recycler: recyclerUsers[0]._id,
    quotedPrice: 2700,
    estimatedPickupDate: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000),
    pickupAvailable: true,
    message: 'We can pick up tomorrow afternoon. DEMO offer.',
    status: 'PENDING',
    isDemo: true,
  });

  console.log('\nDEMO credentials (password for all: Demo@12345)');
  console.log('Admin:     admin@kabadiwala.demo / 9990000001');
  console.log('Collector: ramesh.collector@kabadiwala.demo / 9876500001');
  console.log('Collector: suresh.collector@kabadiwala.demo / 9876500002');
  console.log('Recycler:  greencycle@kabadiwala.demo / 9811100001');
  console.log('Recycler:  ecoboard@kabadiwala.demo / 9811100002');
  console.log('Recycler:  punehub@kabadiwala.demo / 9811100003');
  console.log('\nAuthorization numbers are DEMO placeholders, not real CPCB certificates.\n');

  await mongoose.disconnect();
}

seed().catch((err) => {
  console.error(err);
  process.exit(1);
});
