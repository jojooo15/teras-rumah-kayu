const mongoose = require('mongoose');

// Simplified user schema focusing on core user data: name, phone, address
const userSchema = new mongoose.Schema({
  id: {
    type: String,
    required: true,
    unique: true
  },
  phoneNumber: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  name: {
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
  // Simplified points system
  points: {
    type: Number,
    default: 0,
    min: 0
  },
  // Review count for basic statistics
  totalReviews: {
    type: Number,
    default: 0,
    min: 0
  },
  // Simplified user addresses - core delivery information
  addresses: [{
    id: {
      type: String,
      required: true
    },
    label: {
      type: String,
      required: true,
      trim: true
    },
    fullAddress: {
      type: String,
      required: true,
      trim: true
    },
    city: {
      type: String,
      required: true,
      trim: true
    },
    phoneNumber: {
      type: String,
      required: true,
      trim: true
    },
    notes: {
      type: String,
      default: '',
      trim: true
    },
    isDefault: {
      type: Boolean,
      default: false
    },
    createdAt: {
      type: Date,
      default: Date.now
    }
  }],
  defaultAddressId: {
    type: String,
    default: null
  },
  
  // Last activity
  lastLoginAt: {
    type: Date,
    default: Date.now
  },
  lastOrderAt: {
    type: Date,
    default: null
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Index for better query performance (phoneNumber already has unique index)
userSchema.index({ points: -1 });
userSchema.index({ totalReviews: -1 });
userSchema.index({ isActive: 1, isVerified: 1 });

// Virtual for formatted phone number
userSchema.virtual('formattedPhoneNumber').get(function() {
  if (this.phoneNumber.startsWith('+62')) {
    return this.phoneNumber.replace('+62', '0');
  }
  return this.phoneNumber;
});

// Virtual for user level based on points
userSchema.virtual('userLevel').get(function() {
  if (this.points >= 1000) return 'GOLD';
  if (this.points >= 500) return 'SILVER';
  if (this.points >= 100) return 'BRONZE';
  return 'BASIC';
});

// Virtual for next level points requirement
userSchema.virtual('nextLevelPoints').get(function() {
  const currentLevel = this.userLevel;
  switch (currentLevel) {
    case 'BASIC': return 100 - this.points;
    case 'BRONZE': return 500 - this.points;
    case 'SILVER': return 1000 - this.points;
    case 'GOLD': return 0;
    default: return 0;
  }
});

// Simplified method to add points
userSchema.methods.addPoints = function(points) {
  this.points += points;
  return this.save();
};

// Simplified method to redeem points
userSchema.methods.redeemPoints = function(points) {
  if (this.points < points) {
    throw new Error('Insufficient points');
  }

  this.points -= points;
  return this.save();
};

// Simplified method to update review count
userSchema.methods.updateReviewStats = function() {
  this.totalReviews += 1;
  return this.save();
};

// Method to add address
userSchema.methods.addAddress = function(addressData) {
  const addressId = require('mongoose').Types.ObjectId().toString();

  const newAddress = {
    id: addressId,
    ...addressData,
    createdAt: new Date(),
    updatedAt: new Date()
  };

  // If this is the first address or marked as default, set as default
  if (this.addresses.length === 0 || addressData.isDefault) {
    // Remove default from other addresses
    this.addresses.forEach(addr => addr.isDefault = false);
    newAddress.isDefault = true;
    this.defaultAddressId = addressId;
  }

  this.addresses.push(newAddress);
  return this.save();
};

// Method to update address
userSchema.methods.updateAddress = function(addressId, updateData) {
  const addressIndex = this.addresses.findIndex(addr => addr.id === addressId);

  if (addressIndex === -1) {
    throw new Error('Address not found');
  }

  // If setting as default, remove default from others
  if (updateData.isDefault) {
    this.addresses.forEach(addr => addr.isDefault = false);
    this.defaultAddressId = addressId;
  }

  // Update the address
  Object.assign(this.addresses[addressIndex], updateData, {
    updatedAt: new Date()
  });

  return this.save();
};

// Method to delete address
userSchema.methods.deleteAddress = function(addressId) {
  const addressIndex = this.addresses.findIndex(addr => addr.id === addressId);

  if (addressIndex === -1) {
    throw new Error('Address not found');
  }

  const wasDefault = this.addresses[addressIndex].isDefault;
  this.addresses.splice(addressIndex, 1);

  // If deleted address was default, set first remaining address as default
  if (wasDefault && this.addresses.length > 0) {
    this.addresses[0].isDefault = true;
    this.defaultAddressId = this.addresses[0].id;
  } else if (this.addresses.length === 0) {
    this.defaultAddressId = null;
  }

  return this.save();
};

// Method to set default address
userSchema.methods.setDefaultAddress = function(addressId) {
  const address = this.addresses.find(addr => addr.id === addressId);

  if (!address) {
    throw new Error('Address not found');
  }

  // Remove default from all addresses
  this.addresses.forEach(addr => addr.isDefault = false);

  // Set new default
  address.isDefault = true;
  this.defaultAddressId = addressId;

  return this.save();
};

// Static method to find user by phone number
userSchema.statics.findByPhoneNumber = function(phoneNumber) {
  return this.findOne({ phoneNumber: phoneNumber, isActive: true });
};

// Pre-save middleware
userSchema.pre('save', function(next) {
  // Update lastLoginAt if this is a login
  if (this.isModified('lastLoginAt')) {
    this.lastLoginAt = new Date();
  }
  
  next();
});

module.exports = mongoose.model('User', userSchema);
