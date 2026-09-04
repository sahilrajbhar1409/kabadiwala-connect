/**
 * End-to-end API flow test against a running server.
 * Usage: node scripts/test-flows.js
 */
require('dotenv').config();

const BASE = process.env.API_BASE || `http://127.0.0.1:${process.env.PORT || 5000}/api`;

const results = [];

async function request(method, path, { token, body } = {}) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = await res.json().catch(() => ({}));
  if (!res.ok || json.success === false) {
    throw new Error(`${method} ${path} -> ${res.status} ${json.message || JSON.stringify(json)}`);
  }
  return json;
}

function pass(name) {
  results.push({ name, ok: true });
  console.log(`PASS  ${name}`);
}

function fail(name, err) {
  results.push({ name, ok: false, error: err.message });
  console.error(`FAIL  ${name}: ${err.message}`);
}

async function run() {
  const health = await request('GET', '/health');
  if (!health.success) throw new Error('health failed');
  pass('FLOW0 health');

  const suffix = Date.now().toString().slice(-8);
  const collectorReg = await request('POST', '/auth/register', {
    body: {
      name: 'Test Collector',
      phone: `90000${suffix}`.slice(0, 10),
      email: `collector${suffix}@test.demo`,
      password: 'Test@12345',
      role: 'collector',
      generalLocation: 'Delhi',
    },
  });
  const collectorToken = collectorReg.data.token;
  pass('FLOW1 register collector');

  const login = await request('POST', '/auth/login', {
    body: { identifier: collectorReg.data.user.email, password: 'Test@12345' },
  });
  pass('FLOW1 login collector');

  const lot = await request('POST', '/lots', {
    token: login.data.token,
    body: {
      materialCategory: 'PCB',
      materialDescription: 'Test PCB lot',
      approximateWeight: 5,
      city: 'Delhi',
      address: 'Test Nagar',
    },
  });
  const lotId = lot.data.lot._id;
  pass('FLOW1 create lot');

  const myLots = await request('GET', '/lots/my-lots', { token: collectorToken });
  if (!myLots.data.length) throw new Error('my-lots empty');
  pass('FLOW1 my lots');

  const recyclerReg = await request('POST', '/auth/register', {
    body: {
      name: 'Test Recycler',
      phone: `91000${suffix}`.slice(0, 10),
      email: `recycler${suffix}@test.demo`,
      password: 'Test@12345',
      role: 'recycler',
      generalLocation: 'Delhi',
      companyName: 'Test Recycler DEMO',
      acceptedMaterials: ['PCB', 'CABLE'],
      serviceAreas: ['Delhi'],
      authorizationNumber: 'DEMO-TEST-001',
    },
  });
  const recyclerToken = recyclerReg.data.token;
  pass('FLOW2 register recycler');

  await request('POST', '/auth/login', {
    body: { identifier: recyclerReg.data.user.email, password: 'Test@12345' },
  });
  pass('FLOW2 recycler login');

  const lots = await request('GET', '/lots', { token: recyclerToken });
  pass('FLOW2 view eligible lots');

  const offer = await request('POST', '/offers', {
    token: recyclerToken,
    body: { lotId, quotedPrice: 1100, message: 'Test offer', pickupAvailable: true },
  });
  pass('FLOW2 submit offer');

  const accepted = await request('POST', `/offers/${offer.data._id}/accept`, {
    token: collectorToken,
    body: { scheduledAt: new Date(Date.now() + 86400000).toISOString() },
  });
  pass('FLOW3 accept offer');
  const transactionId = accepted.data.transaction._id;

  const handover = await request('POST', '/handovers', {
    token: collectorToken,
    body: { transactionId, weight: 5.2, address: 'Test Nagar' },
  });
  pass('FLOW4 collector handover');

  await request('POST', `/handovers/${handover.data._id}/confirm`, { token: recyclerToken });
  pass('FLOW4 recycler confirm handover');

  const payment = await request('POST', '/payments', {
    token: recyclerToken,
    body: { transactionId, amount: 1100, paymentMethod: 'CASH', paymentStatus: 'PAID' },
  });
  pass('FLOW5 record payment');

  const dash = await request('GET', '/dashboard/collector', { token: collectorToken });
  if (dash.data.totalEarnings < 1100) throw new Error('earnings not updated');
  pass('FLOW5 collector dashboard earnings');

  const trace = await request('GET', `/trace/${lot.data.lot.lotNumber}`, { token: collectorToken });
  const steps = trace.data.timeline.map((t) => t.step);
  if (!steps.includes('LOT') || !steps.includes('OFFER') || !steps.includes('TRANSACTION') || !steps.includes('HANDOVER') || !steps.includes('PAYMENT')) {
    throw new Error(`incomplete timeline: ${steps.join(',')}`);
  }
  pass('FLOW6 traceability timeline');

  const failed = results.filter((r) => !r.ok);
  console.log(`\n${results.filter((r) => r.ok).length}/${results.length} passed`);
  if (failed.length) process.exit(1);
}

run().catch((err) => {
  fail('SUITE', err);
  process.exit(1);
});
