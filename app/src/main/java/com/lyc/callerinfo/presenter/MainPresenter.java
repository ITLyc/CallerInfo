package com.lyc.callerinfo.presenter;


import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.contract.MainContract;
import com.lyc.callerinfo.model.db.InCallBean;
import com.lyc.callerinfo.permission.Permission;
import com.lyc.callerinfo.permission.PermissionImpl;
import com.lyc.greendao.DaoSession;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter implements MainContract.Presenter {

    private final List<InCallBean> mInCallList = new ArrayList<>();
    public MainContract.View mView;
    private boolean isInvalidateDataUpdate = false;
    private boolean isWaitDataUpdate = false;
    Permission mPermission;
    DaoSession mDaoSession;


    public MainPresenter(MainContract.View view) {
        mView = view;
        mPermission = PermissionImpl.getInstance();
        mDaoSession = MyApplication.getDaoSession();
    }

    @Override
    public void itemOnLongClicked(InCallBean inCallModel) {
    }

    @Override
    public boolean canDrawOverlays() {
        return mPermission.canDrawOverlays();
    }

    @Override
    public int checkPermission(String permission) {
        return mPermission.checkPermission(permission);
    }

    @Override
    public void clearSearch() {

    }

    @Override
    public void clearCache() {

    }

    @Override
    public void clearAll() {
        mDaoSession.getInCallBeanDao().deleteAll();
        loadInCallList();
    }

    @Override
    public void loadInCallList() {
        List<InCallBean> list = mDaoSession.getInCallBeanDao().loadAll();
        mInCallList.clear();
        mInCallList.addAll(list);
        mView.showCallLogs(mInCallList);
        mView.showLoading(false);
    }

    @Override
    public void start() {

    }
}
