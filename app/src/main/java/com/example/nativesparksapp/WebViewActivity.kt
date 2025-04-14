package com.example.nativesparksapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var webView: WebView
    private lateinit var progressLoading: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)
        progressLoading = findViewById(R.id.progressLoading)

        // Preleva l'URL passato via Intent
        val urlToLoad = intent.getStringExtra(EXTRA_URL) ?: "https://google.com"

        // Config WebView
        webView.settings.javaScriptEnabled = true  // Se Drimify / Mindsmith necessitano di JS
        webView.settings.domStorageEnabled = true  // Se ci sono interazioni particolari
        webView.webViewClient = object : WebViewClient() {

            // Quando inizia a caricare, mostra la ProgressBar
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressLoading.visibility = View.VISIBLE
            }

            // Quando finisce di caricare, nascondi la ProgressBar
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressLoading.visibility = View.GONE
            }

            // Assicura che resti dentro l’app e non apra link esterni
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        // Carica la pagina
        webView.loadUrl(urlToLoad)
    }

    // Supporta il tasto "indietro" per tornare nelle pagine precedenti
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
