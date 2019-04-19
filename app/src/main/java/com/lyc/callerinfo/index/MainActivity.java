package com.lyc.callerinfo.index;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lyc.callerinfo.BuildConfig;
import com.lyc.callerinfo.R;
import com.lyc.callerinfo.application.MyApplication;
import com.lyc.callerinfo.base.BaseActivity;
import com.lyc.callerinfo.contract.MainContract;
import com.lyc.callerinfo.index.activity.SettingActivity;
import com.lyc.callerinfo.index.adapter.CallerAdapter;
import com.lyc.callerinfo.model.bean.InCallModel;
import com.lyc.callerinfo.model.db.InCallBean;
import com.lyc.callerinfo.model.setting.Setting;
import com.lyc.callerinfo.model.setting.SettingImpl;
import com.lyc.callerinfo.permission.Permission;
import com.lyc.callerinfo.permission.PermissionImpl;
import com.lyc.callerinfo.presenter.MainPresenter;
import com.lyc.callerinfo.service.FloatWindow;
import com.lyc.callerinfo.utils.SharedPreferencesUtils;
import com.lyc.callerinfo.utils.Window;
import com.lyc.greendao.DaoSession;


import java.util.List;

import javax.inject.Inject;

public class MainActivity extends BaseActivity implements MainContract.View {
    private static final String TAG = MainActivity.class.getSimpleName();
    public final static int REQUEST_CODE_OVERLAY_PERMISSION = 1001;
    public final static int REQUEST_CODE_ASK_PERMISSIONS = 1002;
    public final static int REQUEST_CODE_ASK_CALL_LOG_PERMISSIONS = 1003;
    public final static int REQUEST_CODE_CALL_PERMISSION = 2001;
    public final static int REQUEST_CODE_CALL_LOG_PERMISSION = 2002;
    public final static int REQUEST_CODE_STORAGE_PERMISSION = 2003;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mEmptText;
    private SwipeRefreshLayout mRefreshLayout;
    private FrameLayout mMainLayout;
    LinearLayoutManager mLayoutManager;
    private CallerAdapter mAdapter;
    private Context mContext;
    MainContract.Presenter mPresenter;
    Window mWindow;
    Permission mPermission;
    Setting mSetting;
    private DaoSession mDaoSession;
    private List<InCallBean> mList;


    @Override
    protected int getTitleId() {
        return R.string.app_name;
    }

    @Override
    protected int getlayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showCallLogs(final List<InCallBean> inCalls) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.replaceData(inCalls);
            }
        });
    }

    public class MyRunable implements Runnable {

        @Override
        public void run() {
            mList = mDaoSession.getInCallBeanDao().loadAll();
            if (mList == null || mList.size() == 0) {
                showNoCallLog(true);
            } else {
                showNoCallLog(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.replaceData(mList);
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        checkEula();
        mPresenter = new MainPresenter(this);
        mDaoSession = MyApplication.getDaoSession();
        mSetting = SettingImpl.getInstance();
        mPermission = PermissionImpl.getInstance();
        mWindow = new Window();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int type = getIntent().getIntExtra("type", 0);
            switch (type) {
                case REQUEST_CODE_CALL_PERMISSION:
                    String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        permissions = new String[]{Manifest.permission.CALL_PHONE,
                                Manifest.permission.ANSWER_PHONE_CALLS};
                    }
                    requestPermissions(permissions, REQUEST_CODE_CALL_PERMISSION);
                    break;
                case REQUEST_CODE_CALL_LOG_PERMISSION:
                    requestPermissions(
                            new String[]{Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.WRITE_CALL_LOG},
                            REQUEST_CODE_CALL_LOG_PERMISSION);
                    break;
                case REQUEST_CODE_STORAGE_PERMISSION:
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                    break;
                default:
                    break;
            }
        }

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadInCallList();
            }
        });

    }

    private void checkEula() {
        if (!isEulaSet()) {
            showEula();
        }
    }

    private void showEula() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.eula_title));
        builder.setMessage(getString(R.string.eula_message));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.agree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesUtils.saveSharedPreferences(mContext, "status", "eula", true);
                SharedPreferencesUtils.saveInt(mContext, "eula_version", "eula_version", 1);
                SharedPreferencesUtils.saveInt(mContext, "version", "version", BuildConfig.VERSION_CODE);
            }
        });
        builder.setNegativeButton(getString(R.string.disagree), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private boolean isEulaSet() {
        return SharedPreferencesUtils.getboobean(mContext, "status", "eula");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Thread thread = new Thread(new MyRunable());
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mPermission.canDrawOverlays()) {
            mPermission.requestDrawOverlays(mContext, REQUEST_CODE_OVERLAY_PERMISSION);
        }
        int res = mPermission.checkPermission(Manifest.permission.READ_PHONE_STATE);
        if (res != PackageManager.PERMISSION_GRANTED) {
            mPermission.requestPermissions(mContext, new String[]{
                    Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
        }
//        CallLogs权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int logsres = mPermission.checkPermission(Manifest.permission.READ_CALL_LOG);
            if (logsres != PackageManager.PERMISSION_GRANTED) {
                mPermission.requestPermissions(mContext, new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_CODE_ASK_CALL_LOG_PERMISSIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                case REQUEST_CODE_CALL_PERMISSION:
            int call_phone = mPermission.checkPermission(Manifest.permission.CALL_PHONE);
            if (call_phone != PackageManager.PERMISSION_GRANTED) {
                mPermission.requestPermissions(mContext, new String[]{
                        Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL_PERMISSION);
            }

        }
    }

    @Override
    protected void onStop() {
        if (FloatWindow.status() != FloatWindow.STATUS_CLOSE) {
            // FixME: window in other ui may close because async
            mWindow.closeWindow();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (FloatWindow.status() != FloatWindow.STATUS_CLOSE) {
            mWindow.closeWindow();
        } else {
            super.onBackPressed();
        }
    }

    private void initView() {
        mContext = this;
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mEmptText = findViewById(R.id.empty_text);
        mRecyclerView = findViewById(R.id.history_list);
        mRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CallerAdapter(mContext, mPresenter);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //设置
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            //悬浮窗位置
            case R.id.action_float_window:
                if (FloatWindow.status() == FloatWindow.STATUS_CLOSE) {
                    mWindow.showTextWindow(R.string.float_window_hint, Window.Type.POSITION);
                } else {
                    mWindow.closeWindow();
                }
                break;
            //清除历史
            case R.id.action_clear_history:
                clearHistory();
                break;
            //清除缓存
            case R.id.action_clear_cache:
                clearCache();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCache() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_clear_cache));
        builder.setMessage(getString(R.string.clear_cache_confirm_message));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.clearCache();
                        Snackbar.make(mToolbar, R.string.clear_cache_message, Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.ok), null)
                                .show();
                    }
                });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void clearHistory() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_clear_history));
        builder.setMessage(getString(R.string.clear_history_message));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.clearAll();

                    }
                });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (!mPresenter.canDrawOverlays()) {
                Log.e(TAG, "SYSTEM_ALERT_WINDOW permission not granted...");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
            case REQUEST_CODE_ASK_CALL_LOG_PERMISSIONS:
                if (grantResults.length == 0) {
                    Log.e(TAG, "grantResults is empty!");
                    return;
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public void showNoCallLog(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmptText.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void showLoading(boolean active) {
        mRefreshLayout.setRefreshing(active);
    }

}
