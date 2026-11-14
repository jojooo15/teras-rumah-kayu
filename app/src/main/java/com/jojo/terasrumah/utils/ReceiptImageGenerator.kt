package com.jojo.terasrumah.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.jojo.terasrumah.data.CartItem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ReceiptImageGenerator {

    private const val RECEIPT_WIDTH = 800
    private const val RECEIPT_PADDING = 40
    private const val LINE_HEIGHT = 50
    private const val TITLE_SIZE = 32f
    private const val HEADER_SIZE = 24f
    private const val BODY_SIZE = 20f
    private const val SMALL_SIZE = 16f

    fun generateReceiptImage(
        context: Context,
        cartItems: List<CartItem>,
        totalPrice: Int,
        customerName: String = "",
        customerPhone: String = "",
        deliveryAddress: String = ""
    ): Boolean {
        return try {
            val bitmap = createReceiptBitmap(cartItems, totalPrice, customerName, customerPhone, deliveryAddress)
            saveImageToGallery(context, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuat struk: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun createReceiptBitmap(
        cartItems: List<CartItem>,
        totalPrice: Int,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String
    ): Bitmap {
        // Calculate dynamic height based on content
        val baseHeight = 400 // Header + footer space
        val itemsHeight = cartItems.size * LINE_HEIGHT * 2 // Each item takes 2 lines
        val addressHeight = if (deliveryAddress.isNotEmpty()) deliveryAddress.split("\n").size * LINE_HEIGHT else 0
        val totalHeight = baseHeight + itemsHeight + addressHeight + 200 // Extra padding

        val bitmap = Bitmap.createBitmap(RECEIPT_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        // Paint objects
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = TITLE_SIZE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = HEADER_SIZE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = BODY_SIZE
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            color = Color.GRAY
            textSize = SMALL_SIZE
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        var currentY = RECEIPT_PADDING.toFloat()

        // Header - Restaurant Name
        canvas.drawText("TERAS RUMAH KAYU", RECEIPT_WIDTH / 2f, currentY + TITLE_SIZE, titlePaint)
        currentY += LINE_HEIGHT + 10

        // Separator line
        canvas.drawLine(
            RECEIPT_PADDING.toFloat(),
            currentY,
            (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat(),
            currentY,
            linePaint
        )
        currentY += 20

        // Order Info
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        val currentDate = dateFormat.format(Date())
        val orderNumber = "COD${System.currentTimeMillis().toString().takeLast(6)}"

        canvas.drawText("No. Pesanan: $orderNumber", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
        currentY += LINE_HEIGHT

        canvas.drawText("Tanggal: $currentDate", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
        currentY += LINE_HEIGHT

        canvas.drawText("Metode: Cash on Delivery (COD)", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
        currentY += LINE_HEIGHT + 20

        // Customer Info
        if (customerName.isNotEmpty()) {
            canvas.drawText("Nama: $customerName", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
            currentY += LINE_HEIGHT
        }

        if (customerPhone.isNotEmpty()) {
            canvas.drawText("No. HP: $customerPhone", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
            currentY += LINE_HEIGHT
        }

        // Delivery Address
        if (deliveryAddress.isNotEmpty()) {
            canvas.drawText("Alamat Pengiriman:", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
            currentY += LINE_HEIGHT

            val addressLines = deliveryAddress.split("\n")
            for (line in addressLines) {
                if (line.trim().isNotEmpty()) {
                    canvas.drawText(line.trim(), RECEIPT_PADDING.toFloat(), currentY + SMALL_SIZE, smallPaint)
                    currentY += LINE_HEIGHT - 10
                }
            }
        }

        currentY += 20

        // Separator line
        canvas.drawLine(
            RECEIPT_PADDING.toFloat(),
            currentY,
            (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat(),
            currentY,
            linePaint
        )
        currentY += 30

        // Items Header
        canvas.drawText("DETAIL PESANAN", RECEIPT_PADDING.toFloat(), currentY + HEADER_SIZE, headerPaint)
        currentY += LINE_HEIGHT + 10

        // Items
        for (item in cartItems) {
            // Item name and quantity
            val itemText = "${item.quantity}x ${item.foodItem.name}"
            canvas.drawText(itemText, RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
            
            // Item price (right aligned)
            val itemTotal = item.quantity * item.foodItem.price
            val priceText = formatPrice(itemTotal)
            val priceWidth = bodyPaint.measureText(priceText)
            canvas.drawText(
                priceText,
                (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat() - priceWidth,
                currentY + BODY_SIZE,
                bodyPaint
            )
            currentY += LINE_HEIGHT

            // Item price per unit (smaller text)
            val unitPriceText = "@${formatPrice(item.foodItem.price)}"
            canvas.drawText(unitPriceText, RECEIPT_PADDING.toFloat() + 20, currentY + SMALL_SIZE, smallPaint)
            currentY += LINE_HEIGHT
        }

        currentY += 10

        // Separator line
        canvas.drawLine(
            RECEIPT_PADDING.toFloat(),
            currentY,
            (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat(),
            currentY,
            linePaint
        )
        currentY += 30

        // Total
        canvas.drawText("TOTAL PEMBAYARAN", RECEIPT_PADDING.toFloat(), currentY + HEADER_SIZE, headerPaint)
        val totalText = formatPrice(totalPrice)
        val totalWidth = headerPaint.measureText(totalText)
        canvas.drawText(
            totalText,
            (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat() - totalWidth,
            currentY + HEADER_SIZE,
            headerPaint
        )
        currentY += LINE_HEIGHT + 20

        // Separator line
        canvas.drawLine(
            RECEIPT_PADDING.toFloat(),
            currentY,
            (RECEIPT_WIDTH - RECEIPT_PADDING).toFloat(),
            currentY,
            linePaint
        )
        currentY += 30

        // Notes
        canvas.drawText("CATATAN:", RECEIPT_PADDING.toFloat(), currentY + BODY_SIZE, bodyPaint)
        currentY += LINE_HEIGHT

        val notes = listOf(
            "• Pembayaran dilakukan saat pengiriman",
            "• Mohon siapkan uang pas",
            "• Estimasi pengiriman: 30-60 menit",
            "• Hubungi kami jika ada pertanyaan"
        )

        for (note in notes) {
            canvas.drawText(note, RECEIPT_PADDING.toFloat(), currentY + SMALL_SIZE, smallPaint)
            currentY += LINE_HEIGHT - 10
        }

        currentY += 20

        // Footer
        canvas.drawText("Terima kasih atas pesanan Anda!", RECEIPT_WIDTH / 2f, currentY + BODY_SIZE, titlePaint.apply { 
            textAlign = Paint.Align.CENTER 
            textSize = BODY_SIZE
        })

        return bitmap
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "struk_cod_${System.currentTimeMillis()}.jpg"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToGalleryQ(context, bitmap, filename)
        } else {
            saveImageToGalleryLegacy(context, bitmap, filename)
        }
    }

    private fun saveImageToGalleryQ(context: Context, bitmap: Bitmap, filename: String): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TerasRumahKayu")
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                Toast.makeText(context, "Struk disimpan di Galeri/TerasRumahKayu/$filename", Toast.LENGTH_LONG).show()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveImageToGalleryLegacy(context: Context, bitmap: Bitmap, filename: String): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "TerasRumahKayu")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            val file = File(appDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            // Add to media store
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                file.absolutePath,
                filename,
                "Struk COD Teras Rumah Kayu"
            )

            Toast.makeText(context, "Struk disimpan di Galeri/TerasRumahKayu/$filename", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun formatPrice(price: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(price).replace("IDR", "Rp")
    }
}
