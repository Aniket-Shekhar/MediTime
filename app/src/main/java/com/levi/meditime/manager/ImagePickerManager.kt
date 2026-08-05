package com.levi.meditime.manager

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

class ImagePickerManager(private val context: Context) {

    fun createTempUri(): Uri {
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }

    fun copyToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val manager = remember { ImagePickerManager(context) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { onImageSelected(it) }
        }
    }

    return remember(manager, galleryLauncher, cameraLauncher) {
        ImagePickerLauncher(
            pickFromGallery = { galleryLauncher.launch("image/*") },
            takePhoto = {
                val uri = manager.createTempUri()
                cameraUri = uri
                cameraLauncher.launch(uri)
            },
            manager = manager
        )
    }
}

class ImagePickerLauncher(
    val pickFromGallery: () -> Unit,
    val takePhoto: () -> Unit,
    val manager: ImagePickerManager
)
