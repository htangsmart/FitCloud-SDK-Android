package com.htsmart.wristband.sample.syncdata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.SleepTotalData;
import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.bean.TodayTotalData;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.performer.PerformerListener;
import com.htsmart.wristband.sample.MyApplication;
import com.htsmart.wristband.sample.R;
import com.htsmart.wristband.sample.SimplePerformerListener;
import com.htsmart.wristband.sample.syncdata.db.DbHelper;

import java.util.List;

/**
 * Sync Data
 */
public class SyncDataActivity extends AppCompatActivity {

    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    private ProgressDialog mProgressDialog;

    private DbHelper mDbHelper;
    private int mUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);
        mDevicePerformer.addPerformerListener(mPerformerListener);

        mDbHelper = DbHelper.getInstance(this);
        mUserId = MyApplication.getInstance().getUser().getId();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        findViewById(R.id.sync_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = mDevicePerformer.syncData();
                if (success) {
                    mSyncCount = 0;
                    showProgressMessage(R.string.prepare_sync);
                } else {
                    dismissProgressMessage(R.string.sync_data_cmd_failed);
                }
            }
        });

        findViewById(R.id.sleep_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SyncDataActivity.this, SleepDataActivity.class));
            }
        });
    }

    private int mSyncCount = 0;

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onSyncDataStart(@SyncResult int result) {
            if (result == PerformerListener.SYNC_STARTED) {
                showProgressMessage(R.string.sync_data_started);
            } else {
                dismissProgressMessage(R.string.prepare_sync_failed);
            }
        }

        @Override
        public void onSyncDataEnd(boolean success) {
            if (success) {
                dismissProgressMessage(R.string.sync_data_success);
            } else {
                dismissProgressMessage(R.string.sync_data_failed);
            }
        }

        @Override
        public void onSyncDataResult(final List<SyncRawData> datas) {
            int dataType = datas.get(0).getType();
            Log.d("SyncDataActivity", "dataType:" + dataType + "  size:" + datas.size());

            //Save to database
            mDbHelper.saveSyncRawData(mUserId, datas);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSyncCount += datas.size();
                    mProgressDialog.setMessage(getString(R.string.sync_data_count, mSyncCount));
                }
            });
        }

        @Override
        public void onSyncDataTodayTotalData(TodayTotalData data) {
            Log.d("SyncDataActivity", "TodayTotalData:" + data.toString());
        }

        @Override
        public void onSyncDataSleepTotalData(List<SleepTotalData> datas) {
            if (datas != null && datas.size() > 0) {
                Log.e("SyncDataActivity", "SleepTotalData:" + JSON.toJSONString(datas));
                SleepDataHelper.setSleepTotalData(SyncDataActivity.this, datas);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }


    private void showProgressMessage(@StringRes int msg) {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        mProgressDialog.setMessage(getString(msg));
    }

    private void dismissProgressMessage(@StringRes int msg) {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        mProgressDialog.setMessage(getString(msg));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 1500);
    }

}
