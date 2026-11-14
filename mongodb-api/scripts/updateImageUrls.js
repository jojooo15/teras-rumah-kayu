const mongoose = require('mongoose');
const FoodItem = require('../models/FoodItem');

// Database connection
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/teras_rumah_kayu';

// Function to convert Google Drive sharing URL to direct image URL
function convertGoogleDriveUrl(shareUrl) {
  if (!shareUrl || !shareUrl.includes('drive.google.com')) {
    return shareUrl; // Return original if not a Google Drive URL
  }
  
  // Extract file ID from sharing URL
  const match = shareUrl.match(/\/file\/d\/([a-zA-Z0-9_-]+)/);
  if (match && match[1]) {
    const fileId = match[1];
    return `https://drive.google.com/uc?export=view&id=${fileId}`;
  }
  
  return shareUrl; // Return original if can't extract ID
}

// Updated image URLs - using local assets from foto-menu folder
const imageUpdates = [
  // PAKET UTAMA
  { id: 1, imageUrl: "file:///android_asset/foto-menu/sop iga.png" },
  { id: 2, imageUrl: "file:///android_asset/foto-menu/pecak ikan.png" },
  { id: 3, imageUrl: "file:///android_asset/foto-menu/pecak ikan.png" },
  { id: 4, imageUrl: "file:///android_asset/foto-menu/pecak ikan.png" },
  { id: 5, imageUrl: "file:///android_asset/foto-menu/pecak ayam.jpg" },
  { id: 6, imageUrl: "file:///android_asset/foto-menu/pecak ikan.png" },
  { id: 7, imageUrl: "file:///android_asset/foto-menu/ayam geprek.jpg" },
  { id: 8, imageUrl: "file:///android_asset/foto-menu/bawal dabu.jpg" },
  { id: 9, imageUrl: "file:///android_asset/foto-menu/paket empal.jpg" },
  { id: 10, imageUrl: "file:///android_asset/foto-menu/ayam bakar.jpeg" },
  
  // SAMBAL - Using local assets from foto-menu folder
  { id: 11, imageUrl: "file:///android_asset/foto-menu/sambal dadak.jpg" },
  { id: 12, imageUrl: "file:///android_asset/foto-menu/sambal geprek.jpg" },
  { id: 13, imageUrl: "file:///android_asset/foto-menu/sambal pecel.jpg" },
  { id: 14, imageUrl: "file:///android_asset/foto-menu/sambal ijo.jpg" },
  { id: 15, imageUrl: "file:///android_asset/foto-menu/sambal dabu dabu.jpg" },
  
  // MAKANAN RINGAN
  { id: 16, imageUrl: "file:///android_asset/foto-menu/sarapan sehat.png" },
  { id: 17, imageUrl: "file:///android_asset/foto-menu/cah kangkung.png" },
  { id: 18, imageUrl: "file:///android_asset/foto-menu/tempe penyet.png" },
  { id: 19, imageUrl: "file:///android_asset/foto-menu/telur barendo.png" },
  { id: 20, imageUrl: "file:///android_asset/foto-menu/tempe tahu.jpg" },
  { id: 21, imageUrl: "file:///android_asset/foto-menu/peda selimut jagung.png" },

  // MINUMAN
  { id: 22, imageUrl: "file:///android_asset/foto-menu/es teh tawar.jpeg" },
  { id: 23, imageUrl: "file:///android_asset/foto-menu/es teh manis.jpeg" },
  { id: 24, imageUrl: "file:///android_asset/foto-menu/es kopi.jpg" },
  { id: 25, imageUrl: "file:///android_asset/foto-menu/es susu.jpg" },
  { id: 26, imageUrl: "file:///android_asset/foto-menu/es nutri sari.jpg" },
  { id: 27, imageUrl: "file:///android_asset/foto-menu/es jeruk peras.jpg" },
  { id: 28, imageUrl: "file:///android_asset/foto-menu/teh manis panas.jpg" },
  { id: 29, imageUrl: "file:///android_asset/foto-menu/susu panas.jpeg" },
  { id: 30, imageUrl: "file:///android_asset/foto-menu/kopi panas.jpg" }
];

async function updateImageUrls() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(MONGODB_URI);
    console.log('✅ Connected to MongoDB');

    console.log('🖼️  Updating image URLs...');
    
    let updatedCount = 0;
    
    for (const update of imageUpdates) {
      const result = await FoodItem.updateOne(
        { id: update.id },
        { 
          $set: { 
            imageUrl: update.imageUrl,
            updatedAt: new Date()
          }
        }
      );
      
      if (result.modifiedCount > 0) {
        console.log(`✅ Updated image for item ID ${update.id}`);
        updatedCount++;
      } else {
        console.log(`⚠️  Item ID ${update.id} not found or already up to date`);
      }
    }

    console.log(`\n📊 Update Summary:`);
    console.log(`✅ Successfully updated ${updatedCount} items`);
    console.log(`📝 Total items processed: ${imageUpdates.length}`);

    // Verify updates
    console.log('\n🔍 Verifying updates...');
    const sampleItem = await FoodItem.findOne({ id: 11 });
    if (sampleItem) {
      console.log(`Sample item (ID 11) imageUrl: ${sampleItem.imageUrl}`);
    }

  } catch (error) {
    console.error('❌ Error updating image URLs:', error);
  } finally {
    console.log('🔒 Closing database connection...');
    await mongoose.connection.close();
  }
}

// Run the update
updateImageUrls();
