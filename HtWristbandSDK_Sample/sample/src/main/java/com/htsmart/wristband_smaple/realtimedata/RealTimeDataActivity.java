package com.htsmart.wristband_smaple.realtimedata;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.bean.WristbandVersion;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband_smaple.R;
import com.htsmart.wristband_smaple.SimplePerformerListener;

/**
 * Created by Kilnn on 16-10-26.
 */
public class RealTimeDataActivity extends AppCompatActivity {

    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_data);
        initView();
        mDevicePerformer.addPerformerListener(mPerformerListener);
        mDevicePerformer.cmd_requestWristbandConfig();
    }

    private TextView mHeartRateTv;
    private TextView mOxygenTv;
    private TextView mBloodPressureTv;
    private TextView mRespiratoryRateTv;

    private Button mHeartRateBtn;
    private Button mOxygenBtn;
    private Button mBloodPressureBtn;
    private Button mRespiratoryRateBtn;


    private boolean mHeartRateStarted = false;
    private boolean mOxygenStarted = false;
    private boolean mBloodPressureStarted = false;
    private boolean mRespiratoryRateStarted = false;

    private void initView() {
        mHeartRateTv = (TextView) findViewById(R.id.heart_rate_tv);
        mOxygenTv = (TextView) findViewById(R.id.oxygen_tv);
        mBloodPressureTv = (TextView) findViewById(R.id.blood_pressure_tv);
        mRespiratoryRateTv = (TextView) findViewById(R.id.respiratory_rate_tv);

        mHeartRateBtn = (Button) findViewById(R.id.heart_rate_btn);
        mHeartRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mHeartRateStarted) {
                    mDevicePerformer.openHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_HEART_RATE);
                } else {
                    mDevicePerformer.closeHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_HEART_RATE);
                }
            }
        });

        mOxygenBtn = (Button) findViewById(R.id.oxygen_btn);
        mOxygenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mOxygenStarted) {
                    mDevicePerformer.openHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_OXYGEN);
                } else {
                    mDevicePerformer.closeHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_OXYGEN);
                }
            }
        });

        mBloodPressureBtn = (Button) findViewById(R.id.blood_pressure_btn);
        mBloodPressureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBloodPressureStarted) {
                    mDevicePerformer.openHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE);
                } else {
                    mDevicePerformer.closeHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE);
                }
            }
        });

        mRespiratoryRateBtn = (Button) findViewById(R.id.respiratory_rate_btn);
        mRespiratoryRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRespiratoryRateStarted) {
                    mDevicePerformer.openHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE);
                } else {
                    mDevicePerformer.closeHealthyRealTimeData(IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE);
                }
            }
        });
    }

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onResponseWristbandConfig(WristbandConfig config) {
            WristbandVersion version = config.getWristbandVersion();
            if (version.isHeartRateEnable()) {
                findViewById(R.id.heart_rate_layout).setVisibility(View.VISIBLE);
            }

            if (version.isOxygenEnable()) {
                findViewById(R.id.oxygen_layout).setVisibility(View.VISIBLE);
            }

            if (version.isBloodPressureEnable()) {
                findViewById(R.id.blood_pressure_layout).setVisibility(View.VISIBLE);
            }

            if (version.isRespiratoryRateEnable()) {
                findViewById(R.id.respiratory_rate_layout).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onOpenHealthyRealTimeData(int healthyType, boolean success) {
            if (success) {
                switch (healthyType) {
                    case IDevicePerformer.HEALTHY_TYPE_HEART_RATE:
                        mHeartRateStarted = true;
                        mHeartRateBtn.setText("Stop");
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_OXYGEN:
                        mOxygenStarted = true;
                        mOxygenBtn.setText("Stop");
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE:
                        mBloodPressureStarted = true;
                        mBloodPressureBtn.setText("Stop");
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE:
                        mRespiratoryRateStarted = true;
                        mRespiratoryRateBtn.setText("Stop");
                        break;
                }
            }
        }

        @Override
        public void onCloseHealthyRealTimeData(int healthyType) {
            switch (healthyType) {
                case IDevicePerformer.HEALTHY_TYPE_HEART_RATE:
                    mHeartRateStarted = false;
                    mHeartRateBtn.setText("Start");
                    break;

                case IDevicePerformer.HEALTHY_TYPE_OXYGEN:
                    mOxygenStarted = false;
                    mOxygenBtn.setText("Start");
                    break;

                case IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE:
                    mBloodPressureStarted = false;
                    mBloodPressureBtn.setText("Start");
                    break;

                case IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE:
                    mRespiratoryRateStarted = false;
                    mRespiratoryRateBtn.setText("Start");
                    break;
            }
        }

        @Override
        public void onResultHealthyRealTimeData(int heartRate, int oxygen, int diastolicPressure, int systolicPressure, int respiratoryRate) {
            if (heartRate != 0) {
                mHeartRateTv.setText("Heart rate:" + heartRate);
            }

            if (oxygen != 0) {
                mOxygenTv.setText("Oxygen:" + oxygen);
            }

            if (diastolicPressure != 0 && systolicPressure != 0) {
                mBloodPressureTv.setText("Blood pressure:" + diastolicPressure + "      " + systolicPressure);
            }
            if (respiratoryRate != 0) {
                mRespiratoryRateTv.setText("Respiratory rate:" + respiratoryRate);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }
}
