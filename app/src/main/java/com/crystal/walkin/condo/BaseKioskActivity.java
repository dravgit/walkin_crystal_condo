package com.crystal.walkin.condo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.centerm.system.sdk.aidl.IDeviceService;
import com.centerm.system.sdk.aidl.ISystemOperation;
import com.centerm.system.sdk.aidl.SystemFunctionType;

public abstract class BaseKioskActivity extends Activity {

    public static final String SERVICE_ACTION = "com.centerm.smartpos.systemsdk";
    public static final String PACKAGE_NAME = "com.centerm.system.sdk";

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            if (serviceBinder != null) {
                try {
                IDeviceService serviceManager = IDeviceService.Stub
                        .asInterface(serviceBinder);
                ISystemOperation mSystemOperation = ISystemOperation.Stub
                        .asInterface(serviceManager.getSystemOperation());
                    setHomeKeyDisabled(mSystemOperation);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setHomeKeyDisabled(ISystemOperation mSystemOperation) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SystemFunctionType.HOME_KEY, true);
            bundle.putBoolean(SystemFunctionType.FUNCTION_KEY, true);
            bundle.putBoolean(SystemFunctionType.STATUS_BAR_KEY, true);
            bundle.putBoolean(SystemFunctionType.POWER_KEY, true);
            try {
                mSystemOperation.setSystemFunction(bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME);
        intent.setAction(SERVICE_ACTION);
        boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (flag) {
            Log.e("STATUS", "success");
        } else {
            Log.d("STATUS", "fail");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            this.unbindService(conn);
        }catch (Exception e){
            Log.e("Exception","No regis");
        }
    }

}
