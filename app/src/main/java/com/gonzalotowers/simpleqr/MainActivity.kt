package com.gonzalotowers.simpleqr

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_scan).setOnClickListener {
            val scanIntent = Intent(this, CustomCaptureActivity::class.java)
            startActivity(scanIntent)
        }

        findViewById<Button>(R.id.button_image).setOnClickListener {
            val imageIntent = Intent(this, ScanQRImage::class.java)
            startActivity(imageIntent)
        }
    }

}
