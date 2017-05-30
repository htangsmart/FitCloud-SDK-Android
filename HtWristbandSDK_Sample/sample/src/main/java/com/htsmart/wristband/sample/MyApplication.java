package com.htsmart.wristband.sample;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.sample.bean.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.imengya.bluetoothle.connector.TryTimeStrategy;

/**
 * Created by Kilnn on 16-10-5.
 */
public class MyApplication extends WristbandApplication {

    private static MyApplication INSTANCE;
    private User mFakeUser;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(INSTANCE);
        initUser();

        WristbandApplication.setDebugEnable(true);
        WristbandApplication.getDeviceScanner().setScanPeriods(15 * 1000);
        WristbandApplication.getDeviceConnector().setTryTimeStrategy(new TryTimeStrategy() {
            @Override
            public int nextTryTimes(int i) {
                return 1000;
            }
        });
    }

    private void initUser() {
        mFakeUser = new User();
        mFakeUser.setId(1);
        mFakeUser.setHeight(170);
        mFakeUser.setWeight(65);
        try {
            mFakeUser.setBirthday(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("1990-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mFakeUser.setWearLeft(true);
    }

    private NotificationConfig mNotificationConfig;

    private void loadConfig() {
        String value = mSharedPreferences.getString("NotificationConfig", "");
        mNotificationConfig = new NotificationConfig(value.getBytes());
    }

    public void setNotificationConfig(NotificationConfig config) {
        mSharedPreferences.edit().putString("NotificationConfig", new String(config.getValues())).apply();
        mNotificationConfig = config;
    }

    public NotificationConfig getNotificationConfig() {
        if (mNotificationConfig == null) {
            loadConfig();
        }
        return mNotificationConfig;
    }

    public static MyApplication getInstance() {
        return INSTANCE;
    }

    public User getUser() {
        return mFakeUser;
    }
}
