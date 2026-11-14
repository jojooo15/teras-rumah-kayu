const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Fallback data
const fallbackFoodItems = [
  {
    id: 1,
    name: "Paket Sop Iga",
    price: 25000,
    rating: 4.5,
    deliveryTime: "20-30 min",
    imageUrl: "https://images.unsplash.com/photo-1547592180-85f173990554?w=400",
    fallbackImageUrl: null,
    category: "PAKET_UTAMA",
    description: "Sop iga sapi yang lezat dengan kuah bening",
    ingredients: ["Iga sapi", "Wortel", "Kentang", "Bawang bombay"],
    notes: "Tersedia level pedas"
  },
  {
    id: 2,
    name: "Paket Pecak Lele",
    price: 20000,
    rating: 4.3,
    deliveryTime: "15-25 min",
    imageUrl: "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400",
    fallbackImageUrl: null,
    category: "PAKET_UTAMA",
    description: "Lele goreng dengan sambal pecak khas",
    ingredients: ["Lele", "Sambal pecak", "Lalapan", "Nasi"],
    notes: "Sambal bisa disesuaikan tingkat kepedasan"
  },
  {
    id: 3,
    name: "Sambal Terasi",
    price: 5000,
    rating: 4.7,
    deliveryTime: "5-10 min",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400",
    fallbackImageUrl: null,
    category: "SAMBAL",
    description: "Sambal terasi pedas manis",
    ingredients: ["Cabai", "Terasi", "Gula merah", "Bawang"],
    notes: "Level pedas sedang"
  },
  {
    id: 4,
    name: "Nasi Gudeg",
    price: 18000,
    rating: 4.4,
    deliveryTime: "15-20 min",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400",
    fallbackImageUrl: null,
    category: "PAKET_UTAMA",
    description: "Gudeg khas Yogyakarta dengan nangka muda",
    ingredients: ["Nangka muda", "Santan", "Gula jawa", "Bumbu gudeg"],
    notes: "Tersedia dengan ayam atau tanpa ayam"
  },
  {
    id: 5,
    name: "Es Teh Manis",
    price: 3000,
    rating: 4.2,
    deliveryTime: "5 min",
    imageUrl: "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400",
    fallbackImageUrl: null,
    category: "MINUMAN",
    description: "Es teh manis segar",
    ingredients: ["Teh", "Gula", "Es batu"],
    notes: "Bisa request tingkat kemanisan"
  }
];

// Health check
app.get('/health', (req, res) => {
  console.log('Health check called');
  res.json({
    success: true,
    message: 'Test server is running',
    timestamp: new Date().toISOString()
  });
});

// Legacy endpoint
app.get('/foodItems', (req, res) => {
  console.log('Legacy /foodItems endpoint called');
  const { category } = req.query;
  console.log('Category filter:', category);
  
  let items = [...fallbackFoodItems];
  
  // Filter by category
  if (category && category !== 'ALL') {
    items = items.filter(item => item.category === category);
    console.log('Filtered items:', items.length);
  }
  
  console.log('Returning items:', items.length);
  res.json(items);
});

// API endpoint
app.get('/api/foodItems', (req, res) => {
  console.log('API /api/foodItems endpoint called');
  const { category } = req.query;
  
  let items = [...fallbackFoodItems];
  
  if (category && category !== 'ALL') {
    items = items.filter(item => item.category === category);
  }
  
  res.json({
    success: true,
    data: items,
    total: items.length
  });
});

// Categories endpoint
app.get('/categories', (req, res) => {
  console.log('Categories endpoint called');
  const categories = [
    { id: 'ALL', displayName: 'Semua Menu', description: 'Semua kategori menu' },
    { id: 'PAKET_UTAMA', displayName: 'Paket Makanan Utama', description: 'Paket makanan lengkap' },
    { id: 'SAMBAL', displayName: 'Macam Sambal', description: 'Berbagai jenis sambal' },
    { id: 'MINUMAN', displayName: 'Minuman', description: 'Minuman segar' }
  ];
  
  res.json(categories);
});

app.get('/api/categories', (req, res) => {
  console.log('API Categories endpoint called');
  const categories = [
    { id: 'ALL', displayName: 'Semua Menu', description: 'Semua kategori menu' },
    { id: 'PAKET_UTAMA', displayName: 'Paket Makanan Utama', description: 'Paket makanan lengkap' },
    { id: 'SAMBAL', displayName: 'Macam Sambal', description: 'Berbagai jenis sambal' },
    { id: 'MINUMAN', displayName: 'Minuman', description: 'Minuman segar' }
  ];
  
  res.json({
    success: true,
    data: categories
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Test server running on http://localhost:${PORT}`);
  console.log('📊 Available endpoints:');
  console.log('  - GET /health');
  console.log('  - GET /foodItems');
  console.log('  - GET /api/foodItems');
  console.log('  - GET /categories');
  console.log('  - GET /api/categories');
});
