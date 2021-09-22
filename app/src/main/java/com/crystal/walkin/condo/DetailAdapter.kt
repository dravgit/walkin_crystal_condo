package com.crystal.walkin.condo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.crystal.walkin.R
import com.crystal.walkin.condo.app.WalkinApplication
import com.crystal.walkin.condo.models.CheckInResponseModel
import com.crystal.walkin.condo.models.PartialVisitorResponseModel
import com.crystal.walkin.condo.models.VisitorResponseModel
import com.crystal.walkin.condo.models.WalkInErrorModel
import com.crystal.walkin.condo.utils.BitmapUtils
import com.crystal.walkin.condo.utils.NetworkUtil.Companion.NetworkLisener
import com.crystal.walkin.condo.utils.NetworkUtil.Companion.searchByOrder
import com.crystal.walkin.condo.utils.PreferenceUtils
import com.crystal.walkin.condo.utils.Util.Companion.createImageFromQRCode
import com.crystal.walkin.condo.utils.Util.Companion.toDateFormat
import com.sunmi.peripheral.printer.InnerResultCallbcak
import com.sunmi.peripheral.printer.SunmiPrinterService
import sunmi.sunmiui.utils.LogUtil
import java.util.*

class DetailAdapter     // RecyclerView recyclerView;
(var context: Context) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {
    private var listdata: List<PartialVisitorResponseModel> = ArrayList()
    private var sunmiPrinterService: SunmiPrinterService? = null
    private var printDev: AidlPrinter? = null
    fun setListdata(listdata: List<PartialVisitorResponseModel>) {
        this.listdata = listdata
        notifyDataSetChanged()
    } 

    fun setPrinter(printDev: AidlPrinter?) {
        this.printDev = printDev
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detailListData = listdata[position]
        holder.tvName.text = detailListData.name
        holder.tvObjective.text = context.getString(R.string.contact_s, detailListData.department)
        holder.tvIn.text = toDateFormat(detailListData.checkin_time)
        if (detailListData.checkout_time.isEmpty()) {
            holder.tvOut.text = context.getString(R.string.stay_in)
            holder.btnReprint.visibility = View.VISIBLE
            holder.btnReprint.tag = detailListData.contact_code
            holder.btnReprint.setOnClickListener { view ->
                val code = view.tag.toString()
                search(code)
            }
        } else {
            holder.btnReprint.visibility = View.GONE
            holder.tvOut.text = toDateFormat(detailListData.checkout_time)
        }
        holder.imageView1.setImageResource(0)
        holder.imageView2.setImageResource(0)
        for (imageModel in detailListData.images) {
            val type = imageModel.type
            if ("1" == type && !imageModel.url.isEmpty()) {
                Glide.with(context) //1
                        .load(imageModel.url)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .skipMemoryCache(true) //2
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .into(holder.imageView1)
            } else if ("2" == type && !imageModel.url.isEmpty()) {
                Glide.with(context) //1
                        .load(imageModel.url)
                        .placeholder(R.drawable.ic_car)
                        .error(R.drawable.ic_car)
                        .skipMemoryCache(true) //2
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .into(holder.imageView2)
            } else if ("4" == type && !imageModel.url.isEmpty()) {
                Glide.with(context) //1
                        .load(imageModel.url)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .skipMemoryCache(true) //2
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                        .into(holder.imageView1)
            }
        }
        holder.relativeLayout.setOnClickListener {
            //                Toast.makeText(view.getContext(),"click on item: "+ detailListData.getDescription(),Toast.LENGTH_LONG).show();
        }
    }

    private fun search(code: String) {
        searchByOrder(code, object : NetworkLisener<VisitorResponseModel> {
            override fun onResponse(response: VisitorResponseModel) {
                printP2(response)
            }

            override fun onError(errorModel: WalkInErrorModel) {
                Log.e("error", errorModel.msg)
            }

            override fun onExpired() {
                search(code)
            }
        }, VisitorResponseModel::class.java)
    }

    private fun print(data: VisitorResponseModel) {
        try {
            val bitmap = createImageFromQRCode(data.contact_code)
            val signature = PreferenceUtils.getSignature()
            val textList: MutableList<PrinterParams> = ArrayList()
            var printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.dataType = PrinterParams.DATATYPE.IMAGE
            printerParams.lineHeight = 200
            printerParams.bitmap = PreferenceUtils.getBitmapLogo()
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                
                บริษัท : ${PreferenceUtils.getCompanyName().replace(" ", " ")}
                """.trimIndent()
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                ชื่อ-นามสกุล : ${data.name().replace(" ", " ")}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                เลขบัตรประขาชน : ${data.idcard}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                ทะเบียนรถ : ${data.vehicle_id}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                จากบริษัท : ${data.from}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                ผู้ที่ขอพบ : ${data.person_contact}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                ต่อต่อแผนก : ${data.department}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                วัตถุประสงค์ : ${data.objective_type.replace(" ", " ")}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                อุณหภูมิ : ${data.temperature}
                """.trimIndent()
            printerParams.isBold = true
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.LEFT
            printerParams.textSize = 24
            printerParams.text = """
                
                เวลาเข้า : ${data.checkin_time}
                """.trimIndent()
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
                printerParams.text = """
                    
                    ${signature[i].getname()}
                    """.trimIndent()
                textList.add(printerParams)
            }
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.textSize = 24
            printerParams.text = """
                
                
                ${PreferenceUtils.getCompanyNote()}
                """.trimIndent()
            textList.add(printerParams)
            printerParams = PrinterParams()
            printerParams.align = PrinterParams.ALIGN.CENTER
            printerParams.textSize = 24
            printerParams.text = "\n\n\n\n\n"
            textList.add(printerParams)
            printDev!!.printDatas(textList, object : AidlPrinterStateChangeListener.Stub() {
                @Throws(RemoteException::class)
                override fun onPrintFinish() {
                    Log.e("panya", "onPrintFinish")
                }

                @Throws(RemoteException::class)
                override fun onPrintError(i: Int) {
                    Log.e("panya", "onPrintError")
                }

                @Throws(RemoteException::class)
                override fun onPrintOutOfPaper() {
                    Log.e("panya", "onPrintOutOfPaper")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private fun printP2(data: VisitorResponseModel) {
//        setHeight(0x11)
//        sunmiPrinterService!!.clearBuffer()
//        sunmiPrinterService!!.enterPrinterBuffer(true)
//        val bitmap = createImageFromQRCode(data.contact_code)
//        val signature = PreferenceUtils.getSignature()
//        sunmiPrinterService!!.printText("\n      ", innerResultCallbcak)
//        sunmiPrinterService!!.printBitmap(resizeBitmap(PreferenceUtils.getBitmapLogo()), innerResultCallbcak)
//
//        sunmiPrinterService!!.printText("\n\nบริษัท : " + PreferenceUtils.getCompanyName() + "\nชื่อ-นามสกุล : " + data.name.replace(" ",
//                " ") + "\nเลขบัตรประขาชน : " + data.idcard + "\nทะเบียนรถ : " + data.vehicle_id + "\nจากบริษัท : " + data.from + "\nผู้ที่ขอพบ : " + data.person_contact + "\nติดต่อแผนก : " + data.department + "\nวัตถุประสงค์ : " + data.objective_type.replace(
//                " ",
//                " ") + "\nอุณหภูมิ : " + data.temperature + "\nเวลาเข้า : " + data.checkin_time, innerResultCallbcak)
//        sunmiPrinterService!!.printText("\n      ", innerResultCallbcak)
//        sunmiPrinterService!!.printBitmap(bitmap, innerResultCallbcak)
//        sunmiPrinterService!!.printText("\n        " + data.contact_code, innerResultCallbcak)
//
//        for (i in signature.indices) {
//            sunmiPrinterService!!.printText("\n\n\n____________________________" + "\n        " + signature.get(i)
//                    .getname(), innerResultCallbcak)
//        }
//
//        sunmiPrinterService!!.printText("\n\n" + PreferenceUtils.getCompanyNote() + "\n\n\n\n\n", innerResultCallbcak)
//
//        sunmiPrinterService!!.printText("\n\n ", innerResultCallbcak)
//        sunmiPrinterService!!.printText("\n\n ", innerResultCallbcak)
//        sunmiPrinterService!!.commitPrinterBuffer()
//    }

    private fun printP2(data: VisitorResponseModel) {
        val square = BitmapFactory.decodeResource(context.resources, R.drawable.square)
        setHeight(0x11)
        sunmiPrinterService!!.clearBuffer()
        sunmiPrinterService!!.enterPrinterBuffer(true)
        val bitmap = createImageFromQRCode(data.contact_code)
        sunmiPrinterService!!.printText("\n        ", innerResultCallbcak)
        sunmiPrinterService!!.printBitmap(resizeBitmap(PreferenceUtils.getBitmapLogo()), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\nบริษัท : " + PreferenceUtils.getCompanyName() +
                "\nชื่อ-นามสกุล : " + data.name.replace(" ", " ") +
                "\nเลขบัตรประขาชน : " + data.idcard +
                "\nทะเบียนรถ : " + data.vehicle_id +
                "\nติดต่อ : " + data.person_contact +
                "\nรายละเอียด : " + data.department +
                "\nเวลาเข้า : " + data.checkin_time, innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\n" + PreferenceUtils.getCompanyNote(), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n      ", innerResultCallbcak)
        sunmiPrinterService!!.printBitmap(bitmap, innerResultCallbcak)
        sunmiPrinterService!!.printText("\n        " + data.contact_code, innerResultCallbcak)

        sunmiPrinterService!!.printBitmap(resizeBitmap(square), innerResultCallbcak)

        sunmiPrinterService!!.printText("\n\n\n\n\n\n\n\n ", innerResultCallbcak)
        sunmiPrinterService!!.commitPrinterBuffer()
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

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView1: ImageView
        var imageView2: ImageView
        var tvName: TextView
        var tvObjective: TextView
        var tvIn: TextView
        var tvOut: TextView
        var relativeLayout: RelativeLayout
        var btnReprint: Button

        init {
            imageView1 = itemView.findViewById<View>(R.id.iv_1) as ImageView
            imageView2 = itemView.findViewById<View>(R.id.iv_2) as ImageView
            tvName = itemView.findViewById<View>(R.id.tv_name) as TextView
            tvObjective = itemView.findViewById<View>(R.id.tv_objective) as TextView
            tvIn = itemView.findViewById<View>(R.id.tv_in) as TextView
            tvOut = itemView.findViewById<View>(R.id.tv_out) as TextView
            btnReprint = itemView.findViewById<View>(R.id.btnReprint) as Button
            relativeLayout = itemView.findViewById<View>(R.id.relativeLayout) as RelativeLayout
        }
    }
}