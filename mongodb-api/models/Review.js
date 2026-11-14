const mongoose = require('mongoose');

const reviewSchema = new mongoose.Schema({
  userId: {
    type: String,
    required: true,
    trim: true
  },
  foodItemId: {
    type: Number,
    required: true
  },
  rating: {
    type: Number,
    required: true,
    min: 1,
    max: 5
  },
  comment: {
    type: String,
    required: true,
    trim: true,
    maxlength: 500
  },
  userName: {
    type: String,
    required: true,
    trim: true
  },
  userPhone: {
    type: String,
    required: true,
    trim: true
  },
  isVerified: {
    type: Boolean,
    default: false
  },
  isActive: {
    type: Boolean,
    default: true
  },
  pointsAwarded: {
    type: Number,
    default: 10 // Default points for each review
  },
  adminResponse: {
    type: String,
    default: null
  },
  adminResponseDate: {
    type: Date,
    default: null
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Index for better query performance
reviewSchema.index({ foodItemId: 1, createdAt: -1 });
reviewSchema.index({ userId: 1, createdAt: -1 });
reviewSchema.index({ rating: -1 });
reviewSchema.index({ isActive: 1, isVerified: 1 });

// Virtual for formatted date
reviewSchema.virtual('formattedDate').get(function() {
  return this.createdAt.toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
});

// Static method to get reviews by food item
reviewSchema.statics.getByFoodItem = function(foodItemId, limit = 10, skip = 0) {
  return this.find({ 
    foodItemId: foodItemId, 
    isActive: true 
  })
  .sort({ createdAt: -1 })
  .limit(limit)
  .skip(skip);
};

// Static method to get reviews by user
reviewSchema.statics.getByUser = function(userId, limit = 10, skip = 0) {
  return this.find({ 
    userId: userId, 
    isActive: true 
  })
  .sort({ createdAt: -1 })
  .limit(limit)
  .skip(skip);
};

// Static method to get average rating for a food item
reviewSchema.statics.getAverageRating = async function(foodItemId) {
  const result = await this.aggregate([
    {
      $match: { 
        foodItemId: foodItemId, 
        isActive: true 
      }
    },
    {
      $group: {
        _id: null,
        averageRating: { $avg: '$rating' },
        totalReviews: { $sum: 1 }
      }
    }
  ]);
  
  if (result.length > 0) {
    return {
      averageRating: Math.round(result[0].averageRating * 10) / 10,
      totalReviews: result[0].totalReviews
    };
  }
  
  return {
    averageRating: 0,
    totalReviews: 0
  };
};

// Static method to get review statistics
reviewSchema.statics.getReviewStats = async function(foodItemId) {
  const stats = await this.aggregate([
    {
      $match: { 
        foodItemId: foodItemId, 
        isActive: true 
      }
    },
    {
      $group: {
        _id: '$rating',
        count: { $sum: 1 }
      }
    },
    {
      $sort: { _id: -1 }
    }
  ]);
  
  // Initialize rating distribution
  const distribution = {
    5: 0, 4: 0, 3: 0, 2: 0, 1: 0
  };
  
  stats.forEach(stat => {
    distribution[stat._id] = stat.count;
  });
  
  const totalReviews = stats.reduce((sum, stat) => sum + stat.count, 0);
  
  return {
    distribution,
    totalReviews
  };
};

// Pre-save middleware to validate review
reviewSchema.pre('save', function(next) {
  // Ensure rating is within valid range
  if (this.rating < 1 || this.rating > 5) {
    next(new Error('Rating must be between 1 and 5'));
  }
  
  // Ensure comment is not empty
  if (!this.comment || this.comment.trim().length === 0) {
    next(new Error('Comment cannot be empty'));
  }
  
  next();
});

module.exports = mongoose.model('Review', reviewSchema);
