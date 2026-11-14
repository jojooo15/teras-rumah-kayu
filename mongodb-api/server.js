const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
require('dotenv').config();

// Import database connection
const connectDB = require('./config/database');

// Import routes
const foodItemsRoutes = require('./routes/foodItems');
const categoriesRoutes = require('./routes/categories');
const usersRoutes = require('./routes/users');
const reviewsRoutes = require('./routes/reviews');

const app = express();
const PORT = process.env.PORT || 3000;

// Connect to MongoDB
connectDB();

// Middleware
app.use(helmet());
app.use(morgan('combined'));
app.use(cors({
  origin: process.env.CORS_ORIGIN || '*',
  credentials: true
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Health check endpoint
app.get('/health', (req, res) => {
  console.log('Health check called');
  res.json({
    success: true,
    message: 'Teras Rumah Kayu API is running',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development',
    database: process.env.MONGODB_URI ? 'Connected' : 'Fallback mode'
  });
});

// API Routes
app.use('/api/foodItems', foodItemsRoutes);
app.use('/api/categories', categoriesRoutes);
app.use('/api/users', usersRoutes);
app.use('/api/reviews', reviewsRoutes);

// Legacy routes for backward compatibility with existing Android app
app.use('/foodItems', foodItemsRoutes);
app.use('/categories', categoriesRoutes);

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    success: true,
    message: 'Teras Rumah Kayu API Server',
    version: '1.0.0',
    endpoints: {
      health: '/health',
      api: {
        foodItems: '/api/foodItems',
        categories: '/api/categories',
        users: '/api/users',
        reviews: '/api/reviews'
      },
      legacy: {
        foodItems: '/foodItems',
        categories: '/categories'
      }
    }
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err.stack);
  res.status(500).json({
    success: false,
    message: 'Internal server error',
    error: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong'
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'Endpoint not found',
    path: req.originalUrl
  });
});
// Start server
app.listen(PORT, () => {
  console.log(`🚀 Teras Rumah Kayu API Server running on port ${PORT}`);
  console.log(`📍 Server URL: http://localhost:${PORT}`);
  console.log(`🏥 Health check: http://localhost:${PORT}/health`);
  console.log(`📊 API endpoints:`);
  console.log(`   - GET /api/foodItems`);
  console.log(`   - GET /api/categories`);
  console.log(`   - GET /api/users`);
  console.log(`   - GET /api/reviews`);
  console.log(`🔄 Legacy endpoints (Android compatibility):`);
  console.log(`   - GET /foodItems`);
  console.log(`   - GET /categories`);
  console.log(`\n🎯 Database mode: ${process.env.MONGODB_URI ? 'MongoDB Connected' : 'Fallback mode'}`);
  console.log(`✅ Server ready to accept connections!`);
});

module.exports = app;
