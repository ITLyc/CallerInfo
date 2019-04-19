package com.lyc.callerinfo.contract;

import android.content.Context;

import com.lyc.callerinfo.model.bean.InCallModel;


public interface PhoneStateContract {

    interface View {

        void show(InCallModel number);

        void showFailed(boolean isOnline);

        void showSearching();

        void hide(String number);

        void close(String number);

        Context getContext();
    }

    interface Presenter extends BasePresenter {

        boolean matchIgnore(String number);

        void handleRinging(String number);

        void handleOffHook(String number);

        void handleIdle(String number);

        void resetCallRecord();

        boolean checkClose(String number);

        boolean isIncoming(String number);

        void saveInCall();

        boolean isRingOnce();

        void searchNumber(String number);

        void setOutGoingNumber(String number);

        boolean canReadPhoneState();
    }
}
