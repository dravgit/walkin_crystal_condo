package com.crystal.walkin.condo

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.RemoteException
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager.BadTokenException
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.centerm.centermposoversealib.thailand.*
import com.centerm.smartpos.aidl.iccard.AidlICCard
import com.centerm.smartpos.aidl.magcard.AidlMagCard
import com.centerm.smartpos.aidl.magcard.AidlMagCardListener
import com.centerm.smartpos.aidl.magcard.TrackData
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import com.centerm.smartpos.util.HexUtil
import com.crystal.walkin.BuildConfig
import com.crystal.walkin.R
import com.crystal.walkin.condo.app.WalkinApplication
import com.crystal.walkin.condo.models.*
import com.crystal.walkin.condo.utils.BitmapUtils
import com.crystal.walkin.condo.utils.ByteUtil
import com.crystal.walkin.condo.utils.NetworkUtil.Companion.NetworkLisener
import com.crystal.walkin.condo.utils.NetworkUtil.Companion.checkIn
import com.crystal.walkin.condo.utils.PreferenceUtils
import com.crystal.walkin.condo.utils.SunmiUtility
import com.crystal.walkin.condo.utils.Util.Companion.createImageFromQRCode
import com.crystal.walkin.condo.wrapper.CheckCardCallbackV2Wrapper
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.peripheral.printer.InnerResultCallbcak
import com.sunmi.peripheral.printer.SunmiPrinterService
import com.watermark.androidwm_light.WatermarkBuilder
import com.watermark.androidwm_light.bean.WatermarkText
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sunmi.sunmiui.utils.LogUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen;
//import com.centerm.centermposoversealib.thailand.ThaiIDSecurityListerner;
class CheckInActivity() : BaseActivity() {
    private val watermarkTxt = "ใช้สำหรับงาน รปภ.เท่านั้น"
    private var printDev: AidlPrinter? = null
    private val callback: AidlPrinterStateChangeListener = PrinterCallback()
    private var magCard: AidlMagCard? = null
    private val expdate: TextView? = null
    private var tVbirth: TextView? = null
    private val tVaddress: TextView? = null
    private var tVgender: TextView? = null
    private val name: TextView? = null
    private val bdate: TextView? = null
    private val xid: TextView? = null
    private val mHandler = Handler()
    private val TAG = javaClass.simpleName
    private var aidlIdCardTha: AidlIdCardTha? = null
    private var aidlIcCard: AidlICCard? = null
    private val photoImg: ImageView? = null
    private val resultText: TextView? = null
    var time1: Long = 0
    private var aidlReady = false
    var scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mLoading: ProgressDialog? = null
    private var sLoading: ProgressDialog? = null
    private var mediaPlayer: MediaPlayer? = null
    private var edtnameTH: EditText? = null
    private var edtidcard: EditText? = null
    private var edtCar: EditText? = null
    private var edtTemp: EditText? = null
    private var edtaddress: EditText? = null
    private var edtfrom: EditText? = null
    private var edtperson: EditText? = null
    private var edtnote: EditText? = null
    private val months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    private var iVphoto: ImageView? = null
    private var capUser: ImageButton? = null
    private var capCar: ImageButton? = null
    private var capCard: ImageButton? = null
    private var cardWaterMark: Bitmap? = null
    private var carWaterMark: Bitmap? = null
    private var userWaterMark: Bitmap? = null
    private var face: Bitmap? = null
    private val testBtn: Button? = null
    private var cancleCheckin: Button? = null
    private var okCheckin: Button? = null
    private var uriCarFilePath: Uri? = null
    private var uriCardFilePath: Uri? = null
    private var uriUserFilePath: Uri? = null
    private var dropdownDepartment: Spinner? = null
    private var dropdownObjective: Spinner? = null
    var department = arrayOf("", "1", "2", "three")
    var objective = arrayOf("", "4", "5", "five")
    var watermarkText: WatermarkText? = null
    var disposable: Disposable? = null
    private var sunmiPrinterService: SunmiPrinterService? = null


    internal var baCommandAPDU = byteArrayOf(0x00.toByte(),
            0xA4.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x08.toByte(),
            0xA0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x54.toByte(),
            0x48.toByte(),
            0x00.toByte(),
            0x01.toByte())
    private val _UTF8_CHARSET = Charset.forName("TIS-620")
    private val _req_version = "80b00000020004"
    private val handler = Handler()
    private var photoBytes: ByteArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in)
        mLoading = ProgressDialog(this)
        mLoading!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mLoading!!.setCanceledOnTouchOutside(false)
        mLoading!!.setMessage("Reading...")
        sLoading = ProgressDialog(this)
        sLoading!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        sLoading!!.setCanceledOnTouchOutside(false)
        sLoading!!.setMessage("Processing...")
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound)
        edtnameTH = findViewById(R.id.edtnameTH)
        edtidcard = findViewById(R.id.edtidcard)
        iVphoto = findViewById(R.id.iVphoto)
        Log.i("C", "Create2")
        edtaddress = findViewById<View>(R.id.edtaddress) as EditText
        tVgender = findViewById<View>(R.id.tVgender) as TextView
        tVbirth = findViewById<View>(R.id.tVbirth) as TextView
        edtfrom = findViewById<View>(R.id.edtfrom) as EditText
        edtCar = findViewById<View>(R.id.edtCar) as EditText
        edtTemp = findViewById<View>(R.id.edtTemp) as EditText
        edtperson = findViewById<View>(R.id.edtperson) as EditText
        edtnote = findViewById<View>(R.id.edtnote) as EditText
        capUser = findViewById<View>(R.id.user) as ImageButton
        capCar = findViewById<View>(R.id.car) as ImageButton
        capCard = findViewById<View>(R.id.card) as ImageButton
        cancleCheckin = findViewById<View>(R.id.cancleCheckin) as Button
        okCheckin = findViewById<View>(R.id.okCheckin) as Button
        dropdownDepartment = findViewById<View>(R.id.dropdownDepartment) as Spinner
        dropdownObjective = findViewById<View>(R.id.dropdownObjective) as Spinner
        val adapterDepartment = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, PreferenceUtils.getDepartment())
        val adapterObjective = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, PreferenceUtils.getObjectiveType())
        dropdownDepartment!!.adapter = adapterDepartment
        dropdownObjective!!.adapter = adapterObjective
        capUser!!.setOnClickListener { cameraUser() }
        capCar!!.setOnClickListener { cameraCar() }
        capCard!!.setOnClickListener { cameraCard() }
        watermarkText = WatermarkText(watermarkTxt).setPositionX(1.0)
            .setPositionY(1.0)
            .setRotation(40.0)
            .setTextAlpha(255)
            .setTextSize(50.0)
            .setTextColor(Color.WHITE)
            .setTextShadow(0.05f, 2f, 2f, Color.BLUE)
        cancleCheckin!!.setOnClickListener { finish() }
        okCheckin!!.setOnClickListener {
            sLoading!!.show()
            val name: String
            val department_id: String
            val objective_id: String
            var images: String = ""
            var jsonArray: JSONArray? = null
            try {
                jsonArray = fileImg()
                images = jsonArray.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val selectedItemOfDepartment = dropdownDepartment!!.selectedItemPosition
            val actualPositionOfDepartment = dropdownDepartment!!.getItemAtPosition(selectedItemOfDepartment) as DepartmentModel
            val selectedItemOfObjective = dropdownObjective!!.selectedItemPosition
            val actualPositionOfObjective = dropdownObjective!!.getItemAtPosition(selectedItemOfObjective) as ObjectiveTypeModel
            if (!edtnameTH?.getText()
                    .toString()
                    .isEmpty() && !edtfrom!!.text.toString()
                    .isEmpty() && !edtidcard?.getText()
                    .toString()
                    .isEmpty() && (jsonArray!!.length() != 0)) {
                name = edtnameTH?.getText()
                    .toString()
                department_id = actualPositionOfDepartment.getID()
                objective_id = actualPositionOfObjective.getID()
                val param = CheckInParamModel.Builder(name, department_id, objective_id, (images))
                Log.e("CHECK", param.toString())
                param.idcard(edtidcard?.getText()
                        .toString())
                    .vehicle_id(edtCar!!.text.toString())
                    .temperature(edtTemp!!.text.toString())
                    .gender(tVgender!!.text.toString())
                    .address(edtaddress!!.text.toString())
                    .from(edtfrom!!.text.toString())
                    .birthDate(tVbirth!!.text.toString())
                    .personContact(edtperson!!.text.toString())
                    .objectiveNote(edtnote!!.text.toString())
                val data = param.build()
                Log.e("DATA", data.toString())
                checkIn(data, object : NetworkLisener<CheckInResponseModel> {
                    override fun onResponse(response: CheckInResponseModel) {
                        sLoading!!.dismiss()
                        val data = response
//                        print(data)
                        printP2(data)
                        finish()
                    }

                    override fun onError(errorModel: WalkInErrorModel) {
                        checkError(errorModel)
                        sLoading!!.dismiss()
                    }

                    override fun onExpired() {
                        okCheckin!!.callOnClick()
                        sLoading!!.dismiss()
                    }
                }, CheckInResponseModel::class.java)
            } else {
                sLoading!!.dismiss()
                Toast.makeText(applicationContext, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // sunmi checkmagcard

        checkCard()
    }

    fun isJSONValid(JSON: String?): Boolean {
        try {
            JSONObject(JSON)
        } catch (ex: JSONException) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                JSONArray(JSON)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun fileImg(): JSONArray {
        val jsonArray = JSONArray()
        if (iVphoto!!.drawable != null) {
            iVphoto!!.invalidate()
            val drawable: BitmapDrawable? = iVphoto!!.drawable as BitmapDrawable
            if (drawable != null && drawable.bitmap != null) {
                face = drawable.bitmap
            }
            val imgIc = encodeImg(face, face!!.width, face!!.height, 100)
            val JObject = addImg(4, imgIc)
            jsonArray.put(JObject)
        }
        if (userWaterMark != null) {
            val imgIc = encodeImg(userWaterMark)
            val JObject = addImg(1, imgIc)
            jsonArray.put(JObject)
        }
        if (carWaterMark != null) {
            val imgIc = encodeImg(carWaterMark)
            val JObject = addImg(2, imgIc)
            jsonArray.put(JObject)
        }
        if (cardWaterMark != null) {
            val imgIc = encodeImg(cardWaterMark)
            val JObject = addImg(3, imgIc)
            jsonArray.put(JObject)
        }
        return jsonArray
    }

    private fun addImg(type: Int, base64: String): JSONObject {
        val img = JSONObject()
        try {
            img.put("file", base64)
            img.put("type", type)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return img
    }

    @Throws(IOException::class)
    private fun encodeImg(bitmap: Bitmap?, width: Int = bitmap!!.width / 5, height: Int = bitmap!!.height / 5, quality: Int = 70): String {
        val resize = Bitmap.createScaledBitmap((bitmap)!!, width, height, false)
        var encoded = ""
        val byteArrayOutputStream = ByteArrayOutputStream()
        resize.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        byteArrayOutputStream.close()
        return encoded
    }

    private fun cameraUser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_CODE)
        } else {
            uriUserFilePath = getUriByName("IMG_image_user")
            openCamera(CAMERA_USER_CODE, uriUserFilePath)
        }
    }

    private fun cameraCar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_CODE)
        } else {
            uriCarFilePath = getUriByName("IMG_image_car")
            openCamera(CAMERA_CAR_CODE, uriCarFilePath)
        }
    }

    private fun cameraCard() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERM_CODE)
        } else {
            uriCardFilePath = getUriByName("IMG_image_card")
            openCamera(CAMERA_CARD_CODE, uriCardFilePath)
        }
    }

    private fun getUriByName(name: String): Uri {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/walkin")
        var photoFile: File? = null
        try {
            photoFile = File.createTempFile("" + name,  /* prefix */
                    ".jpg",  /* suffix */
                    storageDir /* directory */)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", (photoFile)!!)
    }

    private fun openCamera(typeCode: Int, uri: Uri?) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(cameraIntent, typeCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_USER_CODE) {
            if (resultCode == RESULT_OK) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriUserFilePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                userWaterMark = WatermarkBuilder.create(this, rotageBitmap(bitmap))
                    .loadWatermarkText((watermarkText)!!)
                    .setTileMode(true).watermark.outputImage
                capUser!!.setImageBitmap(userWaterMark)
            }
        }
        if (requestCode == CAMERA_CAR_CODE) {
            if (resultCode == RESULT_OK) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriCarFilePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                carWaterMark = WatermarkBuilder.create(this, rotageBitmap(bitmap))
                    .loadWatermarkText((watermarkText)!!)
                    .setTileMode(true).watermark.outputImage
                capCar!!.setImageBitmap(carWaterMark)
            }
        }
        if (requestCode == CAMERA_CARD_CODE) {
            if (resultCode == RESULT_OK) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriCardFilePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                cardWaterMark = WatermarkBuilder.create(this, rotageBitmap(bitmap))
                    .loadWatermarkText((watermarkText)!!)
                    .setTileMode(true).watermark.outputImage
                capCard!!.setImageBitmap(cardWaterMark)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.size < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun bindService() {
        super.bindService()
        var intent = Intent()
        intent.setPackage("com.centerm.centermposoverseaservice")
        intent.action = "com.centerm.CentermPosOverseaService.MANAGER_SERVICE"
        bindService(intent, conn, BIND_AUTO_CREATE)
        val intent1 = Intent()
        intent1.setPackage("com.centerm.smartposservice")
        intent1.action = "com.centerm.smartpos.service.MANAGER_SERVICE"
        bindService(intent1, conn1, BIND_AUTO_CREATE)
        intent = Intent()
        intent.setPackage("com.centerm.smartposservice")
        intent.action = "com.centerm.smartpos.service.MANAGER_SERVICE"
        bindService(intent, conn2, BIND_AUTO_CREATE)
    }

    override fun onPrintDeviceConnected(manager: AidlDeviceManager) {
        try {
            printDev = AidlPrinter.Stub.asInterface(manager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private inner class PrinterCallback() : AidlPrinterStateChangeListener.Stub() {
        @Throws(RemoteException::class)
        override fun onPrintError(arg0: Int) {
            // showMessage("打印机异常" + arg0, Color.RED);
        }

        @Throws(RemoteException::class)
        override fun onPrintFinish() {
        }

        @Throws(RemoteException::class)
        override fun onPrintOutOfPaper() {
        }
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager) {
        try {
            var device = deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD)
            if (device != null) {
                aidlIcCard = AidlICCard.Stub.asInterface(device)
                if (aidlIcCard != null) {
                    Log.e("MY", "IcCard bind success!")
                    //This is the IC card service object!!!!
                    //I am do nothing now and it is not null.
                    //you can do anything by yourselef later.
                    d()
                } else {
                    Log.e("MY", "IcCard bind fail!")
                }
            }
            device = deviceManager.getDevice(com.centerm.centermposoversealib.constant.Constant.OVERSEA_DEVICE_CODE.OVERSEA_DEVICE_TYPE_THAILAND_ID)
            if (device != null) {
                aidlIdCardTha = AidlIdCardTha.Stub.asInterface(device)
                aidlReady = aidlIdCardTha != null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDeviceConnectedSwipe(deviceManager: AidlDeviceManager) {
        try {
            magCard = AidlMagCard.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_MAGCARD))
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            try {
                if (magCard != null) dd()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }

    override fun showMessage(str: String, black: Int) {
        showMessage(str, Color.BLACK)
    }

    private var stTestContinue = false
    private var invokStart: Long = 0
    private var invokCount = 0
    private var totalTime: Long = 0
    @Throws(RemoteException::class)
    private fun startStabilityTest() {
        invokStart = System.currentTimeMillis()
        aidlIdCardTha!!.searchIDCard(60000, test)
    }

    var test: AidlIdCardThaListener = object : AidlIdCardThaListener.Stub() {
        @Throws(RemoteException::class)
        override fun onFindIDCard(idInfoThaBean: ThiaIdInfoBeen) {
            totalTime += (System.currentTimeMillis() - invokStart)
            invokCount++
            if (stTestContinue) {
                if (checkInfo(idInfoThaBean)) {
                    displayResult("Testing...")
                    runOnUiThread {
                        try {
                            try {
                                Thread.sleep(200)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            startStabilityTest()
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                            displayResult("Exception...")
                        }
                    }
                } else {
                    displayResult("Check Info ERROR:")
                }
            } else {
                displayResult("Cancel:")
            }
        }

        @Throws(RemoteException::class)
        override fun onTimeout() {
            displayResult("Timeout:")
            runOnUiThread {
                stTestContinue = false
                testBtn!!.text = "Start Stability Test"
            }
        }

        @Throws(RemoteException::class)
        override fun onError(i: Int, s: String) {
            displayResult("Error:$i $s")
            runOnUiThread {
                stTestContinue = false
                testBtn!!.text = "Start Stability Test"
            }
        }
    }
    private var save = false
    private var jsonStr: String? = null
    private fun checkInfo(info: ThiaIdInfoBeen): Boolean {
        if (save) {
            return if ((jsonStr == info.toJSONString())) {
                true
            } else {
                false
            }
        } else {
            save = true
            jsonStr = info.toJSONString()
            showMsg(jsonFormat(jsonStr))
        }
        return true
    }

    private fun showInfo(msg: String, second: String) {
        runOnUiThread {
            try {
                val jObject = JSONObject(msg)
                Log.i(TAG, jObject.toString())
                val thName = jObject.getString("ThaiName")
                val regex = "(#)+"
                val output = thName.replace(regex.toRegex(), " ")
                edtnameTH!!.setText(output)
                val id_card = jObject.getString("CitizenId")
                //                    id_card = id_card.charAt(0) + "-" + id_card.charAt(1) + id_card.charAt(2) +
//                            id_card.charAt(3) + id_card.charAt(4) + "-" + id_card.charAt(5) +
//                            id_card.charAt(6) + id_card.charAt(7) + id_card.charAt(8) + id_card.charAt(9) +
//                            "-" + id_card.charAt(10) + id_card.charAt(11) + "-" + id_card.charAt(12);
//                    id_card = id_card.substring(0, 11) + "X-XX-X";
                edtidcard!!.setText(id_card)
                val gender = jObject.getString("Gender")
                val address = jObject.getString("Address")
                val birth = jObject.getString("BirthDate")
                edtaddress!!.setText(address.replace("#", " "))
                tVgender!!.text = gender
                tVbirth!!.text = birth
                Toast.makeText(this@CheckInActivity, "time running is " + second + "s", Toast.LENGTH_LONG)
                    .show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun getDateFromJson(date: String, reg: String): String {
        val _day = "" + date.substring(0, 2)
            .toInt()
        val _month_eng: String
        val _month_th: String
        var _year_th: String
        var _year_eng: String
        val _birth_eng: String
        val _birth_th: String
        if ((reg == "en")) {
            _month_eng = months_eng[date.substring(2, 4)
                    .toInt() - 1]
            _year_eng = "" + (date.substring(4, 8)
                .toInt() - 543)
            _year_eng = _year_eng.substring(0, 2) + "XX"
            _birth_eng = "$_day $_month_eng $_year_eng"
            return _birth_eng
        } else if ((reg == "th")) {
            _month_th = months_th[date.substring(2, 4)
                    .toInt() - 1]
            _year_th = date.substring(4, 8)
            _year_th = _year_th.substring(0, 2) + "XX"
            _birth_th = "$_day $_month_th $_year_th"
            return _birth_th
        }
        return ""
    }

    private fun showMsg(msg: String) {
        runOnUiThread { resultText!!.text = msg }
    }

    private fun showPhoto(bmp: Bitmap) {
        runOnUiThread {
            val watermarkText2 = WatermarkText(watermarkTxt).setPositionX(1.0)
                .setPositionY(1.0)
                .setRotation(40.0)
                .setTextAlpha(255)
                .setTextSize(6.0)
                .setTextColor(Color.WHITE)
                .setTextShadow(0.05f, 2f, 2f, Color.BLUE)
            WatermarkBuilder.create(this@CheckInActivity, bmp)
                .loadWatermarkText(watermarkText2)
                .setTileMode(true).watermark.setToImageView(iVphoto)
        }
    }

    private fun displayResult(msg: String) {
        runOnUiThread(Runnable {
            resultText!!.text = msg
            if (invokCount == 0) {
                return@Runnable
            }
            resultText.append("\nInvok $invokCount times\n")
            resultText.append("Total Consume " + (totalTime / 1000f) + " s\n")
            resultText.append("Average Consume " + (totalTime / invokCount) + " ms\n")
        })
    }

    private fun jsonFormat(s: String?): String {
        var level = 0
        val jsonForMatStr = StringBuffer()
        for (index in 0 until s!!.length) {
            //获取s中的每个字符
            val c = s[index]
            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr[jsonForMatStr.length - 1]) {
                jsonForMatStr.append(getLevelStr(level))
            }
            when (c) {
                '{', '[' -> {
                    jsonForMatStr.append(c.toString() + "\n")
                    level++
                }
                ',' -> jsonForMatStr.append(c.toString() + "\n")
                '}', ']' -> {
                    jsonForMatStr.append("\n")
                    level--
                    jsonForMatStr.append(getLevelStr(level))
                    jsonForMatStr.append(c)
                }
                else -> jsonForMatStr.append(c)
            }
        }
        return jsonForMatStr.toString()
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun d() {
        val job: Runnable = object : Runnable {
            var _read = false
            override fun run() {
                try {
                    aidlIcCard!!.open()
                    if (aidlIcCard!!.status()
                            .toInt() == 1) {
                        if (!_read) {
                            _read = true
                            runOnUiThread {
                                try {
                                    if (!(this@CheckInActivity).isFinishing) {
                                        try {
                                            mLoading!!.show()
                                        } catch (e: BadTokenException) {
                                            Log.e("WindowManagerBad ", e.toString())
                                        }
                                    }
                                    iVphoto!!.setImageBitmap(null)
                                    time1 = System.currentTimeMillis()
                                    timestart = time1
                                    aidlIdCardTha!!.stopSearch()
                                    aidlIdCardTha!!.searchIDCardInfo(6000, object : ThaiInfoListerner.Stub() {
                                        @Throws(RemoteException::class)
                                        override fun onResult(i: Int, s: String) {
                                            Log.e("DATA", "onResult : $s")
                                            val end = System.currentTimeMillis()
                                            val b = ((end - time1) / 1000).toInt()
                                            val c = (((end - time1) / 100) % 10).toInt()
                                            showInfo(jsonFormat(s), ("$b.$c"))
                                            mediaPlayer!!.start()
                                            mLoading!!.dismiss()
                                        }
                                    })
                                    Handler().postDelayed({ searchPhoto() }, 2000)
//                                        aidlIdCardTha.searchIDCardPhoto(6000, new ThaiPhotoListerner.Stub() {
//                                            @Override
//                                            public void onResult(int i, Bitmap bitmap) throws RemoteException {
//                                                Log.e("DATA", "onResult photo");
//                                                Bitmap rebmp = Bitmap.createScaledBitmap(bitmap, 85, 100, false);
//                                                showPhoto(rebmp);
//                                            }
//                                        });
                                } catch (e: RemoteException) {
                                    Log.e("DATA info", "RemoteException")
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        _read = false
                        runOnUiThread { mLoading!!.dismiss() }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    if (mLoading != null && mLoading!!.isShowing) {
                        mLoading!!.dismiss()
                    }
                }
            }
        }
        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS)
    }

    private fun searchPhoto() {
        try {
            Log.e("DATA", "searchPhoto")
            aidlIdCardTha!!.stopSearch()
            aidlIdCardTha!!.searchIDCardPhoto(6000, object : ThaiPhotoListerner.Stub() {
                @Throws(RemoteException::class)
                override fun onResult(i: Int, bitmap: Bitmap) {
                    showPhoto(bitmap)
                }
            })
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun c() {
        edtidcard!!.setText("")
        edtnameTH!!.setText("")
        edtidcard!!.setText("")
        iVphoto!!.setImageBitmap(null)
        iVphoto!!.destroyDrawingCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduledExecutor.shutdownNow()
        if (aidlIcCard != null) {
            try {
                aidlIcCard!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (magCard != null) {
            scheduledExecutor.shutdown()
            magCard = null
        }

    }

    @Throws(InterruptedException::class, ExecutionException::class)
    fun dd() {
        val job: Runnable = object : Runnable {
            override fun run() {
                try {
                    magCard!!.open()
                    x()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            private fun c() {
                runOnUiThread { edtnameTH!!.setText("") }
            }

            private fun hextostring(_hex: String): String? {
                try {
                    val bytes = HexUtil.hexStringToByte(_hex)
                    return String(bytes, StandardCharsets.UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    return null
                }
            }

            fun x() {
                try {
                    magCard!!.swipeCard(30000, object : AidlMagCardListener.Stub() {
                        @Throws(RemoteException::class)
                        override fun onSwipeCardTimeout() {
                            Log.e("SWIPE", "time out")
                        }

                        @Throws(RemoteException::class)
                        override fun onSwipeCardSuccess(arg0: TrackData) {
                            var _name = hextostring(arg0.firstTrackData)
                            _name = _name!!.replace(" ", "")
                            _name = _name.replace("^", "")
                            val _xname = _name.split("\\$".toRegex())
                                    .toTypedArray()
                            val _second = hextostring(arg0.secondTrackData)!!.substring(6, hextostring(arg0.secondTrackData)!!.length)
                                    .split("=".toRegex())
                                    .toTypedArray()
                            runOnUiThread {
                                val _x = _second[0].toUpperCase()
                                        .toCharArray()
                                edtidcard!!.setText(_x[0].toString() + "" + _x[1] + _x[2] + _x[3] + _x[4] + "" + _x[5] + _x[6] + _x[7] + _x[8] + _x[9] + "" + _x[10] + _x[11] + "" + _x[12])
                                edtnameTH!!.setText(_xname.get(2) + " " + _xname[1] + " " + _xname[0])
                            }
                        }

                        @Throws(RemoteException::class)
                        override fun onSwipeCardFail() {
                            Log.e("SWIPE", "ERROR1")
                            runOnUiThread(Runnable {
                                Toast.makeText(this@CheckInActivity, "กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG)
                                        .show()
                            })
                        }

                        @Throws(RemoteException::class)
                        override fun onSwipeCardException(arg0: Int) {
                            Log.e("SWIPE", "Exception")
                        }

                        @Throws(RemoteException::class)
                        override fun onCancelSwipeCard() {
                            Log.e("SWIPE", "Cancel")
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS)
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
    private fun printP2(data: CheckInResponseModel) {
        val square = BitmapFactory.decodeResource(resources, R.drawable.square)
        setHeight(0x11)
        sunmiPrinterService!!.clearBuffer()
        sunmiPrinterService!!.enterPrinterBuffer(true)
        val bitmap = createImageFromQRCode(data.contact_code)
        sunmiPrinterService!!.printText("\n        ", innerResultCallbcak)
        sunmiPrinterService!!.printBitmap(resizeBitmap(PreferenceUtils.getBitmapLogo()), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\nบริษัท : " + PreferenceUtils.getCompanyName() +
                "\nชื่อ-นามสกุล : " + data.fullname.replace(" ", " ") +
                "\nเลขบัตรประขาชน : " + data.idcard +
                "\nทะเบียนรถ : " + data.vehicle_id +
                "\nติดต่อ : " + data.person_contact +
                "\nรายละเอียด : " + data.department +
                "\nเวลาเข้า : " + data.chcekin_time, innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\n" + PreferenceUtils.getCompanyNote(), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n      ", innerResultCallbcak)
        sunmiPrinterService!!.printBitmap(bitmap, innerResultCallbcak)
        sunmiPrinterService!!.printText("\n        " + data.contact_code, innerResultCallbcak)

        sunmiPrinterService!!.printBitmap(resizeBitmap(square), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\n\n\n\n\n\n\n ", innerResultCallbcak)
        sunmiPrinterService!!.commitPrinterBuffer()
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

    private fun print(data: CheckInResponseModel) {
        try {
            val bitmap = createImageFromQRCode(data.contact_code)
            val signature = PreferenceUtils.getSignature()
            val textList: MutableList<PrinterParams> = ArrayList()
            var printerParams = PrinterParams()
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.dataType = PrinterParams.DATATYPE.IMAGE
            printerParams.lineHeight = 200
            printerParams.bitmap = PreferenceUtils.getBitmapLogo()
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\n\nบริษัท : " + PreferenceUtils.getCompanyName()
                .replace(" ", " ")
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nชื่อ-นามสกุล : " + data.fullname.replace(" ", " ")
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nเลขบัตรประขาชน : " + data.idcard
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nทะเบียนรถ : " + data.vehicle_id
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nจากบริษัท : " + data.from
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nผู้ที่ขอพบ : " + data.person_contact
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nติดต่อแผนก : " + data.department
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nวัตถุประสงค์ : " + data.objective_type.replace(" ", " ")
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nอุณหภูมิ : " + data.temperature
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = "\nเวลาเข้า : " + data.chcekin_time
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.dataType = PrinterParams.DATATYPE.IMAGE
            printerParams.lineHeight = 200
            printerParams.bitmap = bitmap
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.textSize = 24
            printerParams.text = data.contact_code
            textList.add(printerParams)
            for (i in signature.indices) {
                printerParams = PrinterParams()
                printerParams.align = PrinterParams.ALIGN.CENTER
                printerParams.textSize = 24
                printerParams.text = "\n\n\n____________________________"
                textList.add(printerParams)
                printerParams = PrinterParams()
                printerParams.align = PrinterParams.ALIGN.CENTER
                printerParams.textSize = 24
                printerParams.text = "\n" + signature.get(i)
                    .getname()
                textList.add(printerParams)
            }
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.textSize = 24
            printerParams.text = "\n\n" + PreferenceUtils.getCompanyNote()
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.textSize = 24
            printerParams.text = "\n\n\n\n\n"
            textList.add(printerParams)
            printDev!!.printDatas(textList, callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkCard() {
        try {
            val allType = (AidlConstants.CardType.MAGNETIC.getValue() or AidlConstants.CardType.IC.value)
            WalkinApplication.mReadCardOptV2.checkCard(allType, mCheckCardCallback2, 60)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mCheckCardCallback2: CheckCardCallbackV2 = object : CheckCardCallbackV2Wrapper() {
        /**
         * Find magnetic card
         *
         * @param info return data，contain the following keys:
         * <br></br>cardType: card type (int)
         * <br></br>TRACK1: track 1 data (String)
         * <br></br>TRACK2: track 2 data (String)
         * <br></br>TRACK3: track 3 data (String)
         * <br></br>track1ErrorCode: track 1 error code (int)
         * <br></br>track2ErrorCode: track 2 error code (int)
         * <br></br>track3ErrorCode: track 3 error code (int)
         * <br></br> track error code is one of the following values:
         *
         *  * 0 - No error
         *  * -1 - Track has no data
         *  * -2 - Track parity check error
         *  * -3 - Track LRC check error
         *
         */
        @Throws(RemoteException::class)
        override fun findMagCard(info: Bundle) {
            LogUtil.e(TAG, "panya findMagCard")
            handleResult(info)
        }

        @Throws(RemoteException::class)
        override fun findICCard(atr: String) {
            LogUtil.e(TAG, "panya findICCard,atr:$atr")
            sendEdpu()
        }

        @Throws(RemoteException::class)
        override fun findRFCard(uuid: String) {
            LogUtil.e(TAG, "panya findRFCard" + uuid)
        }

        @Throws(RemoteException::class)
        override fun onError(code: Int, message: String) {
            val error = "onError:$message -- $code"
            LogUtil.e(TAG, "panya " + error)
        }

        override fun findICCardEx(info: Bundle?) {
            LogUtil.e(TAG, "panya findICCardEx" + info)
        }

        override fun findRFCardEx(info: Bundle?) {
            LogUtil.e(TAG, "panya findRFCardEx" + info)
        }

        override fun onErrorEx(info: Bundle?) {
            LogUtil.e(TAG, "panya onErrorEx" + info)
        }
    }

    fun sendEdpu() {
        val data = sendAPDUkOnClick()
        runOnUiThread {
            edtnameTH!!.setText(data.nameTH)
            edtidcard!!.setText(data.id)
            edtaddress!!.setText(data.address?.replace("#", " "))
            tVgender!!.text = data.gender
            tVbirth!!.text = data.birthDate
        }
    }

    fun sendAPDUkOnClick(): UserModel {
        val version = transmitApduCmd(_req_version).trim { it <= ' ' }
        val userModel = UserModel()
        var base = ""

        if (version.startsWith("0003")) {
            base = "80B0"
            userModel.id = transmitApduCmd("80b0000402000d").substring(0, 12)
            val data = transmitApduCmd("80b000D902001D")
            val _year_th: String = data.substring(0, 4)
            val _month_th: String = data.substring(4, 6)
            val _day: String = data.substring(6, 8)
            val sex: String = data.substring(8, 9)
            userModel.birthDate = _day + _month_th + _year_th
            if ("1" == sex) {
                userModel.gender = "M"
            } else {
                userModel.gender = "F"
            }
            userModel.nameTH = transmitApduCmd("80b00011020064").replace("#", " ").substring(0, 50)
            userModel.nameEN = transmitApduCmd("80b00075020064").replace("#", " ").substring(0, 50)
            userModel.address = transmitApduCmd("80b015790200A0").replace("#", " ").substring(0, 50)
        } else {
            base = "80B1"
            //cid //offset 4 len:13
            userModel.id = transmitApduCmd("80b1000402000d").substring(0, 12)
            val data = transmitApduCmd("80b100D902001D")
            val _year_th: String = data.substring(0, 4)
            val _month_th: String = data.substring(4, 6)
            val _day: String = data.substring(6, 8)
            val sex: String = data.substring(8, 9)
            userModel.birthDate = _day + _month_th + _year_th
            if ("1" == sex) {
                userModel.gender = "M"
            } else {
                userModel.gender = "F"
            }
            userModel.nameTH = transmitApduCmd("80b10011020064").replace("#", " ").substring(0, 50)
            userModel.nameEN = transmitApduCmd("80b10075020064").replace("#", " ").substring(0, 50)
            userModel.address = transmitApduCmd("80b00004020096").replace("#", " ").substring(0, 50)
        }

        Observable.fromCallable {
                sendCommandForPhoto(base)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val bmp = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes!!.size)
                showPhoto(bmp)
            }, { error ->
                error?.message?.let { Log.e("error", error?.message) }
                handleResult2()
            })

        return userModel
    }

    private fun getThaiIdDataByte(cmd: String): ByteArray {
        val recv = ByteArray(260)
        WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), baCommandAPDU, ByteArray(260))
        val cmdByte = ByteUtil.hexStringToByte(cmd)
        WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), cmdByte, ByteArray(260))
        WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(),
                ByteUtil.hexStringToByte("00c00000" + cmd.substring(12)),
                recv)
        return recv
    }

    private fun r(data: ByteArray?): ByteArray? {
        if (data == null || data.size < 2) {
            throw RuntimeException("read IC card error.")
        }
        return Arrays.copyOfRange(data, 0, data.size - 2)
    }

    private fun transmitApduCmd(cmd: String): String {
        LogUtil.e(TAG, "panya transmitApduCmd" + cmd)

        val send = ByteUtil.hexStr2Bytes(cmd)
        val recv = ByteArray(260)
        var data = ""
        try {
            WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), baCommandAPDU, ByteArray(260))
            val len = WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), send, recv)
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:$len")
                Toast.makeText(this@CheckInActivity, "Read data failed", Toast.LENGTH_LONG).show()
            } else {
//                LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(*recv))
                data = String(recv, _UTF8_CHARSET)
//                LogUtil.e(TAG, "transmitApdu success,recv x :" + data)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return data
    }


    private fun transmitApduCmdPhotoLength(cmd: String): ByteArray? {
        val send = ByteUtil.hexStr2Bytes(cmd)
        val recv = ByteArray(260)
        try {
//            WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), baCommandAPDU, ByteArray(260))
            val len = WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), send, recv)
            if (len < 0) {
//                LogUtil.d(TAG, "transmitApduPhoto failed,code:$len")
                Toast.makeText(this@CheckInActivity, "Read data failed", Toast.LENGTH_LONG).show()
            } else {
//                val x = ByteUtil.bytes2HexStr(*recv)
//                LogUtil.d(TAG, "transmitApdu success,recv:" + x)

            }
        } catch (e: RemoteException) {

            e.printStackTrace()
        }
        return recv
    }

    private fun transmitApduCmdPhoto(cmd: String, hexSize: String): ByteArray? {
        val send = ByteUtil.hexStr2Bytes(cmd)
        val recv = ByteArray(260)
        val size = Integer.parseInt(hexSize, 16)
        var xx = ByteArray(size)
        try {
            val len = WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), send, recv)
            if (len < 0) {
                LogUtil.e(TAG, "panya failed,code:$len")
                Toast.makeText(this@CheckInActivity, "Read data failed", Toast.LENGTH_LONG).show()
            } else {
                val x = ByteUtil.bytes2HexStr(*recv)
                val x2 = x.substring(0, size * 2)
                xx = ByteUtil.hexStr2Bytes(x2)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return xx
    }

    private fun handleResult2() {
        if (isFinishing) {
            return
        }
        handler.post(object : Runnable {
            override fun run() {
                // 继续检卡
                if (!isFinishing) {
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            checkCard()
                        }
                    }, 500)
                }
            }
        })
    }


    private fun handleResult(bundle: Bundle?) {
        if (isFinishing) {
            return
        }
        handler.post(object : Runnable {
            override fun run() {
                if (bundle == null) {
//                showResult(false, "", "", "");
                    return
                }
                val track1 = SunmiUtility.null2String(bundle.getString("TRACK1"))
                val track2 = SunmiUtility.null2String(bundle.getString("TRACK2"))
                val track3 = SunmiUtility.null2String(bundle.getString("TRACK3"))
                //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
                val code1 = bundle.getInt("track1ErrorCode")
                val code2 = bundle.getInt("track2ErrorCode")
                val code3 = bundle.getInt("track3ErrorCode")
                LogUtil.e(TAG,
                        String.format(Locale.getDefault(),
                                "track1ErrorCode:%d,track1:%s\ntrack2ErrorCode:%d,track2:%s\ntrack3ErrorCode:%d,track3:%s",
                                code1,
                                track1,
                                code2,
                                track2,
                                code3,
                                track3))
                if ((code1 != 0 && code1 != -1) || (code2 != 0 && code2 != -1) || (code3 != 0 && code3 != -1)) {
//                showResult(false, track1, track2, track3);
                    Log.e("data", track1 + "" + track2 + "" + track3)
                } else {
                    Log.e("data2", track1 + "////" + track2 + "/////" + track3)
                    runOnUiThread {
                        val name = track1.replace("^", "")
                                .trim()
                                .split("$")
                        edtnameTH!!.setText(name.reversed()
                                .toString()
                                .replace(",", "")
                                .replace("[", "")
                                .replace("]", ""))
                        val list = track2.split("=")
                        val card = list.first()
                        val idcard = card.substring(card.length - 13, card.length)
                        edtidcard!!.setText(idcard)
                        val birthDate = list.get(1)
                        tVbirth!!.text = birthDate.substring(birthDate.length - 2) + "/" + birthDate.substring(birthDate.length - 4,
                                birthDate.length - 2) + "/" + birthDate.substring(
                                birthDate.length - 8,
                                birthDate.length - 4)
                    }
                }
                // 继续检卡
                if (!isFinishing) {
                    handler.postDelayed(object : Runnable {
                        override fun run() {
                            checkCard()
                        }
                    }, 500)
                }
            }
        })
    }

    private fun sendCommandForPhoto(base: String) {
        try {
            //base  offset  fix    length
            WalkinApplication.mReadCardOptV2.transmitApdu(AidlConstants.CardType.IC.getValue(), baCommandAPDU, ByteArray(260))
            val length = r(transmitApduCmdPhotoLength(base + "0179020002"))
            val iLength = ByteUtil.bytes2short(length)
            val out = ByteArrayOutputStream(iLength)//137B
            LogUtil.e(TAG, "panya iLength $iLength")
            val cnt = iLength / 250
            val lastData = iLength % 250
            for (i in 0 until cnt + 1) {
                val xwd: Int
                val xof = i * 250 + 379
                xwd = if (i == cnt) lastData else 250
                if (xwd == 0) {
                    break
                }
                val sp2 = e(xof shr 8 and 0xff)
                val sp3 = e(xof and 0xff)
                val sp6 = e(xwd and 0xff)
                val cmdx = base + sp2 + sp3 + "0200" + sp6
                val _xx = transmitApduCmdPhoto(base + sp2 + sp3 + "0200" + sp6, cmdx.substring(cmdx.length - 2))
                if (_xx != null) {
                    out.write(_xx, 0, _xx.size)
                } else {
                    break
                }
            }
            photoBytes = out.toByteArray()
            out.close()
        } catch (e: Throwable) {
            e.printStackTrace()
            photoBytes = null
        }
    }

    private fun e(value: Int): String {
        //整形转化为16进制字符串，同时看一下长度，长度不是偶数，那么前面补0
        var hex = Integer.toHexString(value)
        hex = if (hex.length % 2 == 1) "0$hex" else hex
        return hex.toUpperCase()
    }

    companion object {
        private val CAMERA_PERM_CODE = 101
        private val CAMERA_USER_CODE = 102
        private val CAMERA_CAR_CODE = 103
        private val CAMERA_CARD_CODE = 104
        var timestart: Long = 0
        private fun getLevelStr(level: Int): String {
            val levelStr = StringBuffer()
            for (levelI in 0 until level) {
                levelStr.append("\t")
            }
            return levelStr.toString()
        }

        fun convert(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }
}