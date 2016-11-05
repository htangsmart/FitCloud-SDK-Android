package com.htsmart.wristband_smaple.syncdata;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.bean.TodayTotalData;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband_smaple.R;
import com.htsmart.wristband_smaple.SimplePerformerListener;

import java.util.List;

/**
 * Sync Data
 */
public class SyncDataActivity extends AppCompatActivity {

    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);
        mDevicePerformer.addPerformerListener(mPerformerListener);
        mDevicePerformer.syncData();
    }

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onSyncDataStart(boolean success) {
            Toast.makeText(SyncDataActivity.this, "onSyncDataStart:" + success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSyncDataEnd(boolean success) {
            Toast.makeText(SyncDataActivity.this, "onSyncDataEnd:" + success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSyncDataResult(List<SyncRawData> datas) {
            int dataType = datas.get(0).getType();
            Log.e("SyncDataActivity", "dataType:" + dataType + "  size:" + datas.size());
        }

        @Override
        public void onSyncDataTodayTotalData(TodayTotalData data) {
            Log.e("SyncDataActivity", "TodayTotalData:" + data.toString());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }
}
