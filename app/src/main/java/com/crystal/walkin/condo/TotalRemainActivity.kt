package com.crystal.walkin.condo

import android.os.Bundle
import android.os.RemoteException
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import com.crystal.walkin.R
import com.crystal.walkin.condo.models.PartialVisitorResponseModel
import com.crystal.walkin.condo.models.WalkInErrorModel
import com.crystal.walkin.condo.utils.NetworkUtil
import kotlinx.android.synthetic.main.activity_detail.*

class TotalRemainActivity : BaseActivity() {
    lateinit var adapter : DetailAdapter
    private var printDev: AidlPrinter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        adapter = DetailAdapter(this@TotalRemainActivity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        tVdetail.setText("จำนวนผู้ที่ยังอยู่ในตึก")
        btnRefresh.setOnClickListener {
            loadData()
        }
        loadData()
    }


    override fun onPrintDeviceConnected(manager: AidlDeviceManager?) {
        try {
            printDev = AidlPrinter.Stub.asInterface(manager!!.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV))
            adapter?.let {
                it.setPrinter(printDev)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    override fun onDeviceConnectedSwipe(manager: AidlDeviceManager?) {
    }

    override fun showMessage(str: String?, black: Int) {
    }

        fun loadData() {
        NetworkUtil.getListByType(NetworkUtil.STATUS_TYPE_STAY, object : NetworkUtil.Companion.NetworkLisener<List<PartialVisitorResponseModel>> {
            override fun onResponse(response: List<PartialVisitorResponseModel>) {
                adapter.setListdata(response)
            }

            override fun onError(errorModel: WalkInErrorModel) {
                checkError(errorModel)
            }

            override fun onExpired() {
                loadData()
            }
        })
    }
}