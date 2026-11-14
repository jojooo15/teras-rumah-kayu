const mongoose = require('mongoose');
require('dotenv').config();

const connectDB = async () => {
  try {
    // Check if MongoDB URI is provided
    if (!process.env.MONGODB_URI || process.env.MONGODB_URI.trim() === '') {
      console.log('⚠️  No MongoDB URI provided - running in fallback mode');
      return null;
    }

    // Set connection options for better reliability
    const options = {
      serverSelectionTimeoutMS: 5000, // Timeout after 5s instead of 30s
      socketTimeoutMS: 45000, // Close sockets after 45s of inactivity
    };

    const conn = await mongoose.connect(process.env.MONGODB_URI, options);

    console.log(`✅ MongoDB Connected: ${conn.connection.host}`);
    console.log(`📊 Database: ${conn.connection.name}`);

    // Handle connection events
    mongoose.connection.on('error', (err) => {
      console.error('❌ MongoDB connection error:', err);
    });

    mongoose.connection.on('disconnected', () => {
      console.log('⚠️  MongoDB disconnected');
    });

    // Graceful shutdown
    process.on('SIGINT', async () => {
      await mongoose.connection.close();
      console.log('🔒 MongoDB connection closed through app termination');
      process.exit(0);
    });

    return conn;
  } catch (error) {
    console.error('❌ Error connecting to MongoDB:', error.message);

    // More detailed error handling
    if (error.message.includes('ENOTFOUND') || error.message.includes('ECONNREFUSED')) {
      console.log('\n🔧 Connection issue detected:');
      console.log('- MongoDB server is not running or not accessible');
      console.log('- Check if MongoDB service is started');
      console.log('- Verify network connectivity');
    }

    console.log('\n🔧 Troubleshooting suggestions:');
    console.log('1. Install and start MongoDB locally:');
    console.log('   - Download from: https://www.mongodb.com/try/download/community');
    console.log('   - Start service: net start MongoDB (Windows) or brew services start mongodb (Mac)');
    console.log('2. Check your MONGODB_URI in .env file');
    console.log('3. For local MongoDB: mongodb://localhost:27017/teras_rumah_kayu');
    console.log('4. For MongoDB Atlas: mongodb+srv://username:password@cluster.mongodb.net/teras_rumah_kayu');
    console.log('\n⚠️  Server will continue without database - using fallback mode');

    // Don't exit, let the server run without database
    return null;
  }
};

module.exports = connectDB;
