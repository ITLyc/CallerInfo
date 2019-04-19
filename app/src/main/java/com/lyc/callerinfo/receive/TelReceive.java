package com.lyc.callerinfo.receive;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.lyc.callerinfo.BuildConfig;
import com.lyc.callerinfo.R;
import com.lyc.callerinfo.contract.PhoneStateContract;
import com.lyc.callerinfo.model.bean.InCallModel;
import com.lyc.callerinfo.presenter.PhoneStatePresenter;
import com.lyc.callerinfo.service.FloatWindow;
import com.lyc.callerinfo.utils.Utils;
import com.lyc.callerinfo.utils.Window;

import javax.inject.Inject;

public class TelReceive extends BroadcastReceiver {
    private final static String TAG = TelReceive.class.getSimpleName();
    private static final String PHONE_INCOMING_KEY = "incoming_number";
    private static boolean incomingFlag = false;
    private PhoneStateListener mPhoneStateListener;

    public TelReceive() {
        mPhoneStateListener = PhoneStateListener.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mPhoneStateListener.setContext(context);
        if (BuildConfig.DEBUG) {
            Log.e("lyc", "onReceive: " + intent.toString() + " " +
                    Utils.bundleToString(intent.getExtras()));
        }

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Intent.ACTION_NEW_OUTGOING_CALL:
                    if (intent.getExtras() != null) {
                        mPhoneStateListener.setOutGoingNumber(
                                intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER));
                    }
                    break;
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                    if (intent.getExtras() != null) {
                        String state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                        String number = intent.getExtras()
                                .getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        mPhoneStateListener.onCallStateChanged(state, number);
                    }
                    break;
            }
        }
    }

    public final static class PhoneStateListener implements PhoneStateContract.View {

        @Inject
        PhoneStateContract.Presenter mPresenter;

        @Inject
        Window mWindow;

        private Context mContext;

        private PhoneStateListener() {
            mWindow = new Window();
            mPresenter = PhoneStatePresenter.getInstance();
        }

        public void setContext(Context context) {
            mContext = context.getApplicationContext();
        }

        private void setOutGoingNumber(String number) {
            mPresenter.setOutGoingNumber(number);
            onCallStateChanged(TelephonyManager.EXTRA_STATE_OFFHOOK, number);
        }

        public void onCallStateChanged(int state, String number) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    onCallStateChanged(TelephonyManager.EXTRA_STATE_RINGING, number);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    onCallStateChanged(TelephonyManager.EXTRA_STATE_OFFHOOK, number);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    onCallStateChanged(TelephonyManager.EXTRA_STATE_IDLE, number);
                    break;
            }
        }

        public void onCallStateChanged(String state, String number) {

            if (BuildConfig.DEBUG) {
                Log.e("lyc", "onCallStateChanged: " + state + " : " + number);
                Log.e("lyc", "onCallStateChanged: permission -> " + mPresenter.canReadPhoneState());
            }

            if (mPresenter.matchIgnore(number)) {
                return;
            }
            switch (state) {
//                呼叫
                case "RINGING":
                    mPresenter.handleRinging(number);
                    break;
//                接听
                case "OFFHOOK":
                    mPresenter.handleOffHook(number);
                    break;
//                挂断
                case "IDLE":
                    mPresenter.handleIdle(number);
                    break;
            }
        }

        @Override
        public void show(InCallModel number) {

        }

        @Override
        public void showFailed(boolean isOnline) {
            if (isOnline) {
                mWindow.sendData(FloatWindow.WINDOW_ERROR,
                        R.string.online_failed, Window.Type.CALLER);
            } else {
                mWindow.showTextWindow(R.string.offline_failed, Window.Type.CALLER);
            }
        }

        @Override
        public void showSearching() {
            mWindow.showTextWindow(R.string.searching, Window.Type.CALLER);
        }

        @Override
        public void hide(String incomingNumber) {
            mWindow.hideWindow();
        }

        @Override
        public void close(String incomingNumber) {
            mWindow.closeWindow();
        }

        @Override
        public Context getContext() {
            return mContext;
        }


        public static PhoneStateListener getInstance() {
            return SingletonHelper.sINSTANCE;
        }

        private final static class SingletonHelper {
            @SuppressLint("StaticFieldLeak")
            private final static PhoneStateListener sINSTANCE = new PhoneStateListener();
        }
    }
}
