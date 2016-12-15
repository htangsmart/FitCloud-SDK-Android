package com.htsmart.wristband_smaple;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.connector.ConnectorListener;
import com.htsmart.wristband.connector.IDeviceConnector;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband_smaple.alarmclock.AlarmClocksActivity;
import com.htsmart.wristband_smaple.bean.User;
import com.htsmart.wristband_smaple.cameracontrol.CameraControlActivity;
import com.htsmart.wristband_smaple.config.ConfigActivity;
import com.htsmart.wristband_smaple.dfu.DfuActivity;
import com.htsmart.wristband_smaple.notification.NotificationConfigActivity;
import com.htsmart.wristband_smaple.realtimedata.RealTimeDataActivity;
import com.htsmart.wristband_smaple.syncdata.SyncDataActivity;

/**
 * Created by Kilnn on 16-10-5.
 * Connect and Operation Activity
 */
public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "ConnectActivity";
    public static final String EXTRA_DEVICE = "device";

    public static final String ACTION_CONNECT_DEVICE = BuildConfig.APPLICATION_ID + ".action.connect_device";

    private BluetoothDevice mBluetoothDevice;
    private IDeviceConnector mDeviceConnector = WristbandApplication.getDeviceConnector();
    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    private TextView mStateTv;
    private Button mConnectBtn;

    private WristbandConfig mWristbandConfig;

    private User mUser = MyApplication.getInstance().getUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mBluetoothDevice = getIntent().getParcelableExtra(EXTRA_DEVICE);

        mDeviceConnector.addConnectorListener(mConnectorListener);
        mDevicePerformer.addPerformerListener(mPerformerListener);

        mStateTv = (TextView) findViewById(R.id.state_tv);
        mConnectBtn = (Button) findViewById(R.id.connect_btn);

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeviceConnector.isConnect()) {
                    mDeviceConnector.close();
                } else {
                    connect();
                }
            }
        });

        connect();

        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_CONNECT_DEVICE));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONNECT_DEVICE.equals(intent.getAction())) {
                connect();
            }
        }
    };

    private boolean isUserBound() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("user_bind" + mUser.getId(), false);
    }

    private void setUserBound(boolean bound) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean("user_bind" + mUser.getId(), bound).apply();
    }

    private void connect() {
        if (isUserBound()) {
            mDeviceConnector.connectWithLogin(mBluetoothDevice, MyApplication.getInstance().getUser());
        } else {
            mDeviceConnector.connectWithBind(mBluetoothDevice, MyApplication.getInstance().getUser());
        }
        mStateTv.setText(R.string.connecting);
        updateConnectBtn(true, false);
    }

    private ConnectorListener mConnectorListener = new ConnectorListener() {
        @Override
        public void onConnect(WristbandConfig config) {
            mStateTv.setText(R.string.connect);
            updateConnectBtn(false, true);
            mWristbandConfig = config;
            MyApplication.getInstance().setNotificationConfig(config.getNotificationConfig());
            setUserBound(true);
        }

        @Override
        public void onDisconnect(final boolean b, final boolean b1) {
            mStateTv.setText(R.string.disconnect);
            updateConnectBtn(true, true);
        }

        @Override
        public void onConnectFailed(final int i) {
            mStateTv.setText(R.string.connect_failed);
            updateConnectBtn(true, true);
        }
    };

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onCommandSend(boolean success, int commandType) {
            Log.e(TAG, "onCommandSend  success:" + success + "   commandType:" + commandType);
        }

        @Override
        public void onResponseBattery(int percentage, int charging) {
            Log.e(TAG, "onResponseBattery  percentage:" + percentage + "    charging:" + charging);
        }

        @Override
        public void onFindPhone() {
            Log.e(TAG, "onFindPhone");
        }

        @Override
        public void onUserUnBind(boolean success) {
            Log.e(TAG, "onUserUnBind  success:" + success);
            if (success) {
                setUserBound(false);
            }
        }

    };

    private void updateConnectBtn(boolean connect, boolean enable) {
        mConnectBtn.setText(connect ? R.string.connect : R.string.disconnect);
        mConnectBtn.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDeviceConnector.removeConnectorListener(mConnectorListener);
        mDevicePerformer.removePerformerListener(mPerformerListener);
        mDeviceConnector.close();
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 1.Alarm clock
     */
    public void alarmClocks(View view) {
        startActivity(new Intent(this, AlarmClocksActivity.class));
    }

    /**
     * 2.Notification
     */
    public void notification(View view) {
        startActivity(new Intent(this, NotificationConfigActivity.class));
    }

    /**
     * 3.Set User info
     */
    public void set_user_info(View view) {
        User user = MyApplication.getInstance().getUser();
        mDevicePerformer.cmd_setUserInfo(user.wristbandSex(), user.wristbandBirthday(), user.wristbandHeight(), user.wristbandWeight());
    }

    /**
     * 4.Set wear way
     */
    public void set_wear_way(View view) {
        mDevicePerformer.cmd_setWearWay(true);
    }

    /**
     * 5.Request battery
     */
    public void request_battery(View view) {
        mDevicePerformer.cmd_requestBattery();
    }

    /**
     * 6.Find wristband
     */
    public void find_wristband(View view) {
        mDevicePerformer.cmd_findWristband();
    }

    /**
     * 7 Set Weather
     */
    public void set_weather(View view) {
        if (mWristbandConfig != null && mWristbandConfig.getWristbandVersion().isWeatherEnable()) {
            mDevicePerformer.cmd_setWeather(30, 10, "深圳");
        }
    }

    /**
     * 8 Real time data
     */
    public void real_time_data(View view) {
        startActivity(new Intent(this, RealTimeDataActivity.class));
    }

    /**
     * 9 Camera control
     */
    public void camera_control(View view) {
        startActivity(new Intent(this, CameraControlActivity.class));
    }

    /**
     * 10 Sync data
     */
    public void sync_data(View view) {
        startActivity(new Intent(this, SyncDataActivity.class));
    }

    /**
     * 11 Unbind user
     */
    public void unbind_user(View view) {
        mDevicePerformer.userUnBind();
    }


    /**
     * 12 Wristband config
     */
    public void wristband_config(View view) {
        startActivity(new Intent(this, ConfigActivity.class));
    }

    /**
     * 13 DFU
     */
    public void dfu(View view) {
        startActivity(new Intent(this, DfuActivity.class));
    }
}
