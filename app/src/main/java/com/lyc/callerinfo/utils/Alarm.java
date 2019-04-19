package com.lyc.callerinfo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.callerinfo.service.ScheduleService;

public class Alarm {
    private static final String TAG = Alarm.class.getSimpleName();
    private Context mContext;
    private Setting mSetting;
    private MyApplication mApplication;
    public Alarm(Context context){
        mContext= context.getApplicationContext();
        mSetting= SettingImpl.getInstance();
        mApplication=  MyApplication.getApp();
    }

    public void alarm(){
        Log.v(TAG, "alarm");
        if (!mSetting.isAutoReportEnabled()&&!mSetting.isMarkingEnabled()){
            Log.v(TAG, "alarm is not installed");
            return;
        }
        Intent intent = new Intent(mApplication, ScheduleService.class);
        PendingIntent pIntent = PendingIntent.getService(mApplication, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        long now = System.currentTimeMillis();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, now + 5 * 1000, 60 * 60 * 1000, pIntent);
    }
}
