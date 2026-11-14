package com.jojo.terasrumah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jojo.terasrumah.ui.theme.PrimaryBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewDialog(
    foodItemName: String,
    rating: Int,
    comment: String,
    isSubmitting: Boolean = false,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Tulis Review",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Food Item Name
                Text(
                    text = foodItemName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Rating Section
                Text(
                    text = "Berikan Rating",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Star Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $starIndex",
                            tint = if (starIndex <= rating) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onRatingChange(starIndex) }
                                .padding(4.dp)
                        )
                    }
                }
                
                // Rating Text
                if (rating > 0) {
                    Text(
                        text = getRatingText(rating),
                        fontSize = 14.sp,
                        color = PrimaryBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Comment Section
                Text(
                    text = "Tulis Komentar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBrown
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    placeholder = { 
                        Text(
                            text = "Bagikan pengalaman Anda tentang makanan ini...",
                            color = Color.Gray.copy(alpha = 0.7f)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBrown,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        cursorColor = PrimaryBrown
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 5
                )
                
                // Character count
                Text(
                    text = "${comment.length}/500",
                    fontSize = 12.sp,
                    color = if (comment.length > 500) Color.Red else Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBrown
                        ),
                        enabled = !isSubmitting
                    ) {
                        Text("Batal")
                    }
                    
                    // Submit Button
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBrown,
                            contentColor = Color.White
                        ),
                        enabled = !isSubmitting && rating > 0 && comment.isNotBlank() && comment.length <= 500
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Kirim Review")
                        }
                    }
                }
                
                // Points Info
                if (rating > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryBrown.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dapatkan 10 poin untuk review ini!",
                                fontSize = 12.sp,
                                color = PrimaryBrown,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getRatingText(rating: Int): String {
    return when (rating) {
        1 -> "Sangat Tidak Puas"
        2 -> "Tidak Puas"
        3 -> "Biasa Saja"
        4 -> "Puas"
        5 -> "Sangat Puas"
        else -> ""
    }
}

@Composable
fun LoadMoreButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryBrown
        )
    ) {
        Text(
            text = "Muat Lebih Banyak",
            fontSize = 14.sp
        )
    }
}
