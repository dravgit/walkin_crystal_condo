package com.crystal.walkin.condo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.crystal.walkin.condo.app.WalkinApplication;
import com.crystal.walkin.condo.models.WalkInErrorModel;
import com.crystal.walkin.condo.utils.NetworkUtil;
import com.crystal.walkin.condo.utils.PreferenceUtils;
import com.crystal.walkin.condo.utils.Util;

import sunmi.paylib.SunmiPayKernel;
import sunmi.sunmiui.utils.LogUtil;

public abstract class BaseActivity extends BaseKioskActivity {

    private SunmiPayKernel mSMPayKernel;

    private boolean isDisConnectService = true;
    protected AidlDeviceManager manager = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService();
        Util.Companion.setContext(this);
        connectPayService();
    }

    private void connectPayService() {
        mSMPayKernel = SunmiPayKernel.getInstance();
        mSMPayKernel.initPaySDK(this, mConnectCallback);
    }


    private SunmiPayKernel.ConnectCallback mConnectCallback = new SunmiPayKernel.ConnectCallback() {

        @Override
        public void onConnectPaySDK() {
            LogUtil.e(Constant.TAG, "onConnectPaySDK");
            try {
                WalkinApplication.mEMVOptV2 = mSMPayKernel.mEMVOptV2;
                WalkinApplication.mBasicOptV2 = mSMPayKernel.mBasicOptV2;
                WalkinApplication.mPinPadOptV2 = mSMPayKernel.mPinPadOptV2;
                WalkinApplication.mReadCardOptV2 = mSMPayKernel.mReadCardOptV2;
                WalkinApplication.mSecurityOptV2 = mSMPayKernel.mSecurityOptV2;
                WalkinApplication.mTaxOptV2 = mSMPayKernel.mTaxOptV2;
                isDisConnectService = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnectPaySDK() {
            LogUtil.e(Constant.TAG, "onDisconnectPaySDK");
            isDisConnectService = true;
//            showToast(R.string.connect_fail);
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        if (mSMPayKernel != null) {
            mSMPayKernel.destroyPaySDK();
        }
    }

    private Bitmap rotateImage(Bitmap source, Float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true
        );
    }

    public Bitmap rotageBitmap(Bitmap bitmap) {
        Bitmap bitmap2;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            bitmap2 = rotateImage(bitmap, 90f);
        }else {
            bitmap2 = bitmap;
        }
        return bitmap2;
    }

    public void checkError(WalkInErrorModel walkInErrorModel) {
        if (walkInErrorModel.getError_code().equals(String.valueOf(NetworkUtil.Companion.getSTATUS_CODE_INVALID_PASSWORD()))) {
            PreferenceUtils.setLoginFail();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.centerm.smartposservice");
        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        Intent intent1 = new Intent();
        intent1.setPackage("com.centerm.smartposservice");
        intent1.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        bindService(intent1, conn1, Context.BIND_AUTO_CREATE);

        intent = new Intent();
        intent.setPackage("com.centerm.smartposservice");
        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        bindService(intent, conn2, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        unbindService(conn);
        unbindService(conn1);
        unbindService(conn2);
    }

    public ServiceConnection conn1 = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            manager = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            manager = AidlDeviceManager.Stub.asInterface(service);
            if (null != manager) {
                onDeviceConnectedSwipe(manager);
            }
        }
    };


    protected ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            manager = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            manager = AidlDeviceManager.Stub.asInterface(service);
            if (null != manager) {
                onDeviceConnected(manager);
            }
        }
    };

    public ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            manager = AidlDeviceManager.Stub.asInterface(service);
            com.centerm.centermposoversealib.util.LogUtil.print("success1");
            com.centerm.centermposoversealib.util.LogUtil.print("manager1 = " + manager);
            if (null != manager) {
                onPrintDeviceConnected(manager);
            }
        }
    };

    protected void log(String log) {
        Log.i("Centerm", log);
    }
    protected abstract void onPrintDeviceConnected(AidlDeviceManager manager);
    public abstract void onDeviceConnected(AidlDeviceManager deviceManager);
    public abstract void onDeviceConnectedSwipe(AidlDeviceManager manager);

    protected abstract void showMessage(String str, int black);
}
