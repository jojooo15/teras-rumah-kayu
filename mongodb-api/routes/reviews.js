const express = require('express');
const router = express.Router();
const Review = require('../models/Review');
const User = require('../models/User');
const FoodItem = require('../models/FoodItem');

// GET /api/reviews/food/:foodItemId - Get reviews for a specific food item
router.get('/food/:foodItemId', async (req, res) => {
  try {
    const { foodItemId } = req.params;
    const { page = 1, limit = 10, sortBy = 'newest' } = req.query;
    
    const skip = (parseInt(page) - 1) * parseInt(limit);
    
    // Build sort criteria
    let sortCriteria = { createdAt: -1 }; // Default: newest first
    if (sortBy === 'oldest') sortCriteria = { createdAt: 1 };
    if (sortBy === 'highest') sortCriteria = { rating: -1, createdAt: -1 };
    if (sortBy === 'lowest') sortCriteria = { rating: 1, createdAt: -1 };
    
    const reviews = await Review.find({ 
      foodItemId: parseInt(foodItemId), 
      isActive: true 
    })
    .sort(sortCriteria)
    .limit(parseInt(limit))
    .skip(skip);
    
    // Get total count for pagination
    const totalReviews = await Review.countDocuments({ 
      foodItemId: parseInt(foodItemId), 
      isActive: true 
    });
    
    // Get average rating and stats
    const ratingStats = await Review.getAverageRating(parseInt(foodItemId));
    const reviewStats = await Review.getReviewStats(parseInt(foodItemId));
    
    res.json({
      success: true,
      data: {
        reviews,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(totalReviews / parseInt(limit)),
          totalReviews,
          hasNext: skip + reviews.length < totalReviews,
          hasPrev: parseInt(page) > 1
        },
        stats: {
          ...ratingStats,
          ...reviewStats
        }
      }
    });
  } catch (error) {
    console.error('Error fetching reviews:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching reviews',
      error: error.message
    });
  }
});

// GET /api/reviews/user/:userId - Get reviews by a specific user
router.get('/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const { page = 1, limit = 10 } = req.query;
    
    const skip = (parseInt(page) - 1) * parseInt(limit);
    
    const reviews = await Review.find({ 
      userId: userId, 
      isActive: true 
    })
    .sort({ createdAt: -1 })
    .limit(parseInt(limit))
    .skip(skip);
    
    const totalReviews = await Review.countDocuments({ 
      userId: userId, 
      isActive: true 
    });
    
    res.json({
      success: true,
      data: {
        reviews,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(totalReviews / parseInt(limit)),
          totalReviews,
          hasNext: skip + reviews.length < totalReviews,
          hasPrev: parseInt(page) > 1
        }
      }
    });
  } catch (error) {
    console.error('Error fetching user reviews:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching user reviews',
      error: error.message
    });
  }
});

// POST /api/reviews - Create a new review
router.post('/', async (req, res) => {
  try {
    const { userId, foodItemId, rating, comment, userName, userPhone } = req.body;
    
    // Validate required fields
    if (!userId || !foodItemId || !rating || !comment || !userName || !userPhone) {
      return res.status(400).json({
        success: false,
        message: 'All fields are required'
      });
    }
    
    // Validate rating range
    if (rating < 1 || rating > 5) {
      return res.status(400).json({
        success: false,
        message: 'Rating must be between 1 and 5'
      });
    }
    
    // Check if user already reviewed this food item
    const existingReview = await Review.findOne({
      userId: userId,
      foodItemId: parseInt(foodItemId),
      isActive: true
    });
    
    if (existingReview) {
      return res.status(400).json({
        success: false,
        message: 'You have already reviewed this item'
      });
    }
    
    // Verify food item exists
    const foodItem = await FoodItem.findOne({ id: parseInt(foodItemId), isActive: true });
    if (!foodItem) {
      return res.status(404).json({
        success: false,
        message: 'Food item not found'
      });
    }
    
    // Create review
    const review = new Review({
      userId,
      foodItemId: parseInt(foodItemId),
      rating: parseInt(rating),
      comment: comment.trim(),
      userName: userName.trim(),
      userPhone: userPhone.trim(),
      pointsAwarded: 10 // Default points for review
    });
    
    await review.save();
    
    // Update user points and review stats
    try {
      let user = await User.findOne({ id: userId });
      if (!user) {
        // Create user if doesn't exist
        user = new User({
          id: userId,
          phoneNumber: userPhone,
          name: userName,
          points: 0,
          totalReviews: 0,
          averageRatingGiven: 0
        });
      }
      
      // Add points for review
      await user.addPoints(10, `Review untuk ${foodItem.name}`, review._id.toString());
      await user.updateReviewStats(parseInt(rating));
      
    } catch (userError) {
      console.error('Error updating user stats:', userError);
      // Don't fail the review creation if user update fails
    }
    
    res.status(201).json({
      success: true,
      message: 'Review created successfully',
      data: review
    });
    
  } catch (error) {
    console.error('Error creating review:', error);
    res.status(500).json({
      success: false,
      message: 'Error creating review',
      error: error.message
    });
  }
});

// PUT /api/reviews/:reviewId - Update a review (user can edit their own review)
router.put('/:reviewId', async (req, res) => {
  try {
    const { reviewId } = req.params;
    const { userId, rating, comment } = req.body;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'User ID is required'
      });
    }
    
    const review = await Review.findById(reviewId);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: 'Review not found'
      });
    }
    
    // Check if user owns this review
    if (review.userId !== userId) {
      return res.status(403).json({
        success: false,
        message: 'You can only edit your own reviews'
      });
    }
    
    // Update review
    if (rating) {
      if (rating < 1 || rating > 5) {
        return res.status(400).json({
          success: false,
          message: 'Rating must be between 1 and 5'
        });
      }
      review.rating = parseInt(rating);
    }
    
    if (comment) {
      review.comment = comment.trim();
    }
    
    await review.save();
    
    res.json({
      success: true,
      message: 'Review updated successfully',
      data: review
    });
    
  } catch (error) {
    console.error('Error updating review:', error);
    res.status(500).json({
      success: false,
      message: 'Error updating review',
      error: error.message
    });
  }
});

// DELETE /api/reviews/:reviewId - Delete a review (soft delete)
router.delete('/:reviewId', async (req, res) => {
  try {
    const { reviewId } = req.params;
    const { userId } = req.body;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'User ID is required'
      });
    }
    
    const review = await Review.findById(reviewId);
    if (!review) {
      return res.status(404).json({
        success: false,
        message: 'Review not found'
      });
    }
    
    // Check if user owns this review
    if (review.userId !== userId) {
      return res.status(403).json({
        success: false,
        message: 'You can only delete your own reviews'
      });
    }
    
    // Soft delete
    review.isActive = false;
    await review.save();
    
    res.json({
      success: true,
      message: 'Review deleted successfully'
    });
    
  } catch (error) {
    console.error('Error deleting review:', error);
    res.status(500).json({
      success: false,
      message: 'Error deleting review',
      error: error.message
    });
  }
});

// GET /api/reviews/stats/:foodItemId - Get detailed review statistics for a food item
router.get('/stats/:foodItemId', async (req, res) => {
  try {
    const { foodItemId } = req.params;
    
    const ratingStats = await Review.getAverageRating(parseInt(foodItemId));
    const reviewStats = await Review.getReviewStats(parseInt(foodItemId));
    
    res.json({
      success: true,
      data: {
        ...ratingStats,
        ...reviewStats
      }
    });
  } catch (error) {
    console.error('Error fetching review stats:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching review stats',
      error: error.message
    });
  }
});

module.exports = router;
