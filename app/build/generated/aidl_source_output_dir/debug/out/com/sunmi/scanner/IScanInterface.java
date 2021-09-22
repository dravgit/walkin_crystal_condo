/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.sunmi.scanner;
// Declare any non-default types here with import statements

public interface IScanInterface extends android.os.IInterface
{
  /** Default implementation for IScanInterface. */
  public static class Default implements com.sunmi.scanner.IScanInterface
  {
    /**
         * 触发开始与停止扫码
         * key.getAction()==KeyEvent.ACTION_UP 触发开始扫码
         * key.getAction()==KeyEvent.ACTION_DWON 触发停止扫码
         */
    @Override public void sendKeyEvent(android.view.KeyEvent key) throws android.os.RemoteException
    {
    }
    /**
         * 触发开始扫码
         */
    @Override public void scan() throws android.os.RemoteException
    {
    }
    /**
         * 触发停止扫码
         */
    @Override public void stop() throws android.os.RemoteException
    {
    }
    /**
         * 获取扫码头类型
         * 100-->NONE
         * 101-->P2Lite
         * 102-->l2-newland
         * 103-->l2-zabra
         */
    @Override public int getScannerModel() throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.sunmi.scanner.IScanInterface
  {
    private static final java.lang.String DESCRIPTOR = "com.sunmi.scanner.IScanInterface";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.sunmi.scanner.IScanInterface interface,
     * generating a proxy if needed.
     */
    public static com.sunmi.scanner.IScanInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.sunmi.scanner.IScanInterface))) {
        return ((com.sunmi.scanner.IScanInterface)iin);
      }
      return new com.sunmi.scanner.IScanInterface.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_sendKeyEvent:
        {
          data.enforceInterface(descriptor);
          android.view.KeyEvent _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.view.KeyEvent.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.sendKeyEvent(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_scan:
        {
          data.enforceInterface(descriptor);
          this.scan();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_stop:
        {
          data.enforceInterface(descriptor);
          this.stop();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_getScannerModel:
        {
          data.enforceInterface(descriptor);
          int _result = this.getScannerModel();
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.sunmi.scanner.IScanInterface
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * 触发开始与停止扫码
           * key.getAction()==KeyEvent.ACTION_UP 触发开始扫码
           * key.getAction()==KeyEvent.ACTION_DWON 触发停止扫码
           */
      @Override public void sendKeyEvent(android.view.KeyEvent key) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((key!=null)) {
            _data.writeInt(1);
            key.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_sendKeyEvent, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().sendKeyEvent(key);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * 触发开始扫码
           */
      @Override public void scan() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_scan, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().scan();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * 触发停止扫码
           */
      @Override public void stop() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().stop();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * 获取扫码头类型
           * 100-->NONE
           * 101-->P2Lite
           * 102-->l2-newland
           * 103-->l2-zabra
           */
      @Override public int getScannerModel() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getScannerModel, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getScannerModel();
          }
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static com.sunmi.scanner.IScanInterface sDefaultImpl;
    }
    static final int TRANSACTION_sendKeyEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_scan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getScannerModel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    public static boolean setDefaultImpl(com.sunmi.scanner.IScanInterface impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.sunmi.scanner.IScanInterface getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * 触发开始与停止扫码
       * key.getAction()==KeyEvent.ACTION_UP 触发开始扫码
       * key.getAction()==KeyEvent.ACTION_DWON 触发停止扫码
       */
  public void sendKeyEvent(android.view.KeyEvent key) throws android.os.RemoteException;
  /**
       * 触发开始扫码
       */
  public void scan() throws android.os.RemoteException;
  /**
       * 触发停止扫码
       */
  public void stop() throws android.os.RemoteException;
  /**
       * 获取扫码头类型
       * 100-->NONE
       * 101-->P2Lite
       * 102-->l2-newland
       * 103-->l2-zabra
       */
  public int getScannerModel() throws android.os.RemoteException;
}
