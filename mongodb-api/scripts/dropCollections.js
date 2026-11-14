const mongoose = require('mongoose');
require('dotenv').config();

async function dropCollections() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/teras_rumah_kayu');
    console.log('✅ Connected to MongoDB');

    // Drop collections
    const collections = ['users', 'reviews'];
    
    for (const collectionName of collections) {
      try {
        await mongoose.connection.db.dropCollection(collectionName);
        console.log(`✅ Dropped collection: ${collectionName}`);
      } catch (error) {
        if (error.message.includes('ns not found')) {
          console.log(`⚠️  Collection ${collectionName} does not exist`);
        } else {
          console.error(`❌ Error dropping ${collectionName}:`, error.message);
        }
      }
    }

    console.log('✅ Collections cleanup completed');

  } catch (error) {
    console.error('❌ Error:', error);
  } finally {
    console.log('🔒 Database connection closed');
    await mongoose.connection.close();
  }
}

dropCollections();
