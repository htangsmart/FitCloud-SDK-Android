package com.htsmart.wristband.sample.notification;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandNotification;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.sample.MyApplication;


/**
 * To capture the notice of the third party App
 */
public class CaptureNotificationService extends NotificationListenerService {
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    /**
     * Is NotificationListenerService enable.
     */
    public static boolean isEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void openNotificationAccess(Context context) {
        context.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    public static final String PACKAGE_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_WECHAT = "com.tencent.mm";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        String content = null;
        CharSequence sequence = sbn.getNotification().tickerText;
        if (sequence != null) {
            content = sequence.toString();
        }
        if (TextUtils.isEmpty(content)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Bundle bundle = sbn.getNotification().extras;
                if (bundle != null) {
                    content = bundle.getString(Notification.EXTRA_TEXT);
                }
            }
        }

        if (TextUtils.isEmpty(content)) return;

        String packageName = sbn.getPackageName();

        NotificationConfig config = MyApplication.getInstance().getNotificationConfig();
        byte noticeType = -1;

        //Only capture QQ and WeChat message.
        switch (packageName) {
            case PACKAGE_QQ:
                if (config.isFlagEnable(NotificationConfig.FLAG_QQ)) {
                    noticeType = WristbandNotification.TYPE_QQ;
                }
                break;

            case PACKAGE_WECHAT:
                if (config.isFlagEnable(NotificationConfig.FLAG_WECHAT)) {
                    noticeType = WristbandNotification.TYPE_WECHAT;
                }
                break;
        }

        if (noticeType == -1) {
            return;
        }

        WristbandNotification notification = new WristbandNotification();
        notification.setType(noticeType);
        notification.setContent(content);
        WristbandApplication.getDevicePerformer().sendWristbandNotification(notification);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

}
