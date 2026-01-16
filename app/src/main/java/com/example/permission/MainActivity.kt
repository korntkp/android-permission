package com.example.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )

    // Activity Result API for permission requests
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnStartCall = findViewById<Button>(R.id.btnStartCall)

        btnStartCall.setOnClickListener {
            if (hasPermissions()) {
                openWebViewScreen()
            } else {
                requestPermissions()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions)
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            openWebViewScreen()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.toTypedArray()
            handleDeniedPermissions(deniedPermissions)
        }
    }

    private fun openWebViewScreen() {
        val intent = Intent(this, WebViewActivity::class.java)
        startActivity(intent)
    }

    private fun handleDeniedPermissions(permissions: Array<String>) {
        var permanentlyDenied = false

        for (permission in permissions) {
            // If the permission is denied...
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Check if user selected "Don't ask again" (Permanent Denial)
                if (!shouldShowRequestPermissionRationale(permission)) {
                    permanentlyDenied = true
                    break
                }
            }
        }

        if (permanentlyDenied) {
            showSettingsDialog()
        } else {
            showRetryDialog()
        }
    }

    // Dialog for "Don't ask again" scenario
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Camera and Microphone permissions are required for video calls. You have permanently denied them.\n\nPlease go to Settings to enable them manually.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Dialog for normal denial (User just clicked "Deny" once)
    private fun showRetryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("We cannot start the video call without Camera and Audio access. Please allow permissions.")
            .setPositiveButton("Try Again") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Open the App's specific settings page in Android OS
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
