package com.example.permission

import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.web_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView = findViewById(R.id.videoWebView)
        setupWebView()

        webView.loadUrl("https://www.google.com")

        // This registers a callback that listens for back swipes/clicks
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If the WebView has history (e.g. user clicked a link), go back in browser
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupWebView() {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // 1. Handle Navigation inside WebView (don't open Chrome)
        webView.webViewClient = WebViewClient()

        // 2. Handle Web Permissions (The Bridge)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                // Since we asked for OS permissions in Screen 1,
                // we can auto-grant the Web permission request here.
                request?.grant(request.resources)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause WebView to save resources when activity is in background
        webView.onPause()
        webView.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        // Resume WebView when activity comes to foreground
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onDestroy() {
        // Clean up WebView to prevent memory leaks
        webView.destroy()
        super.onDestroy()
    }
}
