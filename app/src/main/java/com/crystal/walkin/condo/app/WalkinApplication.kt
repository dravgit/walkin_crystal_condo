package com.crystal.walkin.condo.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.IBinder
import com.crystal.walkin.condo.Constant
import com.crystal.walkin.condo.utils.PreferenceUtils
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import com.sunmi.scanner.IScanInterface
import sunmi.sunmiui.utils.LogUtil

class WalkinApplication: Application() {
    var mBasicOptV2 // 获取基础操作模块
            : BasicOptV2? = null
    var mReadCardOptV2 // 获取读卡模块
            : ReadCardOptV2? = null
    var mPinPadOptV2 // 获取PinPad操作模块
            : PinPadOptV2? = null
    var mSecurityOptV2 // 获取安全操作模块
            : SecurityOptV2? = null
    var mEMVOptV2 // 获取EMV操作模块
            : EMVOptV2? = null
    var mTaxOptV2 // 获取税控操作模块
            : TaxOptV2? = null
//    var sunmiPrinterService: SunmiPrinterService? = null
    var scanInterface: IScanInterface? = null
    override fun onCreate() {
        super.onCreate()
        PreferenceUtils.init(applicationContext)
        appContext = applicationContext
//        initLocaleLanguage()

        bindPrintService()

        bindScannerService()
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LogUtil.e(Constant.TAG, "onConfigurationChanged")
    }

    fun getContext(): Context? {
        return appContext
    }

    private fun bindPrintService() {
        val value: Any = try {
            InnerPrinterManager.getInstance()
                .bindService(this, innerPrinterCallback)
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
    }


    override fun onTerminate() {
        super.onTerminate()
        upbindPrintService()
    }

    private fun upbindPrintService() {
        try {
            InnerPrinterManager.getInstance()
                .unBindService(this, innerPrinterCallback)
        } catch (e: InnerPrinterException) {
            e.printStackTrace()
        }
    }

    var innerPrinterCallback: InnerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            setPrintService(service)
        }

        override fun onDisconnected() {
//            sunmiPrinterService = null
        }
    }


    fun bindScannerService() {
        val intent = Intent()
        intent.setPackage("com.sunmi.scanner")
        intent.action = "com.sunmi.scanner.IScanInterface"
        bindService(intent, scanConn, BIND_AUTO_CREATE)
    }

    private val scanConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            scanInterface = IScanInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            scanInterface = null
        }
    }

    companion object {
        lateinit var mTaxOptV2: TaxOptV2
        lateinit var mSecurityOptV2: SecurityOptV2
        lateinit var mReadCardOptV2: ReadCardOptV2
        lateinit var mPinPadOptV2: PinPadOptV2
        lateinit var mBasicOptV2: BasicOptV2
        lateinit var mEMVOptV2: EMVOptV2
        lateinit var appContext: Context
        var sunmiPrinterService: SunmiPrinterService? = null

        fun setPrintService(service: SunmiPrinterService) {
            sunmiPrinterService = service
        }
    }
}