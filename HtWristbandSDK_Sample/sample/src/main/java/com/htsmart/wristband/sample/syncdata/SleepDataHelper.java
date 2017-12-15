package com.htsmart.wristband.sample.syncdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.htsmart.wristband.bean.SleepTotalData;
import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.sample.MyApplication;
import com.htsmart.wristband.sample.bean.User;
import com.htsmart.wristband.sample.syncdata.db.DbHelper;
import com.htsmart.wristband.sample.syncdata.entity.SyncRawDataEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Kilnn on 2017/12/12.
 */

public class SleepDataHelper {
    private static final String TAG = "SleepDataHelper";

    private static final int SLEEP_TIME_INTERVAL = 5 * 60;//Sleep data interval time
    private static final int SLEEP_START_STATUS = SyncRawData.SLEEP_STATUS_SHALLOW;//Sleep start status


    public static List<SleepDayData> getSleepDetail(Context context, Date date) {
        //Get the timeStamp of this date
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStartTimeStamp = cal.getTimeInMillis();

        //last night sleep start time 21:30
        cal.set(Calendar.HOUR_OF_DAY, -3);
        cal.set(Calendar.MINUTE, 30);
        int start = (int) (cal.getTime().getTime() / 1000);

        //today sleep end time 12:00
        cal.setTimeInMillis(dayStartTimeStamp);//reset to dayStartTimeStamp
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        int end = (int) (cal.getTime().getTime() / 1000);

        User user = MyApplication.getInstance().getUser();
        DbHelper dbHelper = DbHelper.getInstance(context);

        //Get the range of actual sleep time
        SyncRawDataEntity sleepStart = dbHelper.getSleepStart(user.getId(), start, end);
        if (sleepStart == null) {
            return null;
        }
        SyncRawDataEntity sleepEnd = dbHelper.getSleepEnd(user.getId(), sleepStart.getTime(), end);
        if (sleepEnd == null) {
            return null;
        }

        //Query all sleep datas
        List<SyncRawDataEntity> tempDatas = dbHelper.getSleepDataBetween(user.getId(), sleepStart.getTime(), sleepEnd.getTime());
        if (tempDatas == null || tempDatas.size() <= 0) {
            return null;
        }

        List<SleepDayData> results = new ArrayList<>();

        SleepDayData data = null;
        int soberCount = Integer.MAX_VALUE;
        int previousEndTime = start;

        for (int i = 0; i < tempDatas.size(); i++) {
            SyncRawDataEntity dataEntity = tempDatas.get(i);

            if (dataEntity.getValue() < SyncRawData.SLEEP_STATUS_DEEP || dataEntity.getValue() > SyncRawData.SLEEP_STATUS_SOBER) {
                Log.e(TAG, "Error sleep data--> time:" + dataEntity.getTime() + "  value:" + dataEntity.getValue());
                dataEntity.setValue(SLEEP_START_STATUS);
            }

            if (data != null) {//Have a previous sleep segment
                if (data.value == dataEntity.getValue()) {//Sleep status equals,merge the two sleep point
                    data.endTime = dataEntity.getTime();//Move the end sleep time to this point
                } else {
                    //Save this data
                    results.add(data);

                    //reset
                    previousEndTime = data.endTime;//The previous point end time
                    data = null;
                }
            }

            if (data == null) {//Don't have a previous sleep segment, need create a new sleep segment
                //Does it need to construct a virtual sleep segment
                if (soberCount >= 4) {
                    data = new SleepDayData();
                    int startTime = dataEntity.getTime() - 5 * SLEEP_TIME_INTERVAL;
                    if (results.size() > 0 &&
                            results.get(results.size() - 1).value == SyncRawData.SLEEP_STATUS_SOBER) {
                        SleepDayData previousData = results.get(results.size() - 1);//上1个数据
                        if (startTime < previousData.startTime) {
                            startTime = previousData.startTime;
                        }
                        previousData.endTime = startTime;
                    } else if (startTime < previousEndTime) {
                        startTime = previousEndTime;
                    }
                    data.startTime = startTime;

                    int endTime = startTime + 4 * SLEEP_TIME_INTERVAL;
                    if (endTime >= dataEntity.getTime()) {
                        endTime = dataEntity.getTime() - 1;
                    }
                    data.endTime = endTime;
                    if (data.endTime < data.startTime) {
                        data.endTime = data.startTime;
                    }

                    data.value = SLEEP_START_STATUS;

                    if (data.value != dataEntity.getValue()) {//Sleep status not equals, finish this sleep segment
                        results.add(data);

                        //reset
                        previousEndTime = data.endTime;//The previous point end time
                        data = null;
                    }
                }

                if (data == null) {//Create a new sleep segment
                    data = new SleepDayData();
                    data.startTime = previousEndTime;
                    data.value = dataEntity.getValue();
                }

                data.endTime = dataEntity.getTime();
            }

            //If this is the last data
            if (i == tempDatas.size() - 1) {
                //Save data
                results.add(data);
            }

            if (dataEntity.getValue() == SyncRawData.SLEEP_STATUS_SOBER) {
                soberCount++;
            } else {
                soberCount = 0;
            }
        }

        //Calculation specific gravity
        if (results.size() > 0) {
            float totalTime = results.get(results.size() - 1).endTime - results.get(0).startTime;

            for (int i = 0; i < results.size(); i++) {
                SleepDayData d = results.get(i);
                if (d.endTime <= d.startTime) {
                    results.remove(i);
                    i--;
                } else {
                    d.percent = (d.endTime - d.startTime) / totalTime;
                }
            }
        }
        return results;
    }

    public static SleepTotalData getSleepTotalData(Context context, Date date) {
        User user = MyApplication.getInstance().getUser();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("SleepTotalData" + user.getId(), null);
        if (TextUtils.isEmpty(json)) return null;
        List<SleepTotalData> datas = JSON.parseArray(json, SleepTotalData.class);
        if (datas == null || datas.size() <= 0) return null;

        //Get the timeStamp of this date
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        //en:Because the sleep is start at last night, and the SleepTotalData's timeStamp is the zero point of yesterday，In order to contrast, move the date forward for a day
        //中文:因为睡眠是昨晚发生, 并且SleepTotalData的时间戳是昨天零点的时间，为了对比，所以把日期往前挪一天
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        long dayStartTimeStamp = cal.getTimeInMillis();

        for (SleepTotalData sleepTotalData : datas) {
            if (sleepTotalData == null) continue;
            if (sleepTotalData.getTimeStamp() == dayStartTimeStamp) {
                return sleepTotalData;
            }
        }
        return null;
    }

    public static void setSleepTotalData(Context context, List<SleepTotalData> datas) {
        User user = MyApplication.getInstance().getUser();
        //en:As a matter of simplicity, there is a direct coverage of the data. In actual processing, you may need to create or update the data with a timestamp as the primary key.
        //中文:简单起见，这里直接覆盖保存数据。实际处理的时候可能需要以时间戳为主键创建或者更新数据。
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("SleepTotalData" + user.getId(), JSON.toJSONString(datas)).apply();
    }

}
