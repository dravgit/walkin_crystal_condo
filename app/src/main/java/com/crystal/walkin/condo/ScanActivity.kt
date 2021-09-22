package com.crystal.walkin.condo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.crystal.walkin.R
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    val CAMERA_REQUEST_CODE = 5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissions()
        setContentView(R.layout.activity_scan)
        search.setOnClickListener {
            goBackWithCode(edt_code.text.toString())
        }
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                                                           Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("CAMERA_REQUEST_CODE", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                                          arrayOf(Manifest.permission.CAMERA),
                                          CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("CAMERA_REQUEST_CODE", "Permission has been denied by user")
                    this.finish()
                } else {
                    Log.i("CAMERA_REQUEST_CODE", "Permission has been granted by user")
                }
            }
        }
    }

    fun enableButtonSearch() {
        val isReady = edt_code.text.toString().length > 6
        search.isEnabled = isReady
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        goBackWithCode(rawResult.text)
    }

    fun goBackWithCode(code: String) {
        val intent = Intent()
        intent.putExtra("barcode", code)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
