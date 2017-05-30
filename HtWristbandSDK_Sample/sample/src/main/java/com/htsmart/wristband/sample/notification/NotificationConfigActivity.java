package com.htsmart.wristband.sample.notification;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.performer.PerformerListener;
import com.htsmart.wristband.sample.MyApplication;
import com.htsmart.wristband.sample.R;
import com.htsmart.wristband.sample.SimplePerformerListener;

/**
 * Sample only shows 4 configurations, the other configuration like here.
 */
public class NotificationConfigActivity extends AppCompatActivity {

    private SwitchCompat mCallSwitch;
    private SwitchCompat mSMSSwitch;
    private SwitchCompat mQQSwitch;
    private SwitchCompat mWeChatSwitch;

    private NotificationConfig mNotificationConfig = MyApplication.getInstance().getNotificationConfig();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_config);

        mCallSwitch = (SwitchCompat) findViewById(R.id.call_switch);
        mSMSSwitch = (SwitchCompat) findViewById(R.id.sms_switch);
        mQQSwitch = (SwitchCompat) findViewById(R.id.qq_switch);
        mWeChatSwitch = (SwitchCompat) findViewById(R.id.wechat_switch);

        mCallSwitch.setChecked(mNotificationConfig.isFlagEnable(NotificationConfig.FLAG_TELEPHONE));
        mSMSSwitch.setChecked(mNotificationConfig.isFlagEnable(NotificationConfig.FLAG_SMS));
        mQQSwitch.setChecked(mNotificationConfig.isFlagEnable(NotificationConfig.FLAG_QQ));
        mWeChatSwitch.setChecked(mNotificationConfig.isFlagEnable(NotificationConfig.FLAG_WECHAT));

        WristbandApplication.getDevicePerformer().addPerformerListener(mPerformerListener);

        if (!CaptureNotificationService.isEnabled(this)) {
            CaptureNotificationService.openNotificationAccess(this);
        }
    }

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onCommandSend(boolean success, int commandType) {
            if (commandType == PerformerListener.TYPE_SET_NOTIFICATION_CONFIG) {
                if (success) {
                    Toast.makeText(NotificationConfigActivity.this, "cmd_setNotificationConfig success", Toast.LENGTH_SHORT).show();
                    MyApplication.getInstance().setNotificationConfig(mNotificationConfig);
                } else {
                    Toast.makeText(NotificationConfigActivity.this, "cmd_setNotificationConfig failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one_text, menu);
        menu.findItem(R.id.menu_text1).setTitle(R.string.save);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_text1) {
            saveConfig();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveConfig() {
        mNotificationConfig.setFlagEnable(NotificationConfig.FLAG_TELEPHONE, mCallSwitch.isChecked());
        mNotificationConfig.setFlagEnable(NotificationConfig.FLAG_SMS, mSMSSwitch.isChecked());
        mNotificationConfig.setFlagEnable(NotificationConfig.FLAG_QQ, mQQSwitch.isChecked());
        mNotificationConfig.setFlagEnable(NotificationConfig.FLAG_WECHAT, mWeChatSwitch.isChecked());

        if (!WristbandApplication.getDevicePerformer().cmd_setNotificationConfig(mNotificationConfig)) {
            Toast.makeText(this, "cmd_setNotificationConfig failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WristbandApplication.getDevicePerformer().removePerformerListener(mPerformerListener);
    }
}
