package com.lyc.callerinfo.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lyc.callerinfo.R;
import com.lyc.callerinfo.index.MainActivity;
import com.lyc.callerinfo.service.IPluginService;
import com.lyc.callerinfo.service.IPluginServiceCallback;
import com.lyc.callerinfo.utils.Utils;

import java.lang.reflect.Method;

public class PhoneService extends Service implements IPluginService {
    private final static String TAG = PhoneService.class.getSimpleName();
    private IPluginServiceCallback mCallback;
    private String mNumber;
    private String mName;
    private Handler mHandler = new Handler();

    private final IBinder mBinder = new MyBindel();

    public class MyBindel extends Binder {
        public PhoneService getservice() {
            return PhoneService.this;
        }
    }

    public PhoneService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = ((NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE));
            if (nm != null) {
                String CHANNEL_ID = getPackageName() + "." + getClass().getSimpleName();
                NotificationChannel channel;
                channel = new NotificationChannel(CHANNEL_ID,
                        getResources().getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_LOW);
                nm.createNotificationChannel(channel);

                Notification notification = new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("")
                        .setChannelId(CHANNEL_ID)
                        .setContentText("").build();
                startForeground(1, notification);
            }
        }
        int res = super.onStartCommand(intent, flags, startId);
        if (mCallback != null) {
            int type = intent.getIntExtra("type", 0);
            boolean result = intent.getBooleanExtra("result", false);
            switch (type) {
                case MainActivity.REQUEST_CODE_CALL_PERMISSION:
                    mCallback.onCallPermissionResult(result);
                    break;
                case MainActivity.REQUEST_CODE_CALL_LOG_PERMISSION:
                    mCallback.onCallLogPermissionResult(result);
                    break;
                case MainActivity.REQUEST_CODE_STORAGE_PERMISSION:
                    mCallback.onStoragePermissionResult(result);
                    break;
            }
        }
        return res;

    }

    //自动挂断
    @Override
    public void checkCallPermission() {
        Log.d("lyc", "checkCallPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int res = checkSelfPermission(Manifest.permission.CALL_PHONE);
            if (res == PackageManager.PERMISSION_GRANTED
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                res = checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS);
            }
            if (res != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(PhoneService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", MainActivity.REQUEST_CODE_CALL_PERMISSION);
                startActivity(intent);
            }
        }
    }

    //添加号码信息到通话记录
    @Override
    public void checkCallLogPermission() {
        Log.d("lyc", "checkCallLogPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int res = checkSelfPermission(Manifest.permission.READ_CALL_LOG);
            int res2 = checkSelfPermission(Manifest.permission.WRITE_CALL_LOG);
            if (res != PackageManager.PERMISSION_GRANTED ||
                    res2 != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(PhoneService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", MainActivity.REQUEST_CODE_CALL_LOG_PERMISSION);
                startActivity(intent);
            }
        }
    }

    @Override
    public void hangUpPhoneCall() {
        checkCallPermission();
        Log.e("lyc", "hangUpPhoneCall::" + killPhoneCall());
    }

    private boolean killPhoneCall() {
        try {
            tryEndCall();
            //获得电话管理服务，以便获得电话的状态
            TelephonyManager telephonyManager =
                    (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            //通过Java反射技术获取getITelephony方法对应的Method对象
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
            //允许访问getITelephony方法
            methodGetITelephony.setAccessible(true);
            //调用getITelephony方法获取ITelephony对象
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            //获取与endCall方法对应的Method对象
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
            methodEndCall.invoke(telephonyInterface);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hangupPhoneCall: " + e.toString());
            return false;
        }
        return true;
    }

    private void tryEndCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {

                TelecomManager telecomManager = (TelecomManager) getSystemService(
                        Context.TELECOM_SERVICE);
                if (telecomManager != null) {
                    boolean res = telecomManager.endCall();
                    Log.d(TAG, "tryEndCall: " + res);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateCallLog(String number, String name) {
        Log.d(TAG, "updateCallLog: " + "name = [" + name + "]");
        mNumber = number;
        mName = name;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.READ_CALL_LOG) !=
                                PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "updateCallLog: 没有READ_CALL_LOG权限");
                    return;
                }
                ContentValues cintent = new ContentValues();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cintent.put(CallLog.Calls.GEOCODED_LOCATION, mName);
                } else {
                    cintent.put(CallLog.Calls.NUMBER, mName + "(" + mNumber + ")");
                }
                getContentResolver().update(CallLog.Calls.CONTENT_URI, cintent,
                        CallLog.Calls.NUMBER + "=?", new String[]{mNumber});
            }
        }, 1000);
    }

    @Override
    public void registerCallback(IPluginServiceCallback callback) {
        Log.d("lyc", "registerCallback: ");
        mCallback = callback;
    }

    @Override
    public String exportData(String data) {
        return null;
    }

    @Override
    public String importData() {
        return null;
    }

    //        自动上报/自动标记
    @Override
    public void checkStoragePermission() {
        Log.d(TAG, "checkWritePermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int res = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int res2 = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (res != PackageManager.PERMISSION_GRANTED ||
                    res2 != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(PhoneService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", MainActivity.REQUEST_CODE_STORAGE_PERMISSION);
                startActivity(intent);
            }
        }
    }

    @Override
    public void setIconStatus(boolean enabled) {
        if (enabled) {
            Utils.showIcon(PhoneService.this);
        } else {
            Utils.hideIcon(PhoneService.this);
        }
    }
}
