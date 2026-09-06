const admin = require('firebase-admin');

let initialized = false;

const getFirebaseAdmin = () => {
  if (initialized) return admin;

  const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  if (!serviceAccountJson) return null;

  let serviceAccount;
  try {
    serviceAccount = JSON.parse(serviceAccountJson);
  } catch (_error) {
    throw new Error('FIREBASE_SERVICE_ACCOUNT_JSON must contain valid JSON');
  }

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  initialized = true;
  return admin;
};

module.exports = { getFirebaseAdmin };