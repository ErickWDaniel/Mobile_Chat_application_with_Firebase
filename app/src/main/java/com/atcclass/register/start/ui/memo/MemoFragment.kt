package com.atcclass.register.start.ui.memo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.atcclass.register.R


class MemoFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memo, container, false)

    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webview)
        //To enable javascripty code..Be care to use this method on ATC only since you dont trust other site
        webView.settings.javaScriptEnabled = true


        // enable cache
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        webView.webViewClient = MemoWebViewClient()
        webView.loadUrl("https://atc.ac.tz/index.php/news-events")

        // enable back button navigation in webview
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && webView.canGoBack()) {
                webView.goBack()
                return@OnKeyListener true
            }
            false
        })
    }


    inner class MemoWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // do any additional processing here after the page has finished loading
        }
    }
}
