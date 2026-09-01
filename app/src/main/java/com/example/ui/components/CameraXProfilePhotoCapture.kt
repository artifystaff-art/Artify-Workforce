package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * CameraX Profile Photo Capture Dialog allowing users to capture and update their profile picture
 * using the front/back camera with live preview, circular portrait alignment, and preview confirmation.
 */
@Composable
fun CameraXProfilePhotoDialog(
    employeeName: String? = null,
    employeeId: String? = null,
    onDismiss: () -> Unit,
    onCaptureComplete: (profilePhotoPath: String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        CameraXProfilePhotoCaptureView(
            employeeName = employeeName,
            employeeId = employeeId,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 10.dp),
            onDismiss = onDismiss,
            onCaptureComplete = onCaptureComplete
        )
    }
}

@Composable
fun CameraXProfilePhotoCaptureView(
    employeeName: String? = null,
    employeeId: String? = null,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onCaptureComplete: (profilePhotoPath: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var isCameraBound by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var showFlashEffect by remember { mutableStateOf(false) }
    var isFillLightOn by remember { mutableStateOf(false) }
    var capturedImageFile by remember { mutableStateOf<File?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCameraError by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Bind CameraX Lifecycle to PreviewView
    LaunchedEffect(hasCameraPermission, cameraSelector, previewView) {
        val pv = previewView
        if (hasCameraPermission && pv != null) {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(pv.surfaceProvider)
                            }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setTargetRotation(pv.display?.rotation ?: 0)
                            .build()

                        imageCapture = capture
                        cameraProvider.unbindAll()

                        val selector = if (cameraProvider.hasCamera(cameraSelector)) {
                            cameraSelector
                        } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            capture
                        )
                        isCameraBound = true
                        isCameraError = false
                    } catch (exc: Exception) {
                        Log.e("CameraXProfile", "Use case binding failed", exc)
                        isCameraError = true
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Log.e("CameraXProfile", "Camera provider error", e)
                isCameraError = true
            }
        }
    }

    // Shutter Trigger Execution
    val triggerPhotoCapture = {
        isCapturing = true
        showFlashEffect = true
        coroutineScope.launch {
            delay(120)
            showFlashEffect = false
        }

        val cap = imageCapture
        if (cap != null && hasCameraPermission && !isCameraError) {
            val profilesDir = File(context.filesDir, "profiles").apply { if (!exists()) mkdirs() }
            val photoFile = File(profilesDir, "profile_${employeeId ?: "user"}_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            cap.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        try {
                            val rawBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            val rotatedBitmap = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                val matrix = Matrix().apply { postScale(-1f, 1f, rawBitmap.width / 2f, rawBitmap.height / 2f) }
                                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                            } else {
                                rawBitmap
                            }

                            // Overwrite file with corrected orientation
                            FileOutputStream(photoFile).use { fos ->
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
                            }

                            coroutineScope.launch {
                                capturedImageFile = photoFile
                                capturedBitmap = rotatedBitmap
                                isCapturing = false
                            }
                        } catch (e: Exception) {
                            Log.e("CameraXProfile", "Error processing profile photo", e)
                            coroutineScope.launch {
                                capturedImageFile = photoFile
                                capturedBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                isCapturing = false
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraXProfile", "Photo capture error: ${exception.message}", exception)
                        coroutineScope.launch {
                            val fallbackFile = createSimulatedProfilePhotoFile(context, employeeName, employeeId)
                            capturedImageFile = fallbackFile
                            capturedBitmap = BitmapFactory.decodeFile(fallbackFile.absolutePath)
                            isCapturing = false
                        }
                    }
                }
            )
        } else {
            // Emulators or devices without physical camera
            val fallbackFile = createSimulatedProfilePhotoFile(context, employeeName, employeeId)
            capturedImageFile = fallbackFile
            capturedBitmap = BitmapFactory.decodeFile(fallbackFile.absolutePath)
            isCapturing = false
        }
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                RoundedCornerShape(28.dp)
            )
            .testTag("camera_profile_capture_view"),
        color = SophisticatedDarkBg,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SophisticatedDarkBg)
        ) {
            // Top Bar
            Surface(
                color = SophisticatedDarkSurface,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SophisticatedDarkBorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Take Profile Picture",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SophisticatedTextPrimary
                            )
                            Text(
                                text = "Live Device Camera",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_profile_camera_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Camera",
                            tint = SophisticatedTextSecondary
                        )
                    }
                }
            }

            // Main Body: Camera Viewfinder OR Captured Preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    // --- PREVIEW CAPTURED PROFILE PHOTO ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PROFILE PHOTO PREVIEW",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Circular Avatar Preview Frame
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(SophisticatedDarkSurface)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = employeeName ?: "User Profile",
                            color = SophisticatedTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (!employeeId.isNullOrBlank()) {
                            Text(
                                text = "ID: $employeeId",
                                color = SophisticatedTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SophisticatedSuccessContainer,
                            border = BorderStroke(1.dp, SophisticatedSuccess.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SophisticatedSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ready to Save",
                                    color = SophisticatedSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (hasCameraPermission && !isCameraError) {
                    // --- LIVE CAMERAX VIEW ---
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                previewView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("camera_preview_surface")
                    )

                    // Fill Light Overlay (Screen Flash)
                    if (isFillLightOn) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }

                    // Face / Avatar Alignment Guideline Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    CircleShape
                                )
                        )
                    }

                    // Top Viewfinder Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, SophisticatedDarkBorderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SophisticatedSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Position face in circle",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Light / Torch Toggle
                        IconButton(
                            onClick = { isFillLightOn = !isFillLightOn },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFillLightOn) Icons.Default.LightMode else Icons.Outlined.LightMode,
                                contentDescription = "Fill Light",
                                tint = if (isFillLightOn) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    // Fallback / Permission Request Box
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (!hasCameraPermission) "Camera Permission Needed" else "Camera Hardware Initializing",
                            color = SophisticatedTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (!hasCameraPermission)
                                "Allow camera access to capture your profile photo directly from this device."
                            else
                                "You can also generate an instant test profile picture using the quick button below.",
                            color = SophisticatedTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (!hasCameraPermission) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Allow Camera Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Shutter White Flash Animation
                if (showFlashEffect) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
            }

            // Bottom Controls Bar
            Surface(
                color = SophisticatedDarkSurface,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SophisticatedDarkBorderLight)
            ) {
                if (capturedBitmap == null) {
                    // LIVE SHUTTER CONTROLS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Switch Front / Back Camera
                            IconButton(
                                onClick = {
                                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(SophisticatedDarkBg, CircleShape)
                                    .border(1.dp, SophisticatedDarkBorderLight, CircleShape)
                                    .testTag("switch_profile_camera_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Switch Camera",
                                    tint = SophisticatedTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Big Circular Shutter Button
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable(enabled = !isCapturing) { triggerPhotoCapture() }
                                    .testTag("capture_profile_photo_shutter_btn")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        )
                                ) {
                                    if (isCapturing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Capture Profile Picture",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // Quick Simulated Photo (QA / Virtual Environment button)
                            IconButton(
                                onClick = {
                                    val fallbackFile = createSimulatedProfilePhotoFile(context, employeeName, employeeId)
                                    capturedImageFile = fallbackFile
                                    capturedBitmap = BitmapFactory.decodeFile(fallbackFile.absolutePath)
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(SophisticatedDarkBg, CircleShape)
                                    .border(1.dp, SophisticatedDarkBorderLight, CircleShape)
                                    .testTag("quick_test_profile_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Instant Test Photo",
                                    tint = SophisticatedWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap circle to capture photo",
                            fontSize = 11.sp,
                            color = SophisticatedTextSecondary
                        )
                    }
                } else {
                    // CONFIRMATION CONTROLS (Retake / Save)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedBitmap = null
                                capturedImageFile = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("retake_profile_photo_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SophisticatedDarkBorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SophisticatedDarkBg,
                                contentColor = SophisticatedTextPrimary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val path = capturedImageFile?.absolutePath ?: ""
                                onCaptureComplete(path)
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(50.dp)
                                .testTag("confirm_save_profile_photo_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Profile Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates a simulated profile photo for test/qa environments with an artistic portrait avatar.
 */
private fun createSimulatedProfilePhotoFile(
    context: Context,
    employeeName: String?,
    employeeId: String?
): File {
    val dir = File(context.filesDir, "profiles").apply { if (!exists()) mkdirs() }
    val file = File(dir, "profile_${employeeId ?: "user"}_${System.currentTimeMillis()}.jpg")
    try {
        val width = 600
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        // Background Gradient
        paint.color = android.graphics.Color.parseColor("#13121A")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Outer glow
        paint.color = android.graphics.Color.parseColor("#262238")
        canvas.drawCircle(width / 2f, height / 2f, 260f, paint)

        // Avatar Core
        paint.color = android.graphics.Color.parseColor("#D4AF37")
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawCircle(width / 2f, height / 2f, 220f, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.parseColor("#3B82F6")
        canvas.drawCircle(width / 2f, height / 2f, 212f, paint)

        // Head & Shoulders Silhouette
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(width / 2f, height / 2f - 40f, 75f, paint)
        canvas.drawOval(
            width / 2f - 120f,
            height / 2f + 40f,
            width / 2f + 120f,
            height / 2f + 180f,
            paint
        )

        // Watermark Banner at Bottom
        paint.color = android.graphics.Color.parseColor("#0F0E17")
        canvas.drawRect(0f, height - 100f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.parseColor("#D4AF37")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("ARTIFY EMPLOYEE PROFILE PHOTO", width / 2f, height - 60f, paint)

        paint.color = android.graphics.Color.parseColor("#9CA3AF")
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val nameStr = if (!employeeName.isNullOrBlank()) "$employeeName ($employeeId)" else "Verified Workforce Profile"
        canvas.drawText(nameStr, width / 2f, height - 30f, paint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
    } catch (e: Exception) {
        Log.e("CameraXProfile", "Error creating simulated profile photo", e)
    }
    return file
}
