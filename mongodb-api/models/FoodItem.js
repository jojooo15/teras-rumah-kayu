const mongoose = require('mongoose');

const foodItemSchema = new mongoose.Schema({
  id: {
    type: Number,
    required: true,
    unique: true
  },
  name: {
    type: String,
    required: true,
    trim: true
  },
  price: {
    type: Number,
    required: true,
    min: 0
  },
  rating: {
    type: Number,
    required: true,
    min: 0,
    max: 5
  },
  deliveryTime: {
    type: String,
    required: true
  },
  imageUrl: {
    type: String,
    required: true
  },
  fallbackImageUrl: {
    type: String,
    default: null
  },
  category: {
    type: String,
    required: true,
    enum: [
      'PAKET_UTAMA',
      'SAMBAL',
      'MAKANAN_RINGAN',
      'MINUMAN'
    ]
  },
  description: {
    type: String,
    default: ''
  },
  ingredients: [{
    type: String
  }],
  notes: {
    type: String,
    default: ''
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Index for better query performance
foodItemSchema.index({ category: 1 });
foodItemSchema.index({ name: 'text', description: 'text' });
foodItemSchema.index({ price: 1 });
foodItemSchema.index({ rating: -1 });

// Virtual for formatted price
foodItemSchema.virtual('formattedPrice').get(function() {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR'
  }).format(this.price);
});

// Static method to find by category
foodItemSchema.statics.findByCategory = function(category) {
  if (category === 'ALL') {
    return this.find({ isActive: true });
  }
  return this.find({ category: category, isActive: true });
};

// Static method for search
foodItemSchema.statics.search = function(query) {
  return this.find({
    $text: { $search: query },
    isActive: true
  });
};

module.exports = mongoose.model('FoodItem', foodItemSchema);
