package com.lyc.callerinfo.data;


import com.lyc.callerinfo.model.SearchMode;
import com.lyc.callerinfo.model.db.InCallBean;

import rx.Observable;


public interface CallerDataSource {
    SearchMode getSearchMode(String number);

    boolean isIgnoreContact(String number);

    Observable<InCallBean> getCaller(String number);
}
