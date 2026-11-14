package com.jojo.terasrumah.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jojo.terasrumah.R
import com.jojo.terasrumah.ui.theme.PrimaryBrown
import com.jojo.terasrumah.ui.theme.BackgroundCream
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = true) {
        // Animate logo scale
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        
        // Animate text alpha
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        
        // Wait for 2 seconds then navigate
        delay(2000)
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Rumah Kayu
            Image(
                painter = painterResource(id = R.drawable.ic_wooden_house_logo),
                contentDescription = "Logo Teras Rumah Kayu",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "Teras Rumah Kayu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Cita Rasa Tradisional\nDalam Setiap Hidangan",
                fontSize = 16.sp,
                color = PrimaryBrown.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Loading indicator (simple dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(alpha.value)
            ) {
                repeat(3) { index ->
                    val dotAlpha = remember { Animatable(0.3f) }
                    
                    LaunchedEffect(key1 = true) {
                        delay(index * 200L)
                        while (true) {
                            dotAlpha.animateTo(1f, tween(600))
                            dotAlpha.animateTo(0.3f, tween(600))
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                PrimaryBrown.copy(alpha = dotAlpha.value),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
        
        // Footer text
        Text(
            text = "Selamat Datang",
            fontSize = 14.sp,
            color = PrimaryBrown.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .alpha(alpha.value)
        )
    }
}
