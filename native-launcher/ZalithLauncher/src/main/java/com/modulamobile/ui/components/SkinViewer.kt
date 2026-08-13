package com.modulamobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.movtery.zalithlauncher.setting.AllSettings

@Composable
fun SkinViewer(
    username: String,
    uuid: String? = null,
    modifier: Modifier = Modifier
) {
    val customSkinUrl = AllSettings.customSkinUrl.state
    
    // Construct the projection URL
    // If there's a custom skin URL, we can't project it via mc-heads automatically since mc-heads requires it to be on Mojang's servers.
    // However, if we just want to display the custom skin texture for now, or fallback to default steve body.
    // Wait, mc-heads body projection only works for actual usernames/UUIDs!
    // So if there's a custom skin URL, we just show the raw skin for now or use the default projection.
    
    val imageUrl = "https://mc-heads.net/body/${uuid ?: username}/280"

    Box(
        modifier = modifier
            .background(Color(0xFF0C0C16))
            .border(0.8.dp, Color(0x59FFD700), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "3D Skin Projection",
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentScale = ContentScale.Fit,
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            error = {
                // Fallback to steve
                AsyncImage(
                    model = "https://mc-heads.net/body/steve/280",
                    contentDescription = "Default Steve",
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        )

        // Show username watermark
        Text(
            text = username,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0x66FFFFFF)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

