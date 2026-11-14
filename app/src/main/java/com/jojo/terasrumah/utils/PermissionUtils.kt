package com.jojo.terasrumah.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun getStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 and below
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun hasStoragePermissions(context: Context): Boolean {
        val permissions = getStoragePermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions(
        activity: ComponentActivity,
        onPermissionResult: (Boolean) -> Unit
    ) {
        val permissions = getStoragePermissions()
        
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val allGranted = permissionsMap.values.all { it }
            onPermissionResult(allGranted)
        }
        
        requestPermissionLauncher.launch(permissions)
    }
}
