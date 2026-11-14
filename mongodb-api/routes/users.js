const express = require('express');
const router = express.Router();
const User = require('../models/User');

// GET /api/users/:userId - Get user profile with points
router.get('/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    
    const user = await User.findOne({ id: userId, isActive: true });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }
    
    res.json({
      success: true,
      data: user
    });
  } catch (error) {
    console.error('Error fetching user:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching user',
      error: error.message
    });
  }
});

// GET /api/users/:userId/points - Get user points (simplified)
router.get('/:userId/points', async (req, res) => {
  try {
    const { userId } = req.params;

    const user = await User.findOne({ id: userId, isActive: true });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    res.json({
      success: true,
      data: {
        currentPoints: user.points,
        userLevel: user.userLevel,
        nextLevelPoints: user.nextLevelPoints,
        totalReviews: user.totalReviews
      }
    });
  } catch (error) {
    console.error('Error fetching user points:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching user points',
      error: error.message
    });
  }
});

// POST /api/users/:userId/points/add - Add points to user (simplified)
router.post('/:userId/points/add', async (req, res) => {
  try {
    const { userId } = req.params;
    const { points } = req.body;

    if (!points || points <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Points must be a positive number'
      });
    }

    const user = await User.findOne({ id: userId, isActive: true });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    await user.addPoints(parseInt(points));

    res.json({
      success: true,
      message: 'Points added successfully',
      data: {
        currentPoints: user.points,
        pointsAdded: parseInt(points)
      }
    });
  } catch (error) {
    console.error('Error adding points:', error);
    res.status(500).json({
      success: false,
      message: 'Error adding points',
      error: error.message
    });
  }
});

// GET /api/users/leaderboard/reviewers - Get top reviewers leaderboard
router.get('/leaderboard/reviewers', async (req, res) => {
  try {
    const { limit = 10 } = req.query;
    
    const topReviewers = await User.getTopReviewers(parseInt(limit));
    
    res.json({
      success: true,
      data: topReviewers
    });
  } catch (error) {
    console.error('Error fetching top reviewers:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching top reviewers',
      error: error.message
    });
  }
});

// GET /api/users/leaderboard/reviewers - Get top reviewers (simplified)
router.get('/leaderboard/reviewers', async (req, res) => {
  try {
    const { limit = 10 } = req.query;

    const users = await User.find({ isActive: true })
      .sort({ totalReviews: -1, points: -1 })
      .limit(parseInt(limit))
      .select('name phoneNumber totalReviews points');

    res.json({
      success: true,
      data: users
    });
  } catch (error) {
    console.error('Error fetching top reviewers:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching top reviewers',
      error: error.message
    });
  }
});

// POST /api/users - Create or update user
router.post('/', async (req, res) => {
  try {
    const { id, phoneNumber, name, isVerified = false } = req.body;
    
    if (!id || !phoneNumber || !name) {
      return res.status(400).json({
        success: false,
        message: 'ID, phone number, and name are required'
      });
    }
    
    // Check if user already exists
    let user = await User.findOne({ id: id });
    
    if (user) {
      // Update existing user
      user.phoneNumber = phoneNumber;
      user.name = name;
      user.isVerified = isVerified;
      user.lastLoginAt = new Date();
      await user.save();
      
      res.json({
        success: true,
        message: 'User updated successfully',
        data: user
      });
    } else {
      // Create new user with simplified structure
      user = new User({
        id,
        phoneNumber,
        name,
        isVerified,
        points: 0,
        totalReviews: 0
      });
      
      await user.save();
      
      res.status(201).json({
        success: true,
        message: 'User created successfully',
        data: user
      });
    }
  } catch (error) {
    console.error('Error creating/updating user:', error);
    res.status(500).json({
      success: false,
      message: 'Error creating/updating user',
      error: error.message
    });
  }
});

// PUT /api/users/:userId - Update user profile (simplified)
router.put('/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const { name } = req.body;

    const user = await User.findOne({ id: userId, isActive: true });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    if (name) user.name = name.trim();

    await user.save();

    res.json({
      success: true,
      message: 'User updated successfully',
      data: user
    });
  } catch (error) {
    console.error('Error updating user:', error);
    res.status(500).json({
      success: false,
      message: 'Error updating user',
      error: error.message
    });
  }
});

// POST /api/users/:userId/login - Update last login time
router.post('/:userId/login', async (req, res) => {
  try {
    const { userId } = req.params;
    
    const user = await User.findOne({ id: userId, isActive: true });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }
    
    user.lastLoginAt = new Date();
    await user.save();
    
    res.json({
      success: true,
      message: 'Login time updated',
      data: {
        lastLoginAt: user.lastLoginAt
      }
    });
  } catch (error) {
    console.error('Error updating login time:', error);
    res.status(500).json({
      success: false,
      message: 'Error updating login time',
      error: error.message
    });
  }
});

// GET /api/users/:userId/addresses - Get user addresses
router.get('/:userId/addresses', async (req, res) => {
  try {
    const { userId } = req.params;

    const user = await User.findOne({ id: userId });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    // Return all addresses (simplified structure doesn't have isActive)

    res.json({
      success: true,
      data: user.addresses,
      message: 'Addresses retrieved successfully'
    });

  } catch (error) {
    console.error('Error getting user addresses:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// POST /api/users/:userId/addresses - Add new address (simplified)
router.post('/:userId/addresses', async (req, res) => {
  try {
    const { userId } = req.params;
    const { label, phoneNumber, fullAddress, city, notes, isDefault } = req.body;

    // Validate required fields
    if (!label || !phoneNumber || !fullAddress || !city) {
      return res.status(400).json({
        success: false,
        message: 'Label, phone number, full address, and city are required'
      });
    }

    const user = await User.findOne({ id: userId });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    // Simplified address data structure
    const addressData = {
      label: label.trim(),
      phoneNumber: phoneNumber.trim(),
      fullAddress: fullAddress.trim(),
      city: city.trim(),
      notes: notes ? notes.trim() : '',
      isDefault: isDefault || false
    };

    await user.addAddress(addressData);

    // Get the newly added address
    const newAddress = user.addresses[user.addresses.length - 1];

    res.status(201).json({
      success: true,
      message: 'Address added successfully',
      data: newAddress
    });

  } catch (error) {
    console.error('Error adding address:', error);
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// PUT /api/users/:userId/addresses/:addressId - Update address
router.put('/:userId/addresses/:addressId', async (req, res) => {
  try {
    const { userId, addressId } = req.params;
    const updateData = req.body;

    const user = await User.findOne({ id: userId });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    await user.updateAddress(addressId, updateData);

    const updatedAddress = user.addresses.find(addr => addr.id === addressId);

    res.json({
      success: true,
      message: 'Address updated successfully',
      data: updatedAddress
    });

  } catch (error) {
    console.error('Error updating address:', error);
    if (error.message === 'Address not found') {
      return res.status(404).json({
        success: false,
        message: 'Address not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// DELETE /api/users/:userId/addresses/:addressId - Delete address
router.delete('/:userId/addresses/:addressId', async (req, res) => {
  try {
    const { userId, addressId } = req.params;

    const user = await User.findOne({ id: userId });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    await user.deleteAddress(addressId);

    res.json({
      success: true,
      message: 'Address deleted successfully'
    });

  } catch (error) {
    console.error('Error deleting address:', error);
    if (error.message === 'Address not found') {
      return res.status(404).json({
        success: false,
        message: 'Address not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

// PUT /api/users/:userId/addresses/:addressId/default - Set default address
router.put('/:userId/addresses/:addressId/default', async (req, res) => {
  try {
    const { userId, addressId } = req.params;

    const user = await User.findOne({ id: userId });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    await user.setDefaultAddress(addressId);

    res.json({
      success: true,
      message: 'Default address updated successfully'
    });

  } catch (error) {
    console.error('Error setting default address:', error);
    if (error.message === 'Address not found') {
      return res.status(404).json({
        success: false,
        message: 'Address not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Internal server error'
    });
  }
});

module.exports = router;
