package com.jojo.terasrumah.utils

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

object PhoneNumberValidator {
    
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    
    fun isValidIndonesianPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, "ID")
            phoneNumberUtil.isValidNumber(number) && 
            phoneNumberUtil.getRegionCodeForNumber(number) == "ID"
        } catch (e: NumberParseException) {
            false
        }
    }
    
    fun formatIndonesianPhoneNumber(phoneNumber: String): String {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, "ID")
            phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            phoneNumber
        }
    }
    
    fun isValidPhoneNumberFormat(phoneNumber: String): Boolean {
        // Basic validation for Indonesian phone numbers
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        return when {
            // Format: +62xxxxxxxxxx (10-13 digits after +62)
            cleanNumber.startsWith("+62") -> {
                val digits = cleanNumber.substring(3)
                digits.length in 9..12 && digits.all { it.isDigit() }
            }
            // Format: 62xxxxxxxxxx
            cleanNumber.startsWith("62") -> {
                val digits = cleanNumber.substring(2)
                digits.length in 9..12 && digits.all { it.isDigit() }
            }
            // Format: 08xxxxxxxxxx
            cleanNumber.startsWith("08") -> {
                cleanNumber.length in 11..14 && cleanNumber.all { it.isDigit() }
            }
            // Format: 8xxxxxxxxxx
            cleanNumber.startsWith("8") -> {
                cleanNumber.length in 10..13 && cleanNumber.all { it.isDigit() }
            }
            else -> false
        }
    }
    
    fun normalizePhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        return when {
            cleanNumber.startsWith("+62") -> cleanNumber
            cleanNumber.startsWith("62") -> "+$cleanNumber"
            cleanNumber.startsWith("08") -> "+62${cleanNumber.substring(1)}"
            cleanNumber.startsWith("8") -> "+62$cleanNumber"
            else -> cleanNumber
        }
    }
}
