package com.htsmart.wristband.sample.notification;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandNotification;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.sample.MyApplication;


public class SmsPhoneReceiver extends BroadcastReceiver {
    public static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsPhoneReceiver";
    public static volatile String sPhoneNumber = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationConfig config = MyApplication.getInstance().getNotificationConfig();

        if (intent.getAction().equals(SMS_ACTION)) {//SMS
            if (config.isFlagEnable(NotificationConfig.FLAG_SMS)) {
                progressSms(context, intent);
            }
        } else if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {// Outgoing call
            //do nothing
        } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) { // Incoming call
            if (config.isFlagEnable(NotificationConfig.FLAG_TELEPHONE)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                Log.e(TAG, "incomingNumber:" + incomingNumber);

                if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                    if (!TextUtils.isEmpty(sPhoneNumber)) {
                        notice(WristbandNotification.TYPE_TELEPHONE_REJECT, sPhoneNumber, null);
                        sPhoneNumber = null;
                    }
                } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                    if (!TextUtils.isEmpty(sPhoneNumber)) {
                        notice(WristbandNotification.TYPE_TELEPHONE_LISTENED, sPhoneNumber, null);
                        sPhoneNumber = null;
                    }
                } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                    if (!TextUtils.isEmpty(incomingNumber)) {// 输出来电号码
                        if (TextUtils.isEmpty(sPhoneNumber)) {
                            String name = getPeople(context, incomingNumber);
                            sPhoneNumber = TextUtils.isEmpty(name) ? incomingNumber : name;
                            notice(WristbandNotification.TYPE_TELEPHONE_COMING, sPhoneNumber, null);
                        }
                    }
                }

            }
        }
    }

    private static void progressSms(Context context, Intent intent) {
        SmsMessage[] messages = getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;
        for (SmsMessage message : messages) {
            String phone = message.getOriginatingAddress();
            if (TextUtils.isEmpty(phone)) {
                continue;
            }
            String name = getPeople(context, phone);
            notice(WristbandNotification.TYPE_SMS, TextUtils.isEmpty(name) ? phone : name, message.getMessageBody());
        }
    }

    private static void notice(byte type, String name, String body) {
        WristbandNotification notification = new WristbandNotification();
        notification.setType(type);
        notification.setName(name);
        notification.setContent(body);
        WristbandApplication.getDevicePerformer().cmd_sendWristbandNotification(notification);
    }

    private static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        if (messages == null || messages.length == 0) return null;
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

    private static String getPeople(Context context, String phoneNumber) {
        String name = null;
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cur != null) {
            try {
                if (cur.getCount() > 0) {
                    if (cur.moveToFirst()) {
                        name = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                cur.close();
            }
        }
        return name;
    }

}
