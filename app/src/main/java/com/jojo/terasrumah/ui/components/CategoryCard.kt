package com.jojo.terasrumah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jojo.terasrumah.data.FoodCategory
import com.jojo.terasrumah.ui.theme.PrimaryBrown

@Composable
fun CategoryCard(
    category: FoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                PrimaryBrown,
                PrimaryBrown.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White,
                Color.White
            )
        )
    }
    
    val textColor = if (isSelected) Color.White else PrimaryBrown
    val borderColor = if (isSelected) PrimaryBrown else PrimaryBrown.copy(alpha = 0.3f)
    
    Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(25.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.displayName,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedCategoryChip(
    category: FoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) PrimaryBrown else Color.White
    val contentColor = if (isSelected) Color.White else PrimaryBrown
    val borderColor = PrimaryBrown
    
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category.displayName,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        },
        selected = isSelected,
        modifier = modifier.padding(horizontal = 4.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = containerColor,
            containerColor = Color.White,
            selectedLabelColor = contentColor,
            labelColor = contentColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = borderColor,
            selectedBorderColor = borderColor,
            borderWidth = 1.5.dp,
            enabled = true,
            selected = isSelected
        ),
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = if (isSelected) 6.dp else 2.dp
        )
    )
}
