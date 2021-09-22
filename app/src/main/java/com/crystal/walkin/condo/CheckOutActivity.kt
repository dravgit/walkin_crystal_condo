package com.crystal.walkin.condo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.RemoteException
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.crystal.walkin.R
import com.crystal.walkin.condo.app.WalkinApplication
import com.crystal.walkin.condo.models.CheckInResponseModel
import com.crystal.walkin.condo.models.CheckOutResponseModel
import com.crystal.walkin.condo.models.VisitorResponseModel
import com.crystal.walkin.condo.models.WalkInErrorModel
import com.crystal.walkin.condo.utils.BitmapUtils
import com.crystal.walkin.condo.utils.NetworkUtil
import com.crystal.walkin.condo.utils.PreferenceUtils
import com.crystal.walkin.condo.utils.Util
import com.sunmi.peripheral.printer.InnerResultCallbcak
import com.sunmi.peripheral.printer.SunmiPrinterService
import kotlinx.android.synthetic.main.activity_check_out.*
import sunmi.sunmiui.utils.LogUtil

class CheckOutActivity : BaseActivity() {
    private var sunmiPrinterService: SunmiPrinterService? = null
    var alreadyOut = false
    override fun onPrintDeviceConnected(manager: AidlDeviceManager?) {
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    override fun onDeviceConnectedSwipe(manager: AidlDeviceManager?) {
    }

    override fun showMessage(str: String?, black: Int) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val SCAN_REQUEST_CODE = 0
        val SLIP_REQUEST_CODE = 1
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)
        btnSlip.setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, SLIP_REQUEST_CODE)
        }
        btnScan.setOnClickListener{
            val intent = Intent(this, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_REQUEST_CODE)
        }
        cancleCheckout.setOnClickListener{
            this@CheckOutActivity.finish()
        }
        okCheckout.setOnClickListener{
            var encode = ""
            if(!tVencode.getText().toString().isEmpty()){
                encode = tVencode.getText().toString()
            }
            if(tVcode != null) {
                if (!alreadyOut) {
                    NetworkUtil.checkOut(tVcode.getText().toString(),encode, object : NetworkUtil.Companion.NetworkLisener<CheckOutResponseModel> {
                        override fun onResponse(response: CheckOutResponseModel) {
                            Log.e("Status", "SUCCESS")
                            printP2(response)
                            this@CheckOutActivity.finish()
                        }

                        override fun onError(errorModel: WalkInErrorModel) {
                            Log.e("Status", "ERROR")
                            checkError(errorModel)
                        }

                        override fun onExpired() {
                            okCheckout.callOnClick()
                        }
                    }, CheckOutResponseModel::class.java)
                } else {
                    Toast.makeText(this, "ออกไปแล้ว", Toast.LENGTH_LONG).show()
                }
            }
        }

        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check that it is the SecondActivity with an OK result
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val code = data!!.getStringExtra("barcode")
                searchData(code)
            }
        }
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                imgVslip.setImageBitmap(null)
                val bmp = data.extras?.get("data") as Bitmap
                imgVslip.setImageBitmap(rotageBitmap(bmp))
                var encode = Util.encodeImg(imgVslip)
                tVencode.setText(encode)
            }else{
                Toast.makeText(this, "เกิดข้อผิดพลาดบางอย่าง", Toast.LENGTH_SHORT)
            }
        }
    }


    fun searchData(code: String){
        NetworkUtil.searchByOrder(code, object : NetworkUtil.Companion.NetworkLisener<VisitorResponseModel>{
            override fun onResponse(response: VisitorResponseModel) {
                var data = response
                alreadyOut = data.status.equals("1")
                var list = data.images
                Log.e("LIST",list.toString())
                tVname.setText(data.name())
                tVidcard.setText(data.idcard())
                tVcar.setText(data.vehicle_id())
                tVtemperate.setText(data.temperature())
                tVdepartment.setText(data.department())
                tVobjective.setText(data.objective())
                tVcheckintime.setText(data.checkin_time())
                tVcode.setText(data.contact_code)
                if (alreadyOut) {
                    okCheckout.visibility = View.GONE
                    headtVcheckouttime.visibility = View.VISIBLE
                    tVcheckouttime.setText(data.checkout_time)
                } else {
                    okCheckout.visibility = View.VISIBLE
                    headtVcheckouttime.visibility = View.GONE
                    tVcheckouttime.setText("")

                }
                if(list[0].url != ""){
                    imgVperson.setBackground(null)
                    Glide.with(this@CheckOutActivity)
                            .load(list[0].url)
                            .placeholder(android.R.color.background_light)
                            .error(android.R.color.background_light)
                            .into(imgVperson)
                }else if(list[3].url != ""){
                    imgVperson.setBackground(null)
                    Glide.with(this@CheckOutActivity)
                            .load(list[3].url)
                            .placeholder(android.R.color.background_light)
                            .error(android.R.color.background_light)
                            .into(imgVperson)
                }
                if(list[1].url != ""){
                    imgVcar.setBackground(null)
                    Glide.with(this@CheckOutActivity)
                            .load(list[1].url)
                            .placeholder(android.R.color.background_light)
                            .error(android.R.color.background_light)
                            .into(imgVcar)
                }
            }

            override fun onError(errorModel: WalkInErrorModel) {
                Log.e("CHECK",errorModel.toString())
                checkError(errorModel)
            }

            override fun onExpired() {
                searchData(code)
            }
        },VisitorResponseModel::class.java)
    }

    private fun printP2(data: CheckOutResponseModel) {
//        val square = BitmapFactory.decodeResource(resources, R.drawable.square)
        setHeight(0x11)
        sunmiPrinterService!!.clearBuffer()
        sunmiPrinterService!!.enterPrinterBuffer(true)
        val bitmap = Util.createImageFromQRCode(data.contact_code)
        sunmiPrinterService!!.printText("\n        ", innerResultCallbcak)
        sunmiPrinterService!!.printBitmap(resizeBitmap(PreferenceUtils.getBitmapLogo()), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\nบริษัท : " + PreferenceUtils.getCompanyName() +
                "\nชื่อ-นามสกุล : " + data.fullname.replace(" ", " ") +
                "\nเลขบัตรประขาชน : " + data.idcard +
                "\nทะเบียนรถ : " + data.vehicle_registration +
                "\nติดต่อ : " + data.person_contact +
                "\nรายละเอียด : " + data.department_id +
                "\nเวลาเข้า : " + data.checkin_time +
                "\nเวลาออก : " + data.checkout_time +
                "\nเวลาที่อยู่ : " + data.show_time, innerResultCallbcak)

//        sunmiPrinterService!!.printText("\n\n" + PreferenceUtils.getCompanyNote(), innerResultCallbcak)
//
//        sunmiPrinterService!!.printText("\n      ", innerResultCallbcak)
//        sunmiPrinterService!!.printBitmap(bitmap, innerResultCallbcak)
//        sunmiPrinterService!!.printText("\n        " + data.contact_code, innerResultCallbcak)
//
//        sunmiPrinterService!!.printBitmap(resizeBitmap(square), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\n\n\n\n\n\n\n ", innerResultCallbcak)
        sunmiPrinterService!!.commitPrinterBuffer()
    }

    @Throws(RemoteException::class)
    fun setHeight(height: Int) {
        val returnText = ByteArray(3)
        returnText[0] = 0x1B
        returnText[1] = 0x33
        returnText[2] = height.toByte()

        if (sunmiPrinterService == null) {
            sunmiPrinterService = WalkinApplication.sunmiPrinterService
        }
        sunmiPrinterService?.sendRAWData(returnText, null)
    }

    private fun resizeBitmap(cacheBitmap: Bitmap): Bitmap {
        var cacheBitmap = BitmapUtils.scale(cacheBitmap, cacheBitmap.width, cacheBitmap.height)
        cacheBitmap = BitmapUtils.replaceBitmapColor(cacheBitmap, Color.TRANSPARENT, Color.WHITE)
        if (cacheBitmap.width > 384) {
            val newHeight = (1.0 * cacheBitmap.height * 384 / cacheBitmap.width).toInt()
            cacheBitmap = BitmapUtils.scale(cacheBitmap, 384, newHeight)
        }
        return cacheBitmap
    }

    private var `is` = true
    private val innerResultCallbcak: InnerResultCallbcak = object : InnerResultCallbcak() {
        override fun onRunResult(isSuccess: Boolean) {
            LogUtil.e("lxy", "isSuccess:$isSuccess")
            if (`is`) {
                try {
                    sunmiPrinterService!!.lineWrap(6, this)
                    `is` = false
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onReturnString(result: String) {
            LogUtil.e("lxy", "result:$result")
        }

        override fun onRaiseException(code: Int, msg: String) {
            LogUtil.e("lxy", "code:$code,msg:$msg")
        }

        override fun onPrintResult(code: Int, msg: String) {
            LogUtil.e("lxy", "code:$code,msg:$msg")
        }
    }
}
