package com.example

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val serviceIntent = android.content.Intent(this, MediaForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val targetUrl = "https://global-tv-elite.ai.studio/"

        setContent {
            MyApplicationTheme {
                var webViewRef by remember { mutableStateOf<WebView?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var hasError by remember { mutableStateOf(false) }

                BackHandler(enabled = true) {
                    if (webViewRef?.canGoBack() == true) {
                        webViewRef?.goBack()
                    } else {
                        finish()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            setSupportZoom(true)
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                super.onPageStarted(view, url, favicon)
                                                isLoading = true
                                                hasError = false
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                isLoading = false
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: WebResourceError?
                                            ) {
                                                super.onReceivedError(view, request, error)
                                                if (request?.isForMainFrame == true) {
                                                    hasError = true
                                                    isLoading = false
                                                }
                                            }

                                            override fun onReceivedSslError(
                                                view: WebView?,
                                                handler: SslErrorHandler?,
                                                error: SslError?
                                            ) {
                                                handler?.proceed()
                                            }
                                        }
                                        webViewRef = this
                                        loadUrl(targetUrl)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            if (hasError) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Unable to load page. Please check your internet connection.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
