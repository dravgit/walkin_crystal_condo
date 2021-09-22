package com.crystal.walkin.condo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import com.crystal.walkin.condo.app.WalkinApplication
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Util() {
    companion object {
        var activityContext: Context? = null
        fun showToast(@StringRes resId : Int) {
            Toast.makeText(WalkinApplication.appContext, WalkinApplication.appContext.getString(resId), Toast.LENGTH_LONG).show()
        }

        fun setContext(context: Context) {
            activityContext = context
        }

        fun toDateFormat(date: String): String {
            Log.e("toDateFormat", date)
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val timeFormatter = SimpleDateFormat("HH:mm")
            val output: String = formatter.format(parser.parse(date))
            val date = toDateTh(output)
            var hour = date.get(Calendar.HOUR_OF_DAY).toString()
            var minute = date.get(Calendar.MINUTE).toString()
            if (hour.length == 1) {
                hour = "0"+hour
            }
            if (minute.length == 1) {
                minute = "0"+minute
            }
            var time = hour + ":" + minute
            Log.e("toDateFormat", "org : "+date +", time : "+ time)

            return ""+date.get(Calendar.DATE)+" "+ getMonth(date)+" "+(date.get(Calendar.YEAR)+543) +" "+ time
        }

        fun getMonth(c: Calendar): String {
            val Months= arrayListOf( "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.",
                                     "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.",
                                     "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
            var month = c.get(Calendar.MONTH)

            return Months[month]
        }

        fun toDateTh(date: String): Calendar {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
            cal.time = sdf.parse(date)
            return cal
        }

        @Throws(WriterException::class)
        fun createImageFromQRCode(message: String?): Bitmap? {
            var bitMatrix: BitMatrix? = null
            bitMatrix = MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, 240, 240)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (i in 0 until height) {
                for (j in 0 until width) {
                    if (bitMatrix[j, i]) {
                        pixels[i * width + j] = -0x1000000
                    } else {
                        pixels[i * width + j] = -0x1
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        }

        fun encodeImg(type: ImageView): String? {
            var encoded: String? = ""
            type.invalidate()
            val drawable = type.drawable as BitmapDrawable
            if (drawable != null && drawable.bitmap != null) {
                val bitmap = drawable.bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
            return encoded
        }
    }
}