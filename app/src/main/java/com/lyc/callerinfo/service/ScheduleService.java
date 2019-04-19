package com.lyc.callerinfo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.lyc.callerinfo.model.db.MarkedRecord;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.greendao.DaoSession;
import com.lyc.greendao.MarkedRecordDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleService extends Service {
    private static final String TAG = ScheduleService.class.getSimpleName();
    private Handler mThreadHandler;
    private Handler mMainHandler;
    private List<String> mPutList;
    private static Setting mSetting;
    private static Context sContext;
    private static DaoSession sDaoSession;

    public static void init(Context context) {
        sContext = context.getApplicationContext();


    }

    public ScheduleService() {
        mSetting = SettingImpl.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mThreadHandler = new Handler(handlerThread.getLooper());
        mMainHandler = new Handler(getMainLooper());

        mPutList = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        mThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                runScheduledJobs();
            }
        });
        return START_NOT_STICKY;

    }

    private void runScheduledJobs() {
        List<MarkedRecord> records = sDaoSession.getMarkedRecordDao().loadAll();
        boolean isAutoReport = mSetting.isAutoReportEnabled();

        for (MarkedRecord record : records) {
            if (!record.getReported()) {
                if (!isAutoReport && record.getSource() != MarkedRecord.API_ID_USER_MARKED) {
                    continue;
                }
                if (!TextUtils.isEmpty(record.getTypeName())) {
                    mPutList.add(record.getNumber());
                    // this put operation is asynchronous
                    onPutResult(record.getNumber());
                } else {
                }
            }
        }
        mSetting.updateLastScheduleTime();
        checkStopSelf();
    }

    private void onPutResult(String number) {
        List<MarkedRecord> records = sDaoSession.getMarkedRecordDao().queryBuilder()
                .where(MarkedRecordDao.Properties.Number.eq(number)).list();
        MarkedRecord record = null;
        if (records.size() > 0) {
            record = records.get(0);
            record.setReported(true);
            sDaoSession.getMarkedRecordDao().update(record);
        }
        if (records.size() > 1) {
            Log.e("lyc", "updateMarkedRecord duplicate number: " + number);
        }
    }

    private void checkStopSelf() {
        if (mPutList.size() == 0) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        mThreadHandler.removeCallbacksAndMessages(null);
        mThreadHandler.getLooper().quit();

        super.onDestroy();
    }
}
