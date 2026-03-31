package com.trustengine.verifier.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.trustengine.verifier.R
import com.trustengine.verifier.domain.model.CaptureData
import com.trustengine.verifier.ui.theme.SuccessGreen
import com.trustengine.verifier.ui.theme.TrustEngineAccent
import com.trustengine.verifier.ui.theme.TrustEngineDarkBlue
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onCaptureComplete: (CaptureData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var captureMode by remember { mutableStateOf(CaptureMode.KTP) }
    var ktpPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.camera_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                !hasCameraPermission -> {
                    CameraPermissionDenied(onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    })
                }
                showPreview && previewUri != null -> {
                    PhotoPreview(
                        uri = previewUri!!,
                        onRetake = {
                            showPreview = false
                            previewUri = null
                        },
                        onConfirm = {
                            if (captureMode == CaptureMode.KTP) {
                                ktpPhotoUri = previewUri
                                captureMode = CaptureMode.SELFIE
                                showPreview = false
                                previewUri = null
                            } else {
                                selfieUri = previewUri
                                onCaptureComplete(
                                    CaptureData(
                                        ktpPhotoUri = ktpPhotoUri,
                                        selfieUri = selfieUri
                                    )
                                )
                            }
                        }
                    )
                }
                else -> {
                    CameraCapture(
                        captureMode = captureMode,
                        onPhotoTaken = { uri ->
                            previewUri = uri
                            showPreview = true
                        }
                    )
                    
                    // Overlay instructions
                    CameraOverlay(captureMode = captureMode)
                }
            }
        }
    }
}

@Composable
private fun CameraCapture(
    captureMode: CaptureMode,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    
                    val cameraSelector = if (captureMode == CaptureMode.SELFIE) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Capture Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    val imageCapture = imageCapture ?: return@Button
                    
                    val photoFile = File(
                        context.cacheDir,
                        SimpleDateFormat(
                            "yyyy-MM-dd-HH-mm-ss-SSS",
                            Locale.US
                        ).format(System.currentTimeMillis()) + ".jpg"
                    )
                    
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                exc.printStackTrace()
                            }
                            
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onPhotoTaken(Uri.fromFile(photoFile))
                            }
                        }
                    )
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TrustEngineDarkBlue)
                )
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun CameraOverlay(captureMode: CaptureMode) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top instruction
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TrustEngineDarkBlue.copy(alpha = 0.8f))
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = if (captureMode == CaptureMode.KTP) {
                    stringResource(R.string.camera_ktp_instruction)
                } else {
                    stringResource(R.string.camera_selfie_instruction)
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Frame overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (captureMode == CaptureMode.KTP) {
                // KTP frame (rectangle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.6f)
                        .border(
                            width = 2.dp,
                            color = TrustEngineAccent,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            } else {
                // Selfie frame (oval)
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(
                            width = 2.dp,
                            color = TrustEngineAccent,
                            shape = CircleShape
                        )
                )
            }
        }
        
        // Step indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 140.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicator(
                number = 1,
                label = "KTP",
                isActive = captureMode == CaptureMode.KTP,
                isCompleted = captureMode == CaptureMode.SELFIE
            )
            
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(
                        if (captureMode == CaptureMode.SELFIE) SuccessGreen 
                        else Color.Gray.copy(alpha = 0.3f)
                    )
            )
            
            StepIndicator(
                number = 2,
                label = "Selfie",
                isActive = captureMode == CaptureMode.SELFIE,
                isCompleted = false
            )
        }
    }
}

@Composable
private fun StepIndicator(
    number: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> SuccessGreen
                        isActive -> TrustEngineAccent
                        else -> Color.Gray.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isActive || isCompleted) TrustEngineDarkBlue else Color.Gray,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PhotoPreview(
    uri: Uri,
    onRetake: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Captured photo",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f),
                    contentColor = TrustEngineDarkBlue
                )
            ) {
                Text(
                    stringResource(R.string.retake),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                )
            ) {
                Text(
                    stringResource(R.string.confirm),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionDenied(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TrustEngineDarkBlue
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = TrustEngineDarkBlue
            )
        ) {
            Text(
                "Grant Permission",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

enum class CaptureMode {
    KTP,
    SELFIE
}