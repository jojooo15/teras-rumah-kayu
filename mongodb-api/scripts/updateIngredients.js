const mongoose = require('mongoose');
require('dotenv').config();

// Connect to MongoDB
async function connectDB() {
  try {
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/teras_rumah_kayu');
    console.log('🔌 Connected to MongoDB');
  } catch (error) {
    console.error('❌ MongoDB connection error:', error);
    process.exit(1);
  }
}

// Updated ingredients data
const ingredientsUpdates = [
  // PAKET UTAMA
  { 
    id: 1, 
    ingredients: ["Nasi Putih", "Iga Sapi", "Wortel", "Kentang", "Daun Bawang", "Seledri", "Bawang Merah", "Bawang Putih", "Jahe", "Garam", "Merica", "Es Teh Manis"]
  },
  { 
    id: 2, 
    ingredients: ["Nasi Putih", "Ikan Nila", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 3, 
    ingredients: ["Nasi Putih", "Ikan Mas", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 4, 
    ingredients: ["Nasi Putih", "Ikan Lele", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 5, 
    ingredients: ["Nasi Putih", "Ayam", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 6, 
    ingredients: ["Nasi Putih", "Ikan Gurame", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 7, 
    ingredients: ["Nasi Putih", "Ayam Fillet", "Tepung Terigu", "Cabai Rawit", "Cabai Merah", "Bawang Putih", "Garam", "Gula", "Lalapan", "Timun", "Tomat"]
  },
  { 
    id: 8, 
    ingredients: ["Nasi Putih", "Ikan Bawal", "Cabai Rawit", "Tomat", "Bawang Merah", "Jeruk Nipis", "Garam", "Gula", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 9, 
    ingredients: ["Nasi Putih", "Daging Sapi", "Kelapa Parut", "Ketumbar", "Jinten", "Bawang Merah", "Bawang Putih", "Garam", "Gula Merah", "Tahu", "Tempe", "Lalapan", "Es Teh Manis"]
  },
  { 
    id: 10, 
    ingredients: ["Nasi Putih", "Ayam", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Lengkuas", "Ketumbar", "Garam", "Gula Merah", "Lalapan", "Sambal", "Es Teh Manis"]
  },
  
  // SAMBAL
  { 
    id: 11, 
    ingredients: ["Cabai Merah", "Cabai Rawit", "Tomat", "Bawang Merah", "Bawang Putih", "Garam", "Gula", "Terasi"]
  },
  { 
    id: 12, 
    ingredients: ["Cabai Rawit", "Cabai Merah", "Bawang Putih", "Garam", "Gula", "Terasi"]
  },
  { 
    id: 13, 
    ingredients: ["Kacang Tanah", "Cabai Rawit", "Cabai Merah", "Gula Merah", "Asam Jawa", "Garam", "Kencur"]
  },
  { 
    id: 14, 
    ingredients: ["Cabai Hijau Besar", "Cabai Rawit Hijau", "Bawang Merah", "Bawang Putih", "Kemiri", "Garam", "Gula", "Minyak"]
  },
  { 
    id: 15, 
    ingredients: ["Cabai", "Tomat", "Bawang merah", "Jeruk nipis"]
  },
  
  // MAKANAN RINGAN
  { 
    id: 16, 
    ingredients: ["Kentang Rebus", "Brokoli Rebus", "Dada Ayam Panggang", "Wortel Rebus", "Telur Rebus", "Saus Tomat", "Lada", "Garam"]
  },
  { 
    id: 17, 
    ingredients: ["Kangkung", "Bawang Putih", "Bawang Merah", "Cabai Merah", "Kecap Manis", "Garam", "Gula", "Minyak Goreng"]
  },
  { 
    id: 18, 
    ingredients: ["Tempe", "Cabai Rawit", "Cabai Merah", "Bawang Putih", "Tomat", "Garam", "Gula", "Terasi", "Lalapan"]
  },
  { 
    id: 19, 
    ingredients: ["Telur", "Minyak goreng", "Garam"]
  },
  { 
    id: 20, 
    ingredients: ["Tempe/Tahu", "Minyak goreng"]
  },
  { 
    id: 21, 
    ingredients: ["Ikan Peda", "Jagung Manis", "Tepung Terigu", "Telur", "Bawang Putih", "Merica", "Garam", "Minyak Goreng"]
  },
  
  // MINUMAN - Keep existing ingredients (already correct)
  { id: 22, ingredients: ["Teh", "Es batu"] },
  { id: 23, ingredients: ["Teh", "Gula", "Es batu"] },
  { id: 24, ingredients: ["Kopi", "Gula", "Es batu"] },
  { id: 25, ingredients: ["Susu", "Gula", "Es batu"] },
  { id: 26, ingredients: ["Nutri Sari", "Air", "Es batu"] },
  { id: 27, ingredients: ["Jeruk segar", "Gula", "Es batu"] },
  { id: 28, ingredients: ["Teh", "Gula", "Air panas"] },
  { id: 29, ingredients: ["Susu", "Gula"] },
  { id: 30, ingredients: ["Kopi", "Gula", "Air panas"] }
];

async function updateIngredients() {
  try {
    console.log('🍽️  Updating ingredients...');
    
    for (const update of ingredientsUpdates) {
      const result = await mongoose.connection.db.collection('fooditems').updateOne(
        { id: update.id },
        { $set: { ingredients: update.ingredients } }
      );
      
      if (result.matchedCount > 0) {
        console.log(`✅ Updated ingredients for item ID ${update.id}`);
      } else {
        console.log(`⚠️  Item ID ${update.id} not found or already up to date`);
      }
    }
    
    console.log('\n📊 Update Summary:');
    console.log(`✅ Successfully processed ${ingredientsUpdates.length} items`);
    
    // Verify updates
    console.log('\n🔍 Verifying updates...');
    const sampleItem = await mongoose.connection.db.collection('fooditems').findOne({ id: 2 });
    console.log(`Sample item (ID 2) ingredients: ${JSON.stringify(sampleItem?.ingredients)}`);
    
  } catch (error) {
    console.error('❌ Error updating ingredients:', error);
  }
}

async function main() {
  await connectDB();
  await updateIngredients();
  
  console.log('🔒 Closing database connection...');
  await mongoose.connection.close();
  process.exit(0);
}

main().catch(console.error);
