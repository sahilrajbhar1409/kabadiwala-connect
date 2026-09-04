const mongoose = require('mongoose');

const connectDB = async () => {
  const uri = process.env.MONGODB_URI || process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/kabadiwala_connect';
  mongoose.set('strictQuery', true);

  try {
    // Attempt standard connection with 3s timeout
    await mongoose.connect(uri, { serverSelectionTimeoutMS: 3000 });
    console.log(`MongoDB connected: ${mongoose.connection.host}`);
  } catch (err) {
    console.warn(`Local MongoDB connection failed (${err.message}). Starting MongoMemoryServer for SIH Demo...`);
    try {
      const { MongoMemoryServer } = require('mongodb-memory-server');
      const mongod = await MongoMemoryServer.create();
      const memoryUri = mongod.getUri();
      await mongoose.connect(memoryUri);
      console.log(`MongoMemoryServer started successfully at ${memoryUri}`);

      // Auto-seed in-memory database for SIH evaluators
      const seedInMemory = require('../../scripts/seedInMemory');
      await seedInMemory();
    } catch (memErr) {
      console.error('Failed to start MongoMemoryServer fallback', memErr);
      throw memErr;
    }
  }
};

module.exports = connectDB;
