const express = require('express');
const router = express.Router();
const FoodItem = require('../models/FoodItem');

// Fallback data when database is not available - minimal set for emergency
const fallbackFoodItems = [
  {
    id: 1,
    name: "Paket Sop Iga",
    price: 40000,
    rating: 4.8,
    deliveryTime: "25m",
    imageUrl: "file:///android_asset/foto-menu/sop iga.png",
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
    imageUrl: "file:///android_asset/foto-menu/pecak ikan.png",
    category: "PAKET_UTAMA",
    description: "Paket pecak ikan nila dengan nasi dan minuman",
    ingredients: ["Nasi Putih", "Ikan Nila", "Cabai Merah", "Cabai Rawit", "Bawang Merah", "Bawang Putih", "Kemiri", "Kunyit", "Jahe", "Garam", "Gula", "Lalapan", "Es Teh Manis"],
    notes: "Ikan segar dengan bumbu pecak khas"
  },
  {
    id: 11,
    name: "Sambal Dadak",
    price: 5000,
    rating: 4.3,
    deliveryTime: "5m",
    imageUrl: "file:///android_asset/foto-menu/sambal dadak.jpg",
    category: "SAMBAL",
    description: "Sambal dadak pedas segar",
    ingredients: ["Cabai Merah", "Cabai Rawit", "Tomat", "Bawang Merah", "Bawang Putih", "Garam", "Gula", "Terasi"],
    notes: "Sambal segar dengan rasa pedas yang pas"
  }
];

// Check if database is available
const isDatabaseAvailable = () => {
  return process.env.MONGODB_URI && process.env.MONGODB_URI.trim() !== '';
};

// GET /api/foodItems - Get all food items or filter by category
router.get('/', async (req, res) => {
  try {
    const { category, search, sortBy, limit, page } = req.query;

    // Use fallback data if database is not available
    if (!isDatabaseAvailable()) {
      let items = [...fallbackFoodItems];

      // Filter by category
      if (category && category !== 'ALL') {
        items = items.filter(item => item.category === category);
      }

      // Simple search
      if (search) {
        items = items.filter(item =>
          item.name.toLowerCase().includes(search.toLowerCase()) ||
          item.description.toLowerCase().includes(search.toLowerCase())
        );
      }

      return res.json({
        success: true,
        data: items,
        total: items.length,
        message: 'Data retrieved from fallback (database not available)'
      });
    }
    let query = { isActive: true };
    
    // Filter by category
    if (category && category !== 'ALL') {
      query.category = category;
    }
    
    // Search functionality
    if (search) {
      query.$text = { $search: search };
    }
    
    // Build the query
    let foodItemsQuery = FoodItem.find(query);
    
    // Sorting
    switch (sortBy) {
      case 'price_asc':
        foodItemsQuery = foodItemsQuery.sort({ price: 1 });
        break;
      case 'price_desc':
        foodItemsQuery = foodItemsQuery.sort({ price: -1 });
        break;
      case 'rating':
        foodItemsQuery = foodItemsQuery.sort({ rating: -1 });
        break;
      case 'name':
        foodItemsQuery = foodItemsQuery.sort({ name: 1 });
        break;
      default:
        foodItemsQuery = foodItemsQuery.sort({ id: 1 });
    }
    
    // Pagination
    if (limit) {
      const limitNum = parseInt(limit);
      const pageNum = parseInt(page) || 1;
      const skip = (pageNum - 1) * limitNum;
      
      foodItemsQuery = foodItemsQuery.skip(skip).limit(limitNum);
    }
    
    const foodItems = await foodItemsQuery.exec();
    
    // Get total count for pagination
    const totalCount = await FoodItem.countDocuments(query);
    
    res.json({
      success: true,
      data: foodItems,
      pagination: limit ? {
        currentPage: parseInt(page) || 1,
        totalPages: Math.ceil(totalCount / parseInt(limit)),
        totalItems: totalCount,
        itemsPerPage: parseInt(limit)
      } : null
    });
  } catch (error) {
    console.error('Error fetching food items:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching food items',
      error: error.message
    });
  }
});

// GET /api/foodItems/:id - Get specific food item
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const foodItem = await FoodItem.findOne({ id: parseInt(id), isActive: true });
    
    if (!foodItem) {
      return res.status(404).json({
        success: false,
        message: 'Food item not found'
      });
    }
    
    res.json({
      success: true,
      data: foodItem
    });
  } catch (error) {
    console.error('Error fetching food item:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching food item',
      error: error.message
    });
  }
});

// POST /api/foodItems - Create new food item (for admin)
router.post('/', async (req, res) => {
  try {
    const foodItem = new FoodItem(req.body);
    await foodItem.save();
    
    res.status(201).json({
      success: true,
      message: 'Food item created successfully',
      data: foodItem
    });
  } catch (error) {
    console.error('Error creating food item:', error);
    res.status(400).json({
      success: false,
      message: 'Error creating food item',
      error: error.message
    });
  }
});

// PUT /api/foodItems/:id - Update food item (for admin)
router.put('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const foodItem = await FoodItem.findOneAndUpdate(
      { id: parseInt(id) },
      req.body,
      { new: true, runValidators: true }
    );
    
    if (!foodItem) {
      return res.status(404).json({
        success: false,
        message: 'Food item not found'
      });
    }
    
    res.json({
      success: true,
      message: 'Food item updated successfully',
      data: foodItem
    });
  } catch (error) {
    console.error('Error updating food item:', error);
    res.status(400).json({
      success: false,
      message: 'Error updating food item',
      error: error.message
    });
  }
});

// DELETE /api/foodItems/:id - Soft delete food item (for admin)
router.delete('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const foodItem = await FoodItem.findOneAndUpdate(
      { id: parseInt(id) },
      { isActive: false },
      { new: true }
    );
    
    if (!foodItem) {
      return res.status(404).json({
        success: false,
        message: 'Food item not found'
      });
    }
    
    res.json({
      success: true,
      message: 'Food item deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting food item:', error);
    res.status(500).json({
      success: false,
      message: 'Error deleting food item',
      error: error.message
    });
  }
});

module.exports = router;
