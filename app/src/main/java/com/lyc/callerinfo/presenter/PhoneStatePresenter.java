package com.lyc.callerinfo.presenter;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;


import com.lyc.callerinfo.R;
import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.base.UrlConstants;
import com.lyc.callerinfo.contract.PhoneStateContract;
import com.lyc.callerinfo.data.CallerDataSource;
import com.lyc.callerinfo.data.CallerRepository;
import com.lyc.callerinfo.http.HttpRequest;
import com.lyc.callerinfo.http.HttpStringCallBack;
import com.lyc.callerinfo.model.CallRecord;
import com.lyc.callerinfo.model.SearchMode;
import com.lyc.callerinfo.model.bean.InCallModel;
import com.lyc.callerinfo.model.db.InCall;
import com.lyc.callerinfo.model.db.InCallBean;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.callerinfo.permission.Permission;
import com.lyc.callerinfo.permission.PermissionImpl;
import com.lyc.callerinfo.service.PhoneService;
import com.lyc.callerinfo.utils.Contact;
import com.lyc.callerinfo.utils.Utils;
import com.lyc.callerinfo.utils.Window;
import com.lyc.greendao.DaoSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action1;

public class PhoneStatePresenter implements PhoneStateContract.Presenter {
    private static Context sContext;
    private static Permission mPermission;
    private static Setting mSetting;
    CallRecord mCallRecord;
    private String mIncomingNumber;
    private static CallerDataSource mCallerDataSource;
    private static Contact sContact;
    private static Window mWindow;
    private static HttpRequest sRequest;
    InCallBean bean;
    private InCallModel.ResultBean mModel;
    private static DaoSession mDaoSession;
    private boolean mAutoHangup = false;
    private PhoneService mService;
    private PhoneConnection mConnection;
    private boolean mWaitingCheckHangup = false;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        mDaoSession = MyApplication.getDaoSession();
        mPermission = PermissionImpl.getInstance();
        mSetting = SettingImpl.getInstance();
        mCallerDataSource = CallerRepository.getInstance();
        mWindow = new Window();
        sRequest = HttpRequest.get();
        sContact = Contact.getInstance();

    }

    public static PhoneStatePresenter getInstance() {
        if (sContext == null) {
            throw new IllegalStateException("Setting is not initialized!");
        }
        return SingletonHelper.INSTANCE;
    }

    public PhoneStatePresenter() {
        mCallRecord = new CallRecord();
    }

    @Override
    public boolean matchIgnore(String number) {
        if (!TextUtils.isEmpty(number)) {
            number = number.replaceAll(" ", "");
            String ignoreRegex = mSetting.getIgnoreRegex();
            Log.e("lyc::ignoreRegex", ignoreRegex);
            return number.matches(ignoreRegex);
        }
        return false;
    }

    @Override
    public void handleRinging(String number) {
        mCallRecord.ring();
        if (!TextUtils.isEmpty(number)) {
            mIncomingNumber = number;
            mCallRecord.setLogNumber(number);
            searchNumber(number);
        }
    }

    @Override
    public void handleOffHook(String number) {
        if (System.currentTimeMillis() - mCallRecord.getHook() < 1000 &&
                mCallRecord.isEqual(number)) {
            Log.e("lyc", "duplicate hook,ignore.");
            return;
        }
        mCallRecord.hook();
        if (mCallRecord.isIncoming()) {
            if (mSetting.isHidingOffHook()) {
                mWindow.hideWindow();
            }
        } else {
            if (mSetting.isShowingOnOutgoing()) {
                if (TextUtils.isEmpty(number)) {
                    Log.e("lyc", "number is null." + TextUtils.isEmpty(mIncomingNumber));
                    number = mIncomingNumber;
                    mCallRecord.setLogNumber(number);
                    mIncomingNumber = null;
                }
                searchNumber(number);
            }
        }
    }

    @Override
    public void handleIdle(String number) {
        mCallRecord.idle();
        Log.e("lyc", checkClose(number) + "");
        if (checkClose(number)) {
            return;
        }
        boolean saveLog = mSetting.isAddingCallLog();
        Log.e("lyc", saveLog + "");
        if (isIncoming(mIncomingNumber) && !mCallerDataSource.isIgnoreContact(mIncomingNumber)) {
            Log.e("lyc", "1");
            saveInCall();
            mIncomingNumber = null;
            if (isRingOnce() && mSetting.isAddingRingOnceCallLog()) {
                Log.e("lyc", "2");
                if (mAutoHangup) {
                    mCallRecord.setLogName(sContext.getString(R.string.auto_hangup), saveLog);
                } else {
                    mCallRecord.setLogName(sContext.getString(R.string.ring_once), saveLog);
                }
                saveLog = true;
            }
        }
        Log.e("lyc", isShowing() + "1");
        if (isShowing()) {
            if (mCallRecord.isValid()) {
                Log.e("lyc", mCallRecord.isValid() + "1");
                if (mCallRecord.isNameValid()) {
                    Log.e("lyc", mCallRecord.isNameValid() + "1");
                    if (saveLog) {
                        updateCallLog(mCallRecord.getLogNumber(), mCallRecord.getLogName());
                    }
                    if (mSetting.isAutoReportEnabled()) {
                        reportFetchedNumber();
                    }
                } else {
                    if (mSetting.isMarkingEnabled() && mCallRecord.isAnswered() &&
                            !mCallerDataSource.isIgnoreContact(mCallRecord.getLogNumber()) &&
                            !isNotMarkContact(mCallRecord.getLogNumber())) {
                        Log.e("lyc", mCallRecord.getLogNumber() + "2");
                        showMark(mCallRecord.getLogNumber());
                    }
                }
            }
        }
        resetCallRecord();
        mAutoHangup = false;
        mWindow.closeWindow();
        mSetting.setOutgoing(false);
//        插件服务关闭代码
    }

    @Override
    public void resetCallRecord() {
        mCallRecord.reset();
    }

    @Override
    public boolean checkClose(String number) {
        return TextUtils.isEmpty(number) && mCallRecord.callDuration() == -1;
    }

    @Override
    public boolean isIncoming(String number) {
        return mCallRecord.isIncoming() && !TextUtils.isEmpty(mIncomingNumber);
    }

    //    数据插入数据库
    @Override
    public void saveInCall() {
        mDaoSession.getInCallDao().insertOrReplace(new InCall(mIncomingNumber, mCallRecord.time(), mCallRecord.ringDuration(), mCallRecord.callDuration()));
    }

    @Override
    public boolean isRingOnce() {
        return mCallRecord.ringDuration() < 3000 && mCallRecord.callDuration() <= 0;
    }

    private boolean isNotMarkContact(String number) {
        return mSetting.isNotMarkContact() && mPermission.canReadContact()
                && sContact.isExist(number);
    }

    @Override
    public void searchNumber(String number) {
        Log.e("lyc:::::", number);
        if (TextUtils.isEmpty(number)) {
            Log.e("lyc", "searchNumber: number is null!");
            return;
        }

        final SearchMode mode = mCallerDataSource.getSearchMode(number);

        if (mode == SearchMode.IGNORE) {
            return;
        }
        if (mSetting.isAutoHangup() || mSetting.isAddingCallLog()) {
//            自动挂断/添加号码信息到通话记录
            bindPhoneService();
        }
//        号码过滤
        final String numberOrigin = CallerRepository.fixNumber(number);
        //                        获取号码信息
        sRequest.doGet(UrlConstants.Base_Http, numberOrigin, InCallModel.class, new HttpStringCallBack() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof InCallModel) {
                    InCallModel model = (InCallModel) result;
                    if (model.getError_code() == 0) {
                        mModel = model.getResult();
                        bean = new InCallBean();
                        bean.setCity(mModel.getCity());
                        bean.setPhone(mModel.getPhone());
                        bean.setIszhapian(mModel.getIszhapian() + "");
                        bean.setProvince(mModel.getProvince());
                        bean.setSp(mModel.getSp());
                        bean.setRpt_type(mModel.getRpt_type());
                        bean.setRpt_comment(mModel.getRpt_comment());
                        bean.setRpt_cnt(mModel.getRpt_cnt());
                        bean.setHyname(mModel.getHyname());
                        bean.setCountDesc(mModel.getCountDesc());
                        mDaoSession.getInCallBeanDao().insertInTx(bean);
                        showNumber(bean);
                    }
                }
            }

            @Override
            public void onFailure(int code, String msg) {

            }
        });

    }

    private void showNumber(InCallBean inCallBean) {
        mCallRecord.setLogNumber(inCallBean.getPhone());
        mCallRecord.setLogGeo(inCallBean.getProvince() + " " + inCallBean.getCity());
        if (mCallRecord.isActive()) {
            if (inCallBean.getIszhapian().equals("1")) {
                mWindow.showWindow(inCallBean.getCountDesc(), Window.Type.CALLER,1);
            } else {
                mWindow.showWindow(inCallBean.getProvince() + " " + inCallBean.getCity(), Window.Type.CALLER,0);
            }
        }

        checkAutoHangUp();
    }

    private void bindPhoneService() {
        Log.e("lyc", "bindPhoneService");
        if (mService != null) {
            Log.e("lyc", "服务一开始.");
            return;
        }
        if (!mSetting.isAutoHangup() && !mSetting.isAddingCallLog()) {
            Log.e("lyc", "功能未启用.");
            return;
        }
        mAutoHangup = false;
        if (mConnection == null) {
            mConnection = newConnection();
        }
        Intent intent = new Intent(sContext, PhoneService.class);
        sContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private PhoneConnection newConnection() {
        return new PhoneConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e("lyc", "onServiceConnected: " + name.toString());
                mService = ((PhoneService.MyBindel) service).getservice();
                if (mWaitingCheckHangup) {
                    checkAutoHangUp();
                }
                mWaitingCheckHangup = false;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("lyc", "onServiceDisconnected: " + name.toString());
                mService = null;
            }
        };
    }

    private void checkAutoHangUp() {
        Log.e("lyc", "checkAutoHangUp");
        if (mService == null) {
            Log.e("lyc", "checkAutoHangUp: 服务未启动.");
            mWaitingCheckHangup = true;
            return;
        }
        if (!mCallRecord.isIncoming() && mSetting.isDisableOutGoingHangup()) {
            Log.e("lyc", "checkAutoHangUp: 传出时禁用自动挂断.");
            return;
        }
        if (mCallRecord.isIncoming() && mSetting.isTemporaryDisableHangup()) {
            Log.e("lyc", "checkAutoHangUp: 重复时禁用自动挂断功能.");
            return;
        }
        try {
            //        挂断电话号码包含关键词
            if (mSetting.isAutoHangup()) {
                String keywords = mSetting.getKeywords();
                for (String keyword : keywords.split(" ")) {
                    if (mCallRecord.matchName(keyword)) {
                        Log.e("lyc", "checkAutoHangUp: 匹配关键字");
                        mAutoHangup = true;
                        break;
                    }
                }
//                挂断电话，黑名单中的号码归属地
                String geoKeywords = mSetting.getGeoKeyword();
                if (!geoKeywords.isEmpty() && mCallRecord.isGeoValid()) {
                    boolean hangup = false;
                    for (String keyword : geoKeywords.split(" ")) {
                        if (!keyword.startsWith("!")) {
//                            在黑名单中
                            if (mCallRecord.matchGeo(keyword)) {
                                Log.e("lyc", "匹配黑名单");
                                hangup = true;
                                break;
                            }
                        } else if (mCallRecord.matchGeo(keyword.replace("!", ""))) {
//                            白名单中
                            Log.e("lyc", "匹配白名单");
                            hangup = false;
                            break;
                        } else {
//                            不在白名单中
                            hangup = true;
                            Log.e("lyc", "匹配geo不在白名单中");
                        }
                    }
                    if (hangup) {
                        Log.e("lyc", "checkAutoHangUp: geo 挂断");
                        mAutoHangup = true;
                    }
                }
                // 挂断电话号码以关键字开头
                String numberKeywords = mSetting.getNumberKeyword().replaceAll("\\*", "");
                if (!numberKeywords.isEmpty()) {
                    for (String keyword : numberKeywords.split(" ")) {
                        if (mCallRecord.matchNumber(keyword)) {
                            Log.e("lyc", "checkAutoHangUp: match number");
                            mAutoHangup = true;
                        }
                    }
                }
                // 挂断电话
                if (mAutoHangup && mService != null) {
                    Log.e("lyc", "hangUpPhoneCall");
                    mService.hangUpPhoneCall();
                }
            }
        } catch (Exception e) {

        }


    }

    @Override
    public void setOutGoingNumber(String number) {
        mIncomingNumber = number;
        mSetting.setOutgoing(true);
    }

    @Override
    public boolean canReadPhoneState() {
        return mPermission.canReadPhoneState();
    }

    @Override
    public void start() {

    }

    public boolean isShowing() {
        return mWindow.isShowing();
    }

    public void showMark(String number) {

        KeyguardManager keyguardManager = (KeyguardManager) sContext.getSystemService(
                Context.KEYGUARD_SERVICE);

        boolean isKeyguardLocked;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isKeyguardLocked = keyguardManager.isKeyguardLocked();
        } else {
            isKeyguardLocked = keyguardManager.inKeyguardRestrictedInputMode();
        }
        Log.e("lyc", isKeyguardLocked + "");
        if (isKeyguardLocked) {
            Utils.showMarkNotification(sContext, number);
        } else {
            Utils.startMarkActivity(sContext, number);
        }
    }

    private void updateCallLog(String number, String name) {
        Log.d("lyc", name);
        if (mService != null) {
            try {
                mService.updateCallLog(number, name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void reportFetchedNumber() {
        // Currently do noting, let the alarm handle marked number.
    }

    private static class SingletonHelper {
        private final static PhoneStatePresenter INSTANCE = new PhoneStatePresenter();
    }

    interface PhoneConnection extends ServiceConnection {
    }
}
