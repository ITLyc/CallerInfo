package com.lyc.callerinfo.data;

import android.content.Context;
import android.util.Log;

import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.base.UrlConstants;
import com.lyc.callerinfo.data.CallerDataSource;
import com.lyc.callerinfo.http.HttpRequest;
import com.lyc.callerinfo.http.HttpStringCallBack;
import com.lyc.callerinfo.model.SearchMode;
import com.lyc.callerinfo.model.bean.InCallModel;
import com.lyc.callerinfo.model.db.InCallBean;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.callerinfo.permission.Permission;
import com.lyc.callerinfo.permission.PermissionImpl;
import com.lyc.callerinfo.utils.Contact;
import com.lyc.callerinfo.utils.Window;
import com.lyc.greendao.DaoSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class CallerRepository implements CallerDataSource {
    private static Context sContext;
    private static Setting mSetting;
    private static Permission sPermission;
    private static HttpRequest sRequest;
    private static Window mWindow;
    Contact mContact;
    private InCallModel mModel;
    private static DaoSession mDaoSession;
    private Set<String> mLoadingCache;
    private Map<String, InCallBean> mCallerMap;
    private Map<String, Long> mErrorCache;


    public static void init(Context context) {
        sContext = context;
    }

    private CallerRepository() {
        mDaoSession = MyApplication.getDaoSession();
        mSetting = SettingImpl.getInstance();
        sPermission = PermissionImpl.getInstance();
        mContact = Contact.getInstance();

        mWindow = new Window();
        mLoadingCache = Collections.synchronizedSet(new HashSet<String>());
        mCallerMap = Collections.synchronizedMap(new HashMap<String, InCallBean>());
        mErrorCache = Collections.synchronizedMap(new HashMap<String, Long>());
    }

    public static CallerRepository getInstance() {
        if (sContext == null) {
            throw new IllegalStateException("CallerRepository is not initialized!");
        }
        return SingletonHelper.INSTANCE;
    }

    @Override
    public SearchMode getSearchMode(String number) {
        SearchMode mode = SearchMode.ONLINE;
        if (isIgnoreContact(number)) {
            if (mSetting.isShowingContactOffline()) {
                mode = SearchMode.OFFLINE;
            } else {
                mode = SearchMode.IGNORE;
            }
        }
        return mode;
    }

    public static String fixNumber(String number) {
        String fixedNumber = number;
        if (number.startsWith("+86")) {
            fixedNumber = number.replace("+86", "");
        }

        if (number.startsWith("86") && number.length() > 9) {
            fixedNumber = number.replaceFirst("^86", "");
        }

        if (number.startsWith("+400")) {
            fixedNumber = number.replace("+", "");
        }

        if (fixedNumber.startsWith("12583")) {
            fixedNumber = fixedNumber.replaceFirst("^12583.", "");
        }

        if (fixedNumber.startsWith("1259023")) {
            fixedNumber = number.replaceFirst("^1259023", "");
        }

        if (fixedNumber.startsWith("1183348")) {
            fixedNumber = number.replaceFirst("^1183348", "");
        }

        return fixedNumber;
    }

    @Override
    public boolean isIgnoreContact(String number) {
        return mSetting.isIgnoreKnownContact() && sPermission.canReadContact()
                && (mContact.isExist(number) || mContact.isExist(fixNumber(number)));
    }

    @Override
    public Observable<InCallBean> getCaller(String numberOrigin) {
        Log.e("lyc", "getCaller: " + numberOrigin);
        final String number = fixNumber(numberOrigin);
        return Observable.create(new Observable.OnSubscribe<InCallBean>() {
            @Override
            public void call(Subscriber<? super InCallBean> subscriber) {
                try {
                    do {
//                        检查加载缓存
                        if (mLoadingCache.contains(number)) {
//                            没有onCompleted返回
                            return;
                        }
                        mLoadingCache.add(number);

                        if (mModel != null) {
                            Log.e("lyc","mModel != null");
                        }
                    } while (false);
                } catch (Exception e) {
                    Log.e("lyc", "getCaller failed: " + e.getMessage());
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
        }).doOnNext(new Action1<InCallBean>() {
            @Override
            public void call(InCallBean inCallBean) {

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    private static class SingletonHelper {
        private final static CallerRepository INSTANCE = new CallerRepository();
    }
}
