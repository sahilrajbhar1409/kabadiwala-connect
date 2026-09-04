const mongoose = require('mongoose');

const syncIdempotencySchema = new mongoose.Schema(
  {
    clientGeneratedId: { type: String, required: true, unique: true },
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    action: { type: String, required: true },
    result: { type: mongoose.Schema.Types.Mixed },
  },
  { timestamps: true }
);

module.exports = mongoose.model('SyncIdempotency', syncIdempotencySchema);
