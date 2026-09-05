/**
 * API flow test for Analytics endpoints against a running server.
 * Usage: node scripts/test-analytics.js
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
  if (!res.ok) {
     return { status: res.status, ...json };
  }
  return json;
}

function pass(name) {
  results.push({ name, ok: true });
  console.log(`PASS  ${name}`);
}

function fail(name, err) {
  results.push({ name, ok: false, error: err.message || err });
  console.error(`FAIL  ${name}: ${err.message || err}`);
}

async function run() {
  const health = await request('GET', '/health');
  if (!health.success) throw new Error('health failed');
  pass('ANALYTICS: health check');

  const suffix = Date.now().toString().slice(-8);

  // 1. Login as seeded admin (admin registration is forbidden)
  const adminAuth = await request('POST', '/auth/login', {
    body: {
      identifier: 'admin@kabadiwala.demo',
      password: 'Demo@12345',
    },
  });
  if (!adminAuth.success) throw new Error('Failed to login as admin: ' + JSON.stringify(adminAuth));
  const adminToken = adminAuth.data.token;
  pass('ANALYTICS: login seeded admin');

  const collectorReg = await request('POST', '/auth/register', {
    body: {
      name: 'Test Collector Analytics',
      phone: `81000${suffix}`.slice(0, 10),
      email: `collector_analytics_${suffix}@test.demo`,
      password: 'Test@12345',
      role: 'collector',
      generalLocation: 'Delhi',
    },
  });
  const collectorToken = collectorReg.data.token;

  // 2. Test Unauthenticated Access
  const unauthRes = await request('GET', '/admin/analytics/summary');
  if (unauthRes.status !== 401) throw new Error(`Expected 401, got ${unauthRes.status}`);
  pass('ANALYTICS: unauthenticated access rejected');

  // 3. Test Non-Admin Access
  const nonAdminRes = await request('GET', '/admin/analytics/summary', { token: collectorToken });
  if (nonAdminRes.status !== 403) throw new Error(`Expected 403, got ${nonAdminRes.status}`);
  pass('ANALYTICS: non-admin access rejected');

  // 4. Test Valid Admin Access
  const validAdminRes = await request('GET', '/admin/analytics/summary', { token: adminToken });
  if (!validAdminRes.success || !validAdminRes.meta) throw new Error('Valid admin request failed');
  pass('ANALYTICS: valid admin access (summary)');

  // 5. Test Invalid Dates
  const invalidDateRes = await request('GET', '/admin/analytics/summary?from=not-a-date', { token: adminToken });
  if (invalidDateRes.status !== 400 || !invalidDateRes.message.includes('Invalid from date format')) {
    throw new Error('Expected 400 bad request for invalid date');
  }
  pass('ANALYTICS: invalid date returns 400');

  // 6. Test Date Filtering (should complete successfully with from/to)
  const validParamsRes = await request('GET', '/admin/analytics/summary?from=2023-01-01&to=2025-01-01', { token: adminToken });
  if (!validParamsRes.success || validParamsRes.meta.from === null) {
      throw new Error('Date filtering failed to parse dates');
  }
  pass('ANALYTICS: date filtering parameters parse successfully');

  // 7. Test includeDemo behavior
  const demoParamsRes = await request('GET', '/admin/analytics/summary?includeDemo=true', { token: adminToken });
  if (!demoParamsRes.success || demoParamsRes.meta.includeDemo !== true) {
      throw new Error('includeDemo parameter parsing failed');
  }
  pass('ANALYTICS: includeDemo parameter parses successfully');

  // 8. Test all endpoints
  const endpoints = ['/summary', '/recycling', '/materials', '/collectors', '/recyclers', '/traceability/funnel', '/epr'];
  for (const ep of endpoints) {
      const res = await request('GET', `/admin/analytics${ep}`, { token: adminToken });
      if (!res.success) throw new Error(`${ep} failed to respond successfully`);
      if (ep === '/epr' && res.notes?.co2Impact !== 'Estimate not available') {
          throw new Error('/epr endpoint missing co2Impact note');
      }
  }
  pass('ANALYTICS: all 7 analytics endpoints handle success properly');

  const failed = results.filter((r) => !r.ok);
  console.log(`\n${results.filter((r) => r.ok).length}/${results.length} passed`);
  if (failed.length) process.exit(1);
}

run().catch((err) => {
  fail('SUITE', err);
  process.exit(1);
});
