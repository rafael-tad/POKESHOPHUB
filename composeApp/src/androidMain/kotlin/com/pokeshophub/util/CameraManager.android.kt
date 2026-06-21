package com.pokeshophub.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
        
    val scanner = remember { GmsDocumentScanning.getClient(options) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val gmsResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val jpegUri = gmsResult?.pages?.firstOrNull()?.imageUri
            if (jpegUri != null) {
                scope.launch {
                    val bytes = withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(jpegUri)?.use { it.readBytes() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    onResult(bytes)
                }
            } else {
                onResult(null)
            }
        } else {
            onResult(null)
        }
    }

    return remember {
        object : CameraLauncher {
            override fun launch() {
                val activity = context.findActivity()
                if (activity != null) {
                    scanner.getStartScanIntent(activity)
                        .addOnSuccessListener { intentSender ->
                            launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                        }
                        .addOnFailureListener {
                            onResult(null)
                        }
                } else {
                    onResult(null)
                }
            }
        }
    }
}