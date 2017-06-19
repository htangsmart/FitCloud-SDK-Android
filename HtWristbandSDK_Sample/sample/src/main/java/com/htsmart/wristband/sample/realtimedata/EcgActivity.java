package com.htsmart.wristband.sample.realtimedata;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.sample.R;
import com.htsmart.wristband.sample.SimplePerformerListener;

/**
 * Created by Kilnn on 2017/6/6.
 */

public class EcgActivity extends AppCompatActivity {
    private static final String TAG = EcgActivity.class.getSimpleName();

    private Button mEcgBtn;
    private boolean isOpening;
    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);

        mDevicePerformer.addPerformerListener(mPerformerListener);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.startup_ing));
        mProgressDialog.setCancelable(false);

        mEcgBtn = (Button) findViewById(R.id.ecg_btn);
        mEcgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = IDevicePerformer.HEALTHY_TYPE_ECG;
                if (!isOpening) {
                    boolean initSend = WristbandApplication.getDevicePerformer().openHealthyRealTimeData(type);
                    if (initSend) {
                        mProgressDialog.show();
                    } else {
                        Toast.makeText(EcgActivity.this, R.string.device_not_connect, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    WristbandApplication.getDevicePerformer().closeHealthyRealTimeData(type);
                }
            }
        });

    }

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onOpenHealthyRealTimeData(int healthyType, boolean success) {
            if (healthyType == IDevicePerformer.HEALTHY_TYPE_ECG) {
                if (success) {
                    isOpening = true;
                    mEcgBtn.setText(R.string.close_ecg);
                } else {
                    Toast.makeText(EcgActivity.this, R.string.startup_failed, Toast.LENGTH_SHORT).show();
                    this.onCloseHealthyRealTimeData(healthyType);
                }
                mProgressDialog.dismiss();
            }
        }

        @Override
        public void onCloseHealthyRealTimeData(int healthyType) {
            if (healthyType == IDevicePerformer.HEALTHY_TYPE_ECG) {
                isOpening = false;
                mEcgBtn.setText(R.string.open_ecg);
            }
        }


        @Override
        public void onResultEcgRealTimeData(byte[] data) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }
}
