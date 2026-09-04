const crypto = require('crypto');

const prefixes = {
  lot: 'KW-LOT',
  offer: 'KW-OFR',
  txn: 'KW-TXN',
  handover: 'KW-HND',
  payment: 'KW-PAY',
};

const generateReferenceId = (type = 'lot') => {
  const prefix = prefixes[type] || 'KW-REF';
  const date = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  const rand = crypto.randomBytes(3).toString('hex').toUpperCase();
  return `${prefix}-${date}-${rand}`;
};

module.exports = generateReferenceId;
