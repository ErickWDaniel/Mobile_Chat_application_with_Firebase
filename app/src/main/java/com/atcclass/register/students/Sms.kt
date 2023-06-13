package com.atcclass.register.students

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.controls.actions.FloatAction
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.atcclass.register.R
import com.atcclass.register.start.ui.MainActivity
import com.atcclass.register.start.ui.memo.MemoFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Sms : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var backfromsms: FloatingActionButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        supportActionBar?.hide()
        backfromsms=findViewById(R.id.backfromsms)
        backfromsms.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
            else{
                val goback = Intent(this, MainActivity::class.java)
                startActivity(goback)
                finish()
            }
        }

        webView = findViewById(R.id.studentmanagement)
        webView.settings.javaScriptEnabled = true

        // Enable caching
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.domStorageEnabled = true

        // Check if the web page is available in the cache
        val pageExistsInCache = isWebPageCached()

        if (pageExistsInCache) {
            // Load the web page from the cache
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            // Load the web page from the internet
            webView.webViewClient = MemoWebViewClient()
            webView.loadUrl("https://www.atc.ac.tz/sms/index.php")
        }
    }

    private fun isWebPageCached(): Boolean {
        // Check if the web page is available in the cache directory
        val cachePath = File(cacheDir, "web_cache")
        val indexFile = File(cachePath, "index.html")
        return indexFile.exists()
    }

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

