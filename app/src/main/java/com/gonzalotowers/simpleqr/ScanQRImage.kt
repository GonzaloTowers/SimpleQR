package com.gonzalotowers.simpleqr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.Reader
import com.google.zxing.common.HybridBinarizer
import java.io.*
import java.util.*

const val PICK_IMAGE: Int = 1
const val READ_EXTERNAL_STORAGE_CODE: Int = 300

class ScanQRImage : AppCompatActivity() {

    private lateinit var buttonSettings: Button
    private lateinit var resultText: TextView
    private lateinit var imageIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_activity)
        buttonSettings = findViewById(R.id.button_conf)
        resultText = findViewById(R.id.result_text)
        imageIcon = findViewById(R.id.ic_warning)

        findViewById<Button>(R.id.button_back).setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        findViewById<Button>(R.id.button_scan).setOnClickListener {
            val scanIntent = Intent(this, CustomCaptureActivity::class.java)
            startActivity(scanIntent)
        }

        findViewById<Button>(R.id.button_image).setOnClickListener {
            val imageIntent = Intent(this, ScanQRImage::class.java)
            startActivity(imageIntent)
        }

        requestPermission()

    }

    private fun requestPermission() {
        val storagePerm : String = Manifest.permission.READ_EXTERNAL_STORAGE
        // Gallery permission
        if (ContextCompat.checkSelfPermission(this, storagePerm) != PackageManager.PERMISSION_GRANTED) {
            warningMessage(this.getString(R.string.text_permission_denied_300))
            buttonSettings.visibility = View.VISIBLE
            buttonSettings.setOnClickListener {
                alertDialog()
            }
            if(!ActivityCompat.shouldShowRequestPermissionRationale(this, storagePerm)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_CODE)
                }
            }
        } else {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonSettings.visibility = View.GONE
                    imageIcon.visibility = View.GONE
                    resultText.text = this.getString(R.string.text_empty)
                    init()
                } else {
                    warningMessage(this.getString(R.string.text_permission_denied_300))
                    buttonSettings.visibility = View.VISIBLE
                    buttonSettings.setOnClickListener {
                        alertDialog()
                    }
                }
                return
            }
        }
    }

    private fun init() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, this.getString(R.string.text_select_image)), PICK_IMAGE)
    }

    private fun scanFromImage(file: File) {
        val ist: InputStream = BufferedInputStream(FileInputStream(file))
        val bitmap = BitmapFactory.decodeStream(ist)
        val decoded = scanQRImage(bitmap)
        if (decoded != null) {
            if (Patterns.WEB_URL.matcher(decoded.toLowerCase(Locale.ROOT)).matches()) {
                findViewById<FrameLayout>(R.id.frame_result).visibility = View.GONE
                val urlDocs: String = this.getString(R.string.url_docs)
                val webView: WebView = findViewById(R.id.webViewer)
                webView.visibility = View.VISIBLE
                webView.settings.javaScriptEnabled = true
                webView.loadUrl(urlDocs + decoded)

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
                textView.text = decoded
            }

        } else {
            warningMessage(this.getString(R.string.text_invalid_image))
        }
    }

    private fun scanQRImage(bMap: Bitmap): String? {
        var contents: String? = null
        val intArray = IntArray(bMap.width * bMap.height)
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource =
            RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val reader: Reader = MultiFormatReader()
        try {
            val result: Result = reader.decode(bitmap)
            contents = result.text
        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
        }
        return contents
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Log.i("ScanQRImage", "onActivityResult pick image")
            val selectedImageUri: Uri? = data!!.data
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)

            try {
                val file = File(cacheDir, "cacheFileAppeal.srl")
                FileOutputStream(file).use { output ->
                    val buffer =
                        ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream!!.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                scanFromImage(file)
            } finally {
                inputStream!!.close()
            }

        }
    }

    private fun warningMessage(message: String) {
        imageIcon.visibility = View.VISIBLE
        resultText.text = message
        resultText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        resultText.gravity = Gravity.CENTER
        val params: FrameLayout.LayoutParams =
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(10, 230, 10, 10)
        resultText.layoutParams = params
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
                if (ContextCompat.checkSelfPermission(this@ScanQRImage, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    val i = Intent(this@ScanQRImage, ScanQRImage::class.java)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

}