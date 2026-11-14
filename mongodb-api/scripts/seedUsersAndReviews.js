const mongoose = require('mongoose');
require('dotenv').config();

const User = require('../models/User');
const Review = require('../models/Review');
const FoodItem = require('../models/FoodItem');

// Sample users data
const usersData = [
  {
    id: "user_001",
    phoneNumber: "+6281234567890",
    name: "Budi Santoso",
    isVerified: true,
    isActive: true,
    points: 50,
    totalPointsEarned: 80,
    totalPointsRedeemed: 30,
    totalReviews: 3,
    averageRatingGiven: 4.3,
    userLevel: "BRONZE",
    addresses: [
      {
        id: "addr_001",
        label: "Rumah",
        recipientName: "Budi Santoso",
        phoneNumber: "+6281234567890",
        fullAddress: "Jl. Merdeka No. 123, RT 01/RW 02",
        district: "Kemayoran",
        city: "Jakarta Pusat",
        province: "DKI Jakarta",
        postalCode: "10610",
        notes: "Rumah cat hijau, ada pagar putih",
        isDefault: true,
        isActive: true,
        createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
      }
    ],
    defaultAddressId: "addr_001",
    pointHistory: [
      {
        type: "EARNED",
        points: 10,
        description: "Review untuk Paket Sop Iga",
        date: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) // 7 days ago
      },
      {
        type: "EARNED",
        points: 10,
        description: "Review untuk Paket Pecak Lele",
        date: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000) // 5 days ago
      },
      {
        type: "REDEEMED",
        points: 30,
        description: "Penukaran poin untuk reward",
        date: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000) // 3 days ago
      }
    ]
  },
  {
    id: "user_002",
    phoneNumber: "+6281234567891",
    name: "Siti Nurhaliza",
    isVerified: true,
    isActive: true,
    points: 120,
    totalPointsEarned: 120,
    totalPointsRedeemed: 0,
    totalReviews: 5,
    averageRatingGiven: 4.6,
    userLevel: "SILVER",
    addresses: [
      {
        id: "addr_002",
        label: "Kantor",
        recipientName: "Siti Nurhaliza",
        phoneNumber: "+6281234567891",
        fullAddress: "Jl. Sudirman No. 45, Lantai 12",
        district: "Tanah Abang",
        city: "Jakarta Pusat",
        province: "DKI Jakarta",
        postalCode: "10220",
        notes: "Gedung perkantoran, lobby utama",
        isDefault: true,
        isActive: true,
        createdAt: new Date(Date.now() - 25 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 25 * 24 * 60 * 60 * 1000)
      },
      {
        id: "addr_003",
        label: "Rumah",
        recipientName: "Siti Nurhaliza",
        phoneNumber: "+6281234567891",
        fullAddress: "Jl. Kebon Jeruk Raya No. 88",
        district: "Kebon Jeruk",
        city: "Jakarta Barat",
        province: "DKI Jakarta",
        postalCode: "11530",
        notes: "Komplek perumahan, blok A",
        isDefault: false,
        isActive: true,
        createdAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000),
        updatedAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000)
      }
    ],
    defaultAddressId: "addr_002",
    pointHistory: [
      {
        type: "EARNED",
        points: 10,
        description: "Review untuk Paket Geprek",
        date: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000)
      }
    ]
  },
  {
    id: "user_003",
    phoneNumber: "+6281234567892",
    name: "Ahmad Rizki",
    isVerified: true,
    isActive: true,
    points: 30,
    totalPointsEarned: 30,
    totalPointsRedeemed: 0,
    totalReviews: 1,
    averageRatingGiven: 5.0,
    userLevel: "BASIC",
    addresses: [],
    defaultAddressId: null
  }
];

// Sample reviews data
const reviewsData = [
  {
    userId: "user_001",
    foodItemId: 1, // Paket Sop Iga
    rating: 5,
    comment: "Sop iganya enak banget! Dagingnya empuk dan kuahnya gurih. Porsi juga besar, cocok untuk makan siang.",
    userName: "Budi Santoso",
    userPhone: "+6281234567890",
    isVerified: true,
    pointsAwarded: 10,
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
  },
  {
    userId: "user_001", 
    foodItemId: 2, // Paket Pecak Lele
    rating: 4,
    comment: "Lelenya segar dan bumbu pecaknya pas. Cuma agak lama nunggunya, tapi worth it!",
    userName: "Budi Santoso", 
    userPhone: "+6281234567890",
    isVerified: true,
    pointsAwarded: 10,
    createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
  },
  {
    userId: "user_002",
    foodItemId: 3, // Paket Geprek
    rating: 5,
    comment: "Ayam gepreknya juara! Pedasnya pas dan nasinya pulen. Recommended banget!",
    userName: "Siti Nurhaliza",
    userPhone: "+6281234567891", 
    isVerified: true,
    pointsAwarded: 10,
    createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000)
  },
  {
    userId: "user_002",
    foodItemId: 1, // Paket Sop Iga
    rating: 4,
    comment: "Rasanya enak, tapi porsinya agak kecil untuk harga segitu. Overall masih oke lah.",
    userName: "Siti Nurhaliza",
    userPhone: "+6281234567891",
    isVerified: true, 
    pointsAwarded: 10,
    createdAt: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000)
  },
  {
    userId: "user_003",
    foodItemId: 4, // Paket Bawal Bakar
    rating: 5,
    comment: "Ikan bawalnya fresh banget! Bumbu bakarnya meresap sempurna. Pasti order lagi!",
    userName: "Ahmad Rizki",
    userPhone: "+6281234567892",
    isVerified: true,
    pointsAwarded: 10,
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
  }
];

async function seedUsersAndReviews() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/teras_rumah_kayu');
    console.log('✅ Connected to MongoDB');

    // Clear existing users and reviews
    console.log('🗑️  Clearing existing users and reviews...');
    await User.deleteMany({});
    await Review.deleteMany({});
    console.log('✅ Existing users and reviews cleared');

    // Insert users
    console.log('👥 Inserting users...');
    await User.insertMany(usersData);
    console.log(`✅ Inserted ${usersData.length} users`);

    // Insert reviews
    console.log('📝 Inserting reviews...');
    await Review.insertMany(reviewsData);
    console.log(`✅ Inserted ${reviewsData.length} reviews`);

    console.log('\n📊 Users and Reviews seeded successfully!');
    console.log(`👥 Users: ${usersData.length}`);
    console.log(`📝 Reviews: ${reviewsData.length}`);

    // Show sample data
    const sampleUser = await User.findOne();
    const sampleReview = await Review.findOne();
    console.log('\n🔍 Sample data:');
    console.log(`Sample User: ${sampleUser?.name}`);
    console.log(`Sample Review: ${sampleReview?.comment.substring(0, 50)}...`);

  } catch (error) {
    console.error('❌ Error seeding users and reviews:', error);
  } finally {
    console.log('🔒 Database connection closed');
    await mongoose.connection.close();
  }
}

// Run the seeding
seedUsersAndReviews();
