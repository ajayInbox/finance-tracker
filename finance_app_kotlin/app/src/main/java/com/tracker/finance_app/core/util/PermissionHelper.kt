package com.tracker.finance_app.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private fun requiredSmsPermissions(): List<String> = buildList {
    add(Manifest.permission.READ_SMS)
    add(Manifest.permission.RECEIVE_SMS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}

fun hasSmsPermissions(context: Context): Boolean =
    requiredSmsPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/**
 * Returns a trigger that launches the SMS + notification permission request.
 * Invokes [onPermissionsGranted] immediately if permissions are already granted.
 */
@Composable
fun rememberSmsPermissionRequest(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.values.all { it }) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    return {
        val required = requiredSmsPermissions()
        if (hasSmsPermissions(context)) {
            onPermissionsGranted()
        } else {
            launcher.launch(required.toTypedArray())
        }
    }
}

@Composable
fun RequestSmsPermissions(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val request = rememberSmsPermissionRequest(onPermissionsGranted, onPermissionsDenied)
    LaunchedEffect(Unit) { request() }
}
