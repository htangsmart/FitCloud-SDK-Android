package com.htsmart.wristband.sample.realtimedata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.bean.WristbandVersion;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.sample.R;
import com.htsmart.wristband.sample.SimplePerformerListener;

/**
 * Created by Kilnn on 16-10-26.
 * <p>
 * en:Because the hardware may be different, so you should request and check the wristband config
 * before using real time testing.
 * </p>
 * <p>
 * zh-rCN:因为不同的手环可能硬件模块不一样，所以在开始实时测量之前，请先获取并检测手环的配置。
 * </p>
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
                mHeartRateTv.setText(getString(R.string.heart_rate_value, 0));
            }

            if (version.isOxygenEnable()) {
                findViewById(R.id.oxygen_layout).setVisibility(View.VISIBLE);
                mOxygenTv.setText(getString(R.string.oxygen_value, 0));
            }

            if (version.isBloodPressureEnable()) {
                findViewById(R.id.blood_pressure_layout).setVisibility(View.VISIBLE);
                mBloodPressureTv.setText(getString(R.string.blood_pressure_value, 0, 0));
            }

            if (version.isRespiratoryRateEnable()) {
                findViewById(R.id.respiratory_rate_layout).setVisibility(View.VISIBLE);
                mRespiratoryRateTv.setText(getString(R.string.respiratory_rate_value, 0));
            }
        }

        @Override
        public void onOpenHealthyRealTimeData(int healthyType, boolean success) {
            if (success) {
                switch (healthyType) {
                    case IDevicePerformer.HEALTHY_TYPE_HEART_RATE:
                        mHeartRateStarted = true;
                        mHeartRateBtn.setText(R.string.real_time_data_stop);
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_OXYGEN:
                        mOxygenStarted = true;
                        mOxygenBtn.setText(R.string.real_time_data_stop);
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE:
                        mBloodPressureStarted = true;
                        mBloodPressureBtn.setText(R.string.real_time_data_stop);
                        break;

                    case IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE:
                        mRespiratoryRateStarted = true;
                        mRespiratoryRateBtn.setText(R.string.real_time_data_stop);
                        break;
                }
            }
        }

        @Override
        public void onCloseHealthyRealTimeData(int healthyType) {
            switch (healthyType) {
                case IDevicePerformer.HEALTHY_TYPE_HEART_RATE:
                    mHeartRateStarted = false;
                    mHeartRateBtn.setText(R.string.real_time_data_start);
                    break;

                case IDevicePerformer.HEALTHY_TYPE_OXYGEN:
                    mOxygenStarted = false;
                    mOxygenBtn.setText(R.string.real_time_data_start);
                    break;

                case IDevicePerformer.HEALTHY_TYPE_BLOOD_PRESSURE:
                    mBloodPressureStarted = false;
                    mBloodPressureBtn.setText(R.string.real_time_data_start);
                    break;

                case IDevicePerformer.HEALTHY_TYPE_RESPIRATORY_RATE:
                    mRespiratoryRateStarted = false;
                    mRespiratoryRateBtn.setText(R.string.real_time_data_start);
                    break;
            }
        }

        @Override
        public void onResultHealthyRealTimeData(int heartRate, int oxygen, int diastolicPressure, int systolicPressure, int respiratoryRate) {
            if (heartRate != 0) {
                mHeartRateTv.setText(getString(R.string.heart_rate_value, heartRate));
            }

            if (oxygen != 0) {
                mOxygenTv.setText(getString(R.string.oxygen_value, oxygen));
            }

            if (diastolicPressure != 0 && systolicPressure != 0) {
                mBloodPressureTv.setText(getString(R.string.blood_pressure_value, diastolicPressure, systolicPressure));
            }
            if (respiratoryRate != 0) {
                mRespiratoryRateTv.setText(getString(R.string.respiratory_rate_value, respiratoryRate));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one_text, menu);
        menu.findItem(R.id.menu_text1).setTitle("ECG");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_text1) {
            startActivity(new Intent(this, EcgActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }
}
