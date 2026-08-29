package com.modulamobile.ui.update

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modulamobile.updater.UpdateInfo
import com.modulamobile.updater.UpdateState
import com.movtery.zalithlauncher.R
import java.io.File

// Replace with your actual theme/component imports if they differ
// import com.modulamobile.ui.theme.*
// import com.modulamobile.ui.components.*

// Mock components to satisfy the prompt's UI if actual components aren't found in scope
// In a real scenario, use actual FluxCard, FluxButton, etc.

val UpdateInfo.displaySizeBytes: Long
    @androidx.compose.runtime.Composable
    get() {
        val context = androidx.compose.ui.platform.LocalContext.current
        val payload = remember(this) {
            com.modulamobile.updater.PayloadSelector.selectPayload(context, this)
        }
        return payload.sizeBytes
    }

@Composable
fun UpdateBanner(
    info: UpdateInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCC9900))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Update Available",
                        fontSize = 18.sp,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        "v${info.versionName} • ${"%.1f".format(info.displaySizeBytes / 1024f / 1024f)}MB",
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {

                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Divider()
                Spacer(Modifier.height(8.dp))
                info.releaseNotes.forEach { note ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = Color(0xFFFFD700))
                        Text(note, color = Color(0xFFF2F2F2))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                androidx.compose.material3.Button(
                    onClick = onUpdate,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF060609))
                ) { Text("UPDATE NOW") }
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD700))
                ) { Text("LATER") }
            }
        }
    }
}

@Composable
fun UpdateProgressSheet(
    state: UpdateState,
    info: UpdateInfo,
    onInstall: (File) -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF161626)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFD700))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Modula Mobile ${info.versionName}",
                        fontSize = 18.sp,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        "${"%.1f".format(info.displaySizeBytes / 1024f / 1024f)}MB update",
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (state) {
                is UpdateState.Downloading -> {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFD700)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${"%.1f".format(state.downloadedMb)}MB / ${"%.1f".format(state.totalMb)}MB", color = Color(0xFFAAAAAA))
                        Text("${"%.1f".format(state.speedMbps)} MB/s", color = Color(0xFFFFD700))
                    }
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF060609))
                    ) { Text("CANCEL") }
                }

                is UpdateState.Installing -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Processing update...", color = Color(0xFFFFD700))
                    }
                }

                is UpdateState.ReadyToInstall -> {
                    Text("✅ Download complete! Ready to install.", color = Color(0xFF22C55E))
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = { onInstall(state.apkFile) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF060609))
                    ) { Text("⚡ INSTALL NOW") }
                }

                is UpdateState.Failed -> {
                    Text("❌ ${state.message}", color = Color(0xFFEF4444))
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF060609))
                    ) { Text("TRY AGAIN") }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun MandatoryUpdateScreen(
    info: UpdateInfo,
    state: UpdateState,
    onUpdate: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF080810)), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val pulse by rememberInfiniteTransition().animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))
            Image(
                painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(100.dp).scale(pulse).border(2.dp, Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFB300))), CircleShape)
            )

            Spacer(Modifier.height(24.dp))
            Text("UPDATE REQUIRED", fontSize = 24.sp, color = Color(0xFFFFD700))
            Spacer(Modifier.height(8.dp))
            Text("Version ${info.versionName} is required to continue.", color = Color(0xFFAAAAAA), textAlign = TextAlign.Center)

            Spacer(Modifier.height(20.dp))
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCC9900))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("WHAT'S NEW", color = Color(0xFFFFD700))
                    Spacer(Modifier.height(8.dp))
                    info.releaseNotes.forEach { Text("• $it", color = Color(0xFFF2F2F2)) }
                }
            }

            Spacer(Modifier.height(20.dp))
            UpdateProgressSheet(state = state, info = info, onInstall = { }, onCancel = { }, onRetry = onUpdate)

            if (state is UpdateState.Available) {
                androidx.compose.material3.Button(
                    onClick = onUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF060609))
                ) {
                    Text("UPDATE NOW — ${"%.1f".format(info.displaySizeBytes / 1024f / 1024f)}MB")
                }
            }
        }
    }
}
