package com.gonzalotowers.simpleqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

const val CAMERA_CODE: Int = 100
const val INTERNET_CODE: Int = 200

const val CAMERA_PERM : String = Manifest.permission.CAMERA
const val INTERNET_PERM : String = Manifest.permission.INTERNET
const val ACCESS_NET_PERM : String = Manifest.permission.ACCESS_NETWORK_STATE

const val ACTIVITY : String = "CustomCaptureActivity"

class CustomCaptureActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    private lateinit var buttonSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(ACTIVITY, "onCreate")
        super.onCreate(savedInstanceState)
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        setContentView(R.layout.custom_capture_layout)
        findViewById<Button>(R.id.button_image).setOnClickListener {
            val imageIntent = Intent(this, ScanQRImage::class.java)
            startActivity(imageIntent)
        }
        findViewById<Button>(R.id.button_cancel).setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        buttonSettings = findViewById(R.id.button_conf)

        checkForPermissions()

        findViewById<ImageView>(R.id.image_logo).bringToFront()
    }

    private fun checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, CAMERA_PERM) != PackageManager.PERMISSION_GRANTED) {
                warningMessage(this.getString(R.string.text_permission_denied_100))
                buttonSettings.visibility = View.VISIBLE
                buttonSettings.setOnClickListener {
                    alertDialog()
                }
                createPermissions()
            } else {
                init()
            }
        }
    }

    private fun createPermissions() {
        // Camera permission
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERM) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_CODE)
                }
            }
        }
        // Internet permission
        if (ContextCompat.checkSelfPermission(this, INTERNET_PERM) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, INTERNET_PERM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.INTERNET), INTERNET_CODE)
                }
            }
        }
        // Access network permission
        if (ContextCompat.checkSelfPermission(this, ACCESS_NET_PERM) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_NET_PERM)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), INTERNET_CODE)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_CODE -> {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findViewById<FrameLayout>(R.id.frame_result).visibility = View.GONE
                    init()
                } else {
                    warningMessage(this.getString(R.string.text_permission_denied_100))
                    buttonSettings.visibility = View.VISIBLE
                    buttonSettings.setOnClickListener {
                        alertDialog()
                    }
                    this.createPermissions()
                }
            }
        }
    }

    private fun init() {
        //Scanner
        mScannerView = ZXingScannerView(this)
        val rl = findViewById<ConstraintLayout>(R.id.custom_capture_layout)
        rl.addView(mScannerView)
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
        mScannerView!!.isSoundEffectsEnabled = true
    }

    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera() // Start camera on resume
    }

    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera() // Stop camera on pause
    }

    override fun onStop() {
        super.onStop()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(result: Result) {
        val resultIntent = Intent(this, ResultActivity::class.java)
        resultIntent.putExtra("result", result.text)
        startActivity(resultIntent)
    }

    private fun warningMessage(message: String) {
        findViewById<ImageView>(R.id.ic_warning).visibility = View.VISIBLE
        val textView = findViewById<TextView>(R.id.result_text)
        textView.text = message
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        textView.gravity = Gravity.CENTER
        val params: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(10, 230, 10, 10)
        textView.layoutParams = params
    }

    private fun alertDialog() {
        AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.text_settings))
            .setMessage(this.getString(R.string.text_permission_settings))
            .setPositiveButton(this.getString(R.string.text_ok)) { dialog, which ->
                openAppSettings()
            }
            .setNegativeButton(this.getString(R.string.text_cancel)) { dialog, which -> dialog.dismiss() }.create()
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri

        val handler = Handler()
        val checkSettingOn: Runnable = object : Runnable {
            override fun run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return
                }
                if (ContextCompat.checkSelfPermission(this@CustomCaptureActivity, CAMERA_PERM) == PackageManager.PERMISSION_GRANTED) {
                    val i = Intent(this@CustomCaptureActivity, CustomCaptureActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(i)
                    return
                }
                handler.postDelayed(this, 100)
            }
        }

        handler.postDelayed(checkSettingOn, 100)

        startActivity(intent)
    }

    override fun onRestart() {
        super.onRestart()
        val customCaptureLayout = Intent(this, CustomCaptureActivity::class.java)
        startActivity(customCaptureLayout)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

}