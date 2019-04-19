package com.lyc.callerinfo.contract;


import com.lyc.callerinfo.model.db.InCallBean;

import java.util.List;

public interface MainContract {

    interface View extends BaseView<Presenter> {
        void showCallLogs(List<InCallBean> inCalls);
        void showNoCallLog(boolean show);
        void showLoading(boolean active);
    }

    interface Presenter extends BasePresenter {
        void itemOnLongClicked(InCallBean inCallModel);

        boolean canDrawOverlays();

        int checkPermission(String permission);

        void clearSearch();

        void clearCache();

        void clearAll();
        void loadInCallList();
    }
}
