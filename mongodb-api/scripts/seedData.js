const mongoose = require('mongoose');
require('dotenv').config();

const FoodItem = require('../models/FoodItem');
const Category = require('../models/Category');

// Categories data (Optimized - 5 categories)
const categoriesData = [
  {
    id: "ALL",
    displayName: "Semua Menu",
    description: "Semua kategori menu",
    sortOrder: 0
  },
  {
    id: "PAKET_UTAMA",
    displayName: "Paket Makanan Utama",
    description: "Paket makanan lengkap dengan nasi dan pelengkap (Sop, Pecak, Geprek, Bawal, Empal, Ayam Bakar)",
    sortOrder: 1
  },
  {
    id: "SAMBAL",
    displayName: "Macam Sambal",
    description: "Berbagai macam sambal pelengkap",
    sortOrder: 2
  },
  {
    id: "MAKANAN_RINGAN",
    displayName: "Makanan Ringan & Dadakan",
    description: "Menu ringan, sarapan sehat, dan makanan dadakan",
    sortOrder: 3
  },
  {
    id: "MINUMAN",
    displayName: "Minuman",
    description: "Berbagai pilihan minuman segar dan hangat",
    sortOrder: 4
  }
];

// Food items data - EXACT COPY from Android Application FoodRepository.kt
const foodItemsData = [
  // PAKET MAKANAN UTAMA
  {
    id: 1,
    name: "Paket Sop Iga",
    price: 40000,
    rating: 4.8,
    deliveryTime: "25m",
    imageUrl: "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_wooden_house_logo",
    category: "PAKET_UTAMA",
    description: "Paket lengkap sop iga dengan nasi dan minuman",
    ingredients: ["Nasi Putih", "Iga Sapi", "Wortel", "Kentang", "Daun Bawang", "Seledri", "Bawang Merah", "Bawang Putih", "Jahe", "Garam", "Merica", "Es Teh Manis"],
    notes: "Paket hemat dan mengenyangkan"
  },
  {
    id: 2,
    name: "Paket Pecak Nila",
    price: 28000,
    rating: 4.6,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1544943910-4c1dc44aab44?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket pecak ikan nila dengan nasi dan minuman",
    ingredients: ["Nasi", "Pecak Nila", "Minuman"],
    notes: "Ikan segar dengan bumbu pecak khas"
  },
  {
    id: 3,
    name: "Paket Pecak Mas",
    price: 28000,
    rating: 4.6,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket pecak ikan mas dengan nasi dan minuman",
    ingredients: ["Nasi", "Pecak Mas", "Minuman"],
    notes: "Ikan mas segar dengan bumbu pecak"
  },
  {
    id: 4,
    name: "Paket Pecak Lele",
    price: 28000,
    rating: 4.5,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1559847844-d721426d6edc?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket pecak ikan lele dengan nasi dan minuman",
    ingredients: ["Nasi", "Pecak Lele", "Minuman"],
    notes: "Lele crispy dengan bumbu pecak pedas"
  },
  {
    id: 5,
    name: "Paket Pecak Ayam",
    price: 28000,
    rating: 4.7,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket pecak ayam dengan nasi dan minuman",
    ingredients: ["Nasi", "Pecak Ayam", "Minuman"],
    notes: "Ayam dengan bumbu pecak yang gurih"
  },
  {
    id: 6,
    name: "Paket Pecak Gurame (+Rp 10.000)",
    price: 38000,
    rating: 4.8,
    deliveryTime: "25m",
    imageUrl: "https://images.unsplash.com/photo-1544943910-4c1dc44aab44?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket pecak ikan gurame premium dengan nasi dan minuman",
    ingredients: ["Nasi", "Pecak Gurame", "Minuman"],
    notes: "Gurame segar dengan bumbu pecak istimewa"
  },
  {
    id: 7,
    name: "Paket Geprek",
    price: 23000,
    rating: 4.5,
    deliveryTime: "15m",
    imageUrl: "https://images.unsplash.com/photo-1606491956689-2ea866880c84?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket ayam geprek dengan nasi dan lalapan",
    ingredients: ["Nasi", "Ayam/Lele", "Lalap", "Sambal"],
    notes: "Pilihan level kepedasan tersedia"
  },
  {
    id: 8,
    name: "Paket Bawal Dabu",
    price: 22000,
    rating: 4.6,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket ikan bawal dengan sambal dabu-dabu khas Manado",
    ingredients: ["Nasi", "Bawal", "Sambal Dabu"],
    notes: "Cita rasa khas Manado yang segar"
  },
  {
    id: 9,
    name: "Paket Empal",
    price: 30000,
    rating: 4.7,
    deliveryTime: "20m",
    imageUrl: "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket empal daging dengan nasi dan pelengkap",
    ingredients: ["Nasi", "Daging Empal", "Tahu/Tempe", "Lalap", "Sambal", "Minuman"],
    notes: "Empal daging empuk dengan bumbu tradisional"
  },
  {
    id: 10,
    name: "Paket Ayam Bakar",
    price: 24000,
    rating: 4.8,
    deliveryTime: "25m",
    imageUrl: "https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "PAKET_UTAMA",
    description: "Paket ayam bakar dengan bumbu khas",
    ingredients: ["Nasi", "Ayam Paha/Dada", "Lalap", "Sambel"],
    notes: "Ayam bakar dengan bumbu meresap sempurna"
  },

  // MACAM SAMBAL
  {
    id: 11,
    name: "Sambal Dadak",
    price: 5000,
    rating: 4.3,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
    category: "SAMBAL",
    description: "Sambal dadak pedas segar",
    ingredients: ["Cabai", "Tomat", "Bawang"],
    notes: "Sambal segar dengan rasa pedas yang pas"
  },
  {
    id: 12,
    name: "Sambal Geprek",
    price: 5000,
    rating: 4.4,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
    category: "SAMBAL",
    description: "Sambal geprek dengan cabai yang digeprek kasar",
    ingredients: ["Cabai rawit", "Garam", "Terasi"],
    notes: "Sambal geprek dengan tekstur kasar"
  },
  {
    id: 13,
    name: "Sambal Pecel",
    price: 5000,
    rating: 4.2,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
    category: "SAMBAL",
    description: "Sambal pecel khas Jawa Timur",
    ingredients: ["Kacang tanah", "Cabai", "Gula merah"],
    notes: "Sambal pecel manis pedas"
  },
  {
    id: 14,
    name: "Sambal Ijo",
    price: 5000,
    rating: 4.5,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
    category: "SAMBAL",
    description: "Sambal hijau khas Padang",
    ingredients: ["Cabai hijau", "Bawang", "Garam"],
    notes: "Sambal hijau dengan rasa segar"
  },
  {
    id: 15,
    name: "Sambal Dabu",
    price: 5000,
    rating: 4.6,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_sambal_placeholder",
    category: "SAMBAL",
    description: "Sambal dabu-dabu khas Manado",
    ingredients: ["Cabai", "Tomat", "Bawang merah", "Jeruk nipis"],
    notes: "Sambal segar khas Manado"
  },
  // MAKANAN RINGAN & DADAKAN
  {
    id: 16,
    name: "Sarapan Sehat",
    price: 20000,
    rating: 4.4,
    deliveryTime: "15m",
    imageUrl: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=500&h=400&fit=crop",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Menu sarapan sehat lengkap dengan berbagai sayuran dan protein",
    ingredients: ["Kentang Rebus", "Brokoli", "Katsu/Dada Ayam", "Wortel", "Mayo"],
    notes: "Menu bergizi lengkap untuk memulai hari dengan energi"
  },
  {
    id: 17,
    name: "Cah Kangkung",
    price: 10000,
    rating: 4.2,
    deliveryTime: "10m",
    imageUrl: "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Kangkung segar ditumis dengan bumbu sederhana",
    ingredients: ["Kangkung", "Bawang putih", "Cabai", "Kecap"],
    notes: "Sayuran segar dan sehat"
  },
  {
    id: 18,
    name: "Tempe Penyet",
    price: 10000,
    rating: 4.3,
    deliveryTime: "10m",
    imageUrl: "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Tempe goreng yang dipenyet dengan sambal",
    ingredients: ["Tempe", "Sambal", "Lalapan"],
    notes: "Protein nabati yang lezat"
  },
  {
    id: 19,
    name: "Telor Barendo/Dadar/Ceplok",
    price: 5000,
    rating: 4.0,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1506084868230-bb9d95c24759?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Telur dengan berbagai pilihan masakan",
    ingredients: ["Telur", "Minyak goreng", "Garam"],
    notes: "Pilihan: barendo, dadar, atau ceplok"
  },
  {
    id: 20,
    name: "Tempe/Tahu 1 Porsi",
    price: 5000,
    rating: 4.1,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1559847844-5315695dadae?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Tempe atau tahu goreng 1 porsi",
    ingredients: ["Tempe/Tahu", "Minyak goreng"],
    notes: "Pilihan tempe atau tahu"
  },
  {
    id: 21,
    name: "Peda Selimut Jagung",
    price: 15000,
    rating: 4.5,
    deliveryTime: "15m",
    imageUrl: "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MAKANAN_RINGAN",
    description: "Ikan peda dengan selimut jagung yang unik",
    ingredients: ["Ikan Peda", "Jagung", "Bumbu"],
    notes: "Menu khas dengan cita rasa istimewa"
  },

  // MINUMAN
  {
    id: 22,
    name: "Es Teh Tawar",
    price: 3000,
    rating: 4.0,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Es teh tawar segar tanpa gula",
    ingredients: ["Teh", "Es batu"],
    notes: "Minuman sehat tanpa gula"
  },
  {
    id: 23,
    name: "Es Teh Manis",
    price: 5000,
    rating: 4.2,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Es teh manis yang menyegarkan",
    ingredients: ["Teh", "Gula", "Es batu"],
    notes: "Minuman favorit yang menyegarkan"
  },
  {
    id: 24,
    name: "Es Kopi (All Variant)",
    price: 7000,
    rating: 4.3,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Es kopi dengan berbagai varian rasa",
    ingredients: ["Kopi", "Gula", "Es batu"],
    notes: "Tersedia berbagai varian kopi"
  },
  {
    id: 25,
    name: "Es Susu",
    price: 7000,
    rating: 4.1,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1553909489-cd47e0ef937f?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Es susu segar dan creamy",
    ingredients: ["Susu", "Gula", "Es batu"],
    notes: "Minuman susu yang creamy"
  },
  {
    id: 26,
    name: "Es Nutri Sari",
    price: 7000,
    rating: 4.0,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Minuman sari buah yang menyegarkan",
    ingredients: ["Nutri Sari", "Air", "Es batu"],
    notes: "Minuman vitamin yang menyegarkan"
  },
  {
    id: 27,
    name: "Es Jeruk Peras",
    price: 10000,
    rating: 4.4,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Es jeruk peras segar langsung dari buah",
    ingredients: ["Jeruk segar", "Gula", "Es batu"],
    notes: "Jeruk segar diperas langsung"
  },
  {
    id: 28,
    name: "Teh Manis Panas",
    price: 3000,
    rating: 4.1,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1562967914-608f82629710?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Teh manis hangat yang menghangatkan",
    ingredients: ["Teh", "Gula", "Air panas"],
    notes: "Minuman hangat untuk cuaca dingin"
  },
  {
    id: 29,
    name: "Susu Panas",
    price: 5000,
    rating: 4.0,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Susu hangat yang menenangkan",
    ingredients: ["Susu", "Gula"],
    notes: "Minuman hangat yang menenangkan"
  },
  {
    id: 30,
    name: "Kopi",
    price: 5000,
    rating: 4.2,
    deliveryTime: "5m",
    imageUrl: "https://images.unsplash.com/photo-1580476262798-bddd9f4b7369?w=500",
    fallbackImageUrl: "android.resource://com.jojo.terasrumah/drawable/ic_food_placeholder",
    category: "MINUMAN",
    description: "Kopi panas klasik",
    ingredients: ["Kopi", "Gula", "Air panas"],
    notes: "Kopi panas untuk memulai hari"
  }
];

// Removed voucher data - using points only


async function seedDatabase() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('✅ Connected to MongoDB');

    // Clear existing data
    console.log('🗑️  Clearing existing data...');
    await FoodItem.deleteMany({});
    await Category.deleteMany({});
    console.log('✅ Existing data cleared');

    // Insert categories
    console.log('📂 Inserting categories...');
    await Category.insertMany(categoriesData);
    console.log(`✅ Inserted ${categoriesData.length} categories`);

    // Insert food items
    console.log('🍽️  Inserting food items...');
    await FoodItem.insertMany(foodItemsData);
    console.log(`✅ Inserted ${foodItemsData.length} food items`);

    // Removed voucher insertion - using points only

    // Verify data
    const categoryCount = await Category.countDocuments();
    const foodItemCount = await FoodItem.countDocuments();

    console.log('\n📊 Database seeded successfully!');
    console.log(`📂 Categories: ${categoryCount}`);
    console.log(`🍽️  Food Items: ${foodItemCount}`);

    // Show sample data
    console.log('\n🔍 Sample data:');
    const sampleCategory = await Category.findOne();
    const sampleFoodItem = await FoodItem.findOne();

    console.log('Sample Category:', sampleCategory?.displayName);
    console.log('Sample Food Item:', sampleFoodItem?.name);

  } catch (error) {
    console.error('❌ Error seeding database:', error);
  } finally {
    await mongoose.connection.close();
    console.log('🔒 Database connection closed');
    process.exit(0);
  }
}

// Run the seed function
if (require.main === module) {
  seedDatabase();
}

module.exports = { seedDatabase, categoriesData, foodItemsData };
