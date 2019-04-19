package com.lyc.callerinfo.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getlayoutId());
        setTitle(getTitleId());
    }

    protected abstract int getTitleId();

    protected abstract int getlayoutId();

}
