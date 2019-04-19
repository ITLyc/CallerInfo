package com.lyc.callerinfo.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.lyc.callerinfo.R;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.pkmmte.view.CircularImageView;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

public class MarkWindow extends StandOutWindow {
    private static final String TAG = MarkWindow.class.getSimpleName();
    private WindowManager mWindowManager;
    private Setting mSetting;

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mSetting = SettingImpl.getInstance();
            return super.onStartCommand(intent, flags, startId);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }


    @Override
    public String getAppName() {
        return getResources().getString(R.string.mark_window);
    }

    @Override
    public int getAppIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.mark_window, frame, true);
        bindCircleImage(view, R.id.express);
        bindCircleImage(view, R.id.takeout);
        bindCircleImage(view, R.id.selling);
        bindCircleImage(view, R.id.harass);
        bindCircleImage(view, R.id.bilk);
    }

    private void bindCircleImage(View view, int id) {
        final CircularImageView circularImageView = view.findViewById(id);
        circularImageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e(TAG, v.toString() + ":" + hasFocus);
            }
        });
        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, v.toString() + "xxx");
            }
        });
    }

    @Override
    public void onMove(int id, Window window, View view, MotionEvent event) {
        super.onMove(id, window, view, event);
        int x = window.getLayoutParams().x;
        int width = mSetting.getScreenWidth();
        View layout = window.findViewById(R.id.content);
        float alpha = (float) ((width - Math.abs(x) * 1.2) / width);
        layout.setAlpha(alpha);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (alpha < 0.6) {
                    hide(id);
                } else {
                    reset(id);
                    layout.setAlpha(1.0f);
                }
                break;
        }
    }

    private void reset(int id) {
        final Window window = getWindow(id);
        mWindowManager.updateViewLayout(window, getParams(id, window));
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        StandOutLayoutParams params = new StandOutLayoutParams(id, mSetting.getScreenWidth(), mSetting.getWindowHeight(),
                StandOutLayoutParams.CENTER,
                StandOutLayoutParams.CENTER);
        int x = mSetting.getWindowX();
        int y = mSetting.getWindowY();

        if (x != -1 && y != -1) {
            params.x = x;
            params.y = y;
        }

        params.y = (int) (mSetting.getDefaultHeight() * 1.5);

        params.minWidth = mSetting.getScreenWidth();
        params.maxWidth = Math.max(mSetting.getScreenWidth(), mSetting.getScreenHeight());
        params.minHeight = mSetting.getDefaultHeight() * 2;
        params.height = mSetting.getDefaultHeight() * 5;
        return params;
    }

    @Override
    public int getFlags(int id) {
        return StandOutFlags.FLAG_BODY_MOVE_ENABLE
                |StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }

    @Override
    public int getThemeStyle() {
        return R.style.AppTheme;
    }

    @Override
    public boolean isDisableMove(int id) {
        return false;
    }
}
