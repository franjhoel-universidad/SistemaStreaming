package com.example.streamingiot

import android.Manifest
import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.streamingiot.streaming.StreamConfig
import com.example.streamingiot.streaming.StreamManager
import com.example.streamingiot.streaming.StreamState
import com.example.streamingiot.ui.theme.StreamingIoTTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamingIoTTheme {
                StreamingScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StreamingScreen() {
    val context = LocalContext.current
    val streamManager = remember { StreamManager(context) }

    val streamState by streamManager.streamState.collectAsState()
    val errorMessage by streamManager.errorMessage.collectAsState()

    var rtmpUrl by remember { mutableStateOf("") }
    var streamKey by remember { mutableStateOf("stream") }
    var selectedQuality by remember { mutableStateOf("MEDIUM") }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )

    val streamConfig = remember(selectedQuality) {
        when (selectedQuality) {
            "LOW" -> StreamConfig.LOW_QUALITY
            "MEDIUM" -> StreamConfig.MEDIUM_QUALITY
            else -> StreamConfig.MEDIUM_QUALITY
        }
    }

    DisposableEffect(Unit) {
        onDispose { streamManager.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!permissionsState.allPermissionsGranted) {
            PermissionRequestScreen { permissionsState.launchMultiplePermissionRequest() }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    CameraPreview(streamManager = streamManager, streamConfig = streamConfig)
                }

                StreamingControls(
                    streamState = streamState,
                    rtmpUrl = rtmpUrl,
                    onRtmpUrlChange = { rtmpUrl = it },
                    streamKey = streamKey,
                    onStreamKeyChange = { streamKey = it },
                    selectedQuality = selectedQuality,
                    onQualityChange = { selectedQuality = it },
                    onStartStop = {
                        if (streamManager.isStreaming()) {
                            streamManager.stopStreaming()
                        } else {
                            streamManager.configure(streamConfig)
                            streamManager.startStreaming(rtmpUrl, streamKey)
                        }
                    }
                )
            }
        }

        errorMessage?.let { error ->
            ErrorSnackbar(error, { streamManager.clearError() }, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun CameraPreview(streamManager: StreamManager, streamConfig: StreamConfig) {
    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(object : android.view.SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                        streamManager.initialize(this@apply)
                        streamManager.configure(streamConfig)
                        streamManager.startPreview()
                    }
                    override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {}
                    override fun surfaceDestroyed(holder: android.view.SurfaceHolder) { streamManager.stopPreview() }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun StreamingControls(
    streamState: StreamState,
    rtmpUrl: String,
    onRtmpUrlChange: (String) -> Unit,
    streamKey: String,
    onStreamKeyChange: (String) -> Unit,
    selectedQuality: String,
    onQualityChange: (String) -> Unit,
    onStartStop: () -> Unit
) {
    val isStreaming = streamState == StreamState.STREAMING

    Column(
        modifier = Modifier.fillMaxWidth().background(Color.DarkGray.copy(alpha = 0.9f)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = rtmpUrl,
            onValueChange = onRtmpUrlChange,
            label = { Text("URL RTMP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = streamKey,
            onValueChange = onStreamKeyChange,
            label = { Text("Stream Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.Gray
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("LOW" to "480p", "MEDIUM" to "720p").forEach { (key, label) ->
                FilterChip(
                    selected = selectedQuality == key,
                    onClick = { onQualityChange(key) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Cyan,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.Gray.copy(alpha = 0.3f),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onStartStop,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isStreaming) Color.Red else Color.Green),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = when (streamState) {
                    StreamState.STREAMING -> "Detener Streaming"
                    StreamState.PREPARING -> "Conectando..."
                    else -> "Iniciar Streaming"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = when (streamState) {
                StreamState.IDLE -> "Listo para transmitir"
                StreamState.PREPARING -> "Conectando..."
                StreamState.STREAMING -> "Transmitiendo en vivo"
                StreamState.STOPPED -> "Transmisión detenida"
                StreamState.ERROR -> "Error de conexión"
            },
            color = when (streamState) {
                StreamState.STREAMING -> Color.Green
                StreamState.ERROR -> Color.Red
                else -> Color.White
            },
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermissions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permisos Requeridos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Para transmitir video en vivo, necesitamos acceso a tu cámara y micrófono.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Conceder Permisos", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ErrorSnackbar(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = message, color = Color.White, modifier = Modifier.weight(1f))
            Text("✕", color = Color.White, fontSize = 18.sp, modifier = Modifier.clickable { onDismiss() })
        }
    }
}