package com.atcclass.register.admin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.atcclass.register.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
class Admin : AppCompatActivity() {

    private lateinit var webView: WebView
    private  lateinit var backbutton:FloatingActionButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        supportActionBar?.hide()
        backbutton=findViewById(R.id.backfromadmin)
        backbutton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                super.onBackPressed()
            }
        }

        webView = findViewById(R.id.webadmin)
        webView.settings.javaScriptEnabled = true

        // Enable caching
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.domStorageEnabled = true

        // Check if the web page is available in the cache so that it can load,,by the way erick remember to check offline
        val pageExistsInCache = isWebPageCached()

        if (pageExistsInCache) {
            // Load the web page from the cache
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            // Load the web page from the internet direct to the firebase page..this is not advisor erick,look for another way
            webView.webViewClient = MemoWebViewClient()
            webView.loadUrl("https://console.firebase.google.com/u/0/project/final-year-project-atc/overview")
        }
    }

    private fun isWebPageCached(): Boolean {
        // Check if the web page is available in the cache directory
        val cachePath = File(cacheDir, "web_cache")
        val indexFile = File(cachePath, "index.html")
        return indexFile.exists()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class MemoWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // Additional processing after the page has finished loading

            // Save the web page resources to the cache
            saveWebPageToCache()
        }
    }

    private fun saveWebPageToCache() {
        // Copy the web page resources to the cache directory
        val cachePath = File(cacheDir, "web_cache")
        cachePath.mkdirs()

        try {
            val inputStream = assets.open("index.html")
            val outputFile = File(cachePath, "index.html")
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}