package com.jojo.terasrumah.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.jojo.terasrumah.data.CartItem
import com.jojo.terasrumah.data.CartRepository
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object CheckoutUtils {

    fun openWhatsAppForTransfer(context: Context, deliveryAddress: String? = null, customerPhone: String? = null) {
        val cartItems = CartRepository.cartItems.value
        val totalPrice = CartRepository.getTotalPrice()

        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = buildWhatsAppMessage(cartItems, totalPrice, deliveryAddress, customerPhone)
        val phoneNumber = "6282125116950" // Ganti dengan nomor WhatsApp toko
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback jika WhatsApp tidak terinstall
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun generateCODReceipt(context: Context, deliveryAddress: String? = null, customerPhone: String? = null, customerName: String? = null) {
        val cartItems = CartRepository.cartItems.value
        val totalPrice = CartRepository.getTotalPrice()

        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Check storage permissions
        if (!PermissionUtils.hasStoragePermissions(context)) {
            Toast.makeText(context, "Memerlukan izin akses penyimpanan untuk menyimpan struk", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Generate image receipt
            val success = ReceiptImageGenerator.generateReceiptImage(
                context = context,
                cartItems = cartItems,
                totalPrice = totalPrice,
                customerName = customerName ?: "",
                customerPhone = customerPhone ?: "",
                deliveryAddress = deliveryAddress ?: ""
            )

            if (success) {
                // Clear cart after successful order
                CartRepository.clearCart()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuat struk: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun buildWhatsAppMessage(cartItems: List<CartItem>, totalPrice: Int, deliveryAddress: String? = null, customerPhone: String? = null): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        
        val message = StringBuilder()
        message.append("🍽️ *PESANAN TERAS RUMAH KAYU* 🍽️\n\n")
        message.append("📅 Tanggal: $currentDate\n")
        message.append("💳 Metode: Transfer Bank\n\n")
        message.append("📋 *DETAIL PESANAN:*\n")
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        
        cartItems.forEach { item ->
            val itemTotal = item.foodItem.price * item.quantity
            message.append("• ${item.foodItem.name}\n")
            message.append("  ${formatPrice(item.foodItem.price)} x ${item.quantity} = ${formatPrice(itemTotal)}\n")
            if (item.specialNotes.isNotBlank()) {
                message.append("  📝 Catatan: ${item.specialNotes}\n")
            }
            message.append("\n")
        }
        
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        message.append("💰 *TOTAL: ${formatPrice(totalPrice)}*\n\n")
        message.append("📍 Alamat pengiriman:\n")
        if (deliveryAddress != null) {
            message.append("$deliveryAddress\n\n")
        } else {
            message.append("(Mohon isi alamat lengkap)\n\n")
        }
        message.append("📞 No. HP:\n")
        if (customerPhone != null) {
            message.append("$customerPhone\n\n")
        } else {
            message.append("(Mohon isi nomor HP)\n\n")
        }
        message.append("💳 *REKENING TRANSFER:*\n")
        message.append("BCA: 8330236842\n")
        message.append("a.n. Teras Rumah Kayu\n\n")
        message.append("Terima kasih telah memesan! 🙏")
        
        return message.toString()
    }
    
    private fun buildReceiptContent(cartItems: List<CartItem>, totalPrice: Int): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        val orderNumber = "COD${System.currentTimeMillis().toString().takeLast(6)}"
        
        val receipt = StringBuilder()
        receipt.append("===============================================\n")
        receipt.append("           TERAS RUMAH KAYU\n")
        receipt.append("        Jl. Contoh No. 123, Jakarta\n")
        receipt.append("         Telp: (021) 1234-5678\n")
        receipt.append("===============================================\n\n")
        receipt.append("No. Pesanan: $orderNumber\n")
        receipt.append("Tanggal    : $currentDate\n")
        receipt.append("Metode     : Cash on Delivery (COD)\n")
        receipt.append("Status     : Menunggu Konfirmasi\n\n")
        receipt.append("-----------------------------------------------\n")
        receipt.append("DETAIL PESANAN:\n")
        receipt.append("-----------------------------------------------\n")
        
        cartItems.forEach { item ->
            val itemTotal = item.foodItem.price * item.quantity
            receipt.append("${item.foodItem.name}\n")
            receipt.append("${formatPrice(item.foodItem.price)} x ${item.quantity} = ${formatPrice(itemTotal)}\n")
            if (item.specialNotes.isNotBlank()) {
                receipt.append("Catatan: ${item.specialNotes}\n")
            }
            receipt.append("\n")
        }
        
        receipt.append("-----------------------------------------------\n")
        receipt.append("TOTAL PEMBAYARAN: ${formatPrice(totalPrice)}\n")
        receipt.append("-----------------------------------------------\n\n")
        receipt.append("CATATAN:\n")
        receipt.append("- Pembayaran dilakukan saat pengiriman\n")
        receipt.append("- Mohon siapkan uang pas\n")
        receipt.append("- Estimasi pengiriman: 30-60 menit\n")
        receipt.append("- Hubungi kami jika ada pertanyaan\n\n")
        receipt.append("Terima kasih atas pesanan Anda!\n")
        receipt.append("===============================================\n")
        
        return receipt.toString()
    }
    
    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(price).replace("IDR", "Rp")
    }
}
