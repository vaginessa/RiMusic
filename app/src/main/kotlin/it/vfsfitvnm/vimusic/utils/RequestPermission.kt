package it.vfsfitvnm.vimusic.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun requestPermission(activity: Activity, permission: String) {
    if (ContextCompat.checkSelfPermission(
            activity,
            //Manifest.permission.READ_EXTERNAL_STORAGE
        permission
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                //Manifest.permission.READ_EXTERNAL_STORAGE
                permission
            ), 42
        )
    }
}