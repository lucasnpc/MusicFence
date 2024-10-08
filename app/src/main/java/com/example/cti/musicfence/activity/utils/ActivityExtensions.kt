package com.example.cti.musicfence.activity.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cti.musicfence.activity.MainActivity.Companion.MY_PERMISSIONS_READ_EXTERNAL_STORAGE

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
inline fun Activity.handlePermissionAboveApi33(action: () -> Unit) {
    val permissions = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
    }
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_VIDEO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
    }
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
    }
    if (permissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            MY_PERMISSIONS_READ_EXTERNAL_STORAGE
        )
    } else {
        action()
    }
}

fun Activity.handlePermissionBeforeApi33(action: () -> Unit) {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_READ_EXTERNAL_STORAGE
        )
    } else {
        action()
    }
}