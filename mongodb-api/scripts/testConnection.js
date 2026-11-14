const mongoose = require('mongoose');
require('dotenv').config();

async function testConnection() {
  try {
    console.log('🔌 Testing MongoDB connection...');
    console.log(`📍 URI: ${process.env.MONGODB_URI}`);
    
    const conn = await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });

    console.log('✅ MongoDB connection successful!');
    console.log(`🏠 Host: ${conn.connection.host}`);
    console.log(`📊 Database: ${conn.connection.name}`);
    console.log(`🔗 Ready State: ${conn.connection.readyState}`);
    
    // Test basic operations
    console.log('\n🧪 Testing basic operations...');
    
    // Create a test collection
    const testCollection = conn.connection.db.collection('test');
    
    // Insert test document
    const testDoc = { message: 'Hello MongoDB!', timestamp: new Date() };
    await testCollection.insertOne(testDoc);
    console.log('✅ Insert operation successful');
    
    // Find test document
    const foundDoc = await testCollection.findOne({ message: 'Hello MongoDB!' });
    console.log('✅ Find operation successful');
    console.log('📄 Found document:', foundDoc);
    
    // Delete test document
    await testCollection.deleteOne({ message: 'Hello MongoDB!' });
    console.log('✅ Delete operation successful');
    
    // List collections
    const collections = await conn.connection.db.listCollections().toArray();
    console.log('\n📂 Available collections:');
    collections.forEach(col => {
      console.log(`  - ${col.name}`);
    });
    
    console.log('\n🎉 All tests passed! MongoDB is ready to use.');
    
  } catch (error) {
    console.error('❌ MongoDB connection failed:', error.message);
    
    console.log('\n🔧 Troubleshooting suggestions:');
    
    if (error.message.includes('ECONNREFUSED')) {
      console.log('1. ❌ MongoDB server is not running');
      console.log('   💡 Start MongoDB with: mongod');
      console.log('   💡 Or install MongoDB: https://www.mongodb.com/try/download/community');
    }
    
    if (error.message.includes('authentication failed')) {
      console.log('2. ❌ Authentication failed');
      console.log('   💡 Check username/password in MONGODB_URI');
    }
    
    if (error.message.includes('network')) {
      console.log('3. ❌ Network connection issue');
      console.log('   💡 Check internet connection for MongoDB Atlas');
      console.log('   💡 Check firewall settings');
    }
    
    console.log('\n📋 Current configuration:');
    console.log(`   MONGODB_URI: ${process.env.MONGODB_URI}`);
    console.log(`   NODE_ENV: ${process.env.NODE_ENV || 'development'}`);
    
    console.log('\n🔗 Quick setup options:');
    console.log('   Local MongoDB: mongodb://localhost:27017/teras_rumah_kayu');
    console.log('   MongoDB Atlas: mongodb+srv://username:password@cluster.mongodb.net/teras_rumah_kayu');
    
  } finally {
    await mongoose.connection.close();
    console.log('\n🔒 Connection closed');
    process.exit(0);
  }
}

// Run the test
if (require.main === module) {
  testConnection();
}

module.exports = testConnection;
