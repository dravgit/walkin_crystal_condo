package com.crystal.walkin.condo

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.crystal.walkin.R
import com.crystal.walkin.condo.models.PartialVisitorResponseModel
import com.crystal.walkin.condo.models.WalkInErrorModel
import com.crystal.walkin.condo.utils.NetworkUtil
import kotlinx.android.synthetic.main.activity_detail.*


class TotalCheckOutActivity : BaseActivity() {
    override fun onPrintDeviceConnected(manager: AidlDeviceManager?) {
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    override fun onDeviceConnectedSwipe(manager: AidlDeviceManager?) {
    }

    override fun showMessage(str: String?, black: Int) {
    }

    var statusCode = true
    lateinit var adapter : DetailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        adapter = DetailAdapter(this@TotalCheckOutActivity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        tVdetail.setText("จำนวนผู้ออกจากตึก")
        btnRefresh.setOnClickListener {
            loadData()
        }
        loadData()
    }

    fun loadData() {
        NetworkUtil.getListByType(NetworkUtil.STATUS_TYPE_OUT, object : NetworkUtil.Companion.NetworkLisener<List<PartialVisitorResponseModel>> {
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