package com.gonzalotowers.simpleqr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)

        val intent: Intent = intent
        val result = intent.getStringExtra("result")

        if (result != null) {
            handleResult(result)
        } else {
            Toast.makeText(this, this.getString(R.string.text_error), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_back).setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        findViewById<Button>(R.id.button_scan).setOnClickListener {
            val scanIntent : Intent = Intent(this, CustomCaptureActivity::class.java)
            startActivity(scanIntent)
        }

        findViewById<Button>(R.id.button_image).setOnClickListener {
            val imageIntent = Intent(this, ScanQRImage::class.java)
            startActivity(imageIntent)
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun handleResult(result: String) {
        if (Patterns.WEB_URL.matcher(result.toLowerCase(Locale.ROOT)).matches()) {
            val urlDocs: String = this.getString(R.string.url_docs)
            val webView: WebView = findViewById(R.id.webViewer)
            webView.visibility = View.VISIBLE
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(urlDocs + result)

            // Any link clicked inside the webview does not open browser and works inside webview
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(urlDocs + url)
                    return true
                }
            }
        } else {
            findViewById<FrameLayout>(R.id.frame_result).visibility = View.VISIBLE
            val textView: TextView = findViewById(R.id.result_text)
            textView.text = result
        }
    }

}