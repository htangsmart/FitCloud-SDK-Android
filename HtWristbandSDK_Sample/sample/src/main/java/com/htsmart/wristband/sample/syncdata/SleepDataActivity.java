package com.htsmart.wristband.sample.syncdata;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.htsmart.wristband.bean.SleepTotalData;
import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.sample.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Kilnn on 2017/12/12.
 * en:Because sleep data is relatively complex.So this example is provided to show how to parse sleep data.
 * 1.The data of the 5 minute node can be used to analyze the detailed process of sleep.
 * 2.7 days of sleep history, because the hand rings only preserve the detailed data of the latest sleep. So if you don't get the detailed data, you can get a statistical data from here.
 * 3.Sleep data in the total data of the day. Because there are 7 days of sleep history, this can not be used.
 * <p>
 * 中文:因为睡眠数据相对来说更加复杂，所以这里提供一个例子来展示如果去解析睡眠数据。睡眠部分的数据分为三部分：
 * 一，5分钟节点的数据，可以从里面解析出睡眠的详细过程。
 * 二，7天的睡眠历史记录，因为手环只会保存最近一次的睡眠的详细数据。所以如果没有得到详细数据，可以从这里获取一个统计的数据。
 * 三，当天总数据中的睡眠数据。因为有7天的睡眠历史记录，所以这个其实可以不使用。
 */

public class SleepDataActivity extends AppCompatActivity {

    private SleepDayView mSleepDayView;
    private TextView mTvDate;
    private TextView mTvNoSleepDetail;
    private TextView mTvSleepTotal;
    private TextView mTvSleepDeep;
    private TextView mTvSleepShallow;


    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_data);

        mTvDate = findViewById(R.id.tv_date);
        mSleepDayView = findViewById(R.id.sleep_day_view);
        mTvNoSleepDetail = findViewById(R.id.tv_no_sleep_detail);
        mTvSleepTotal = findViewById(R.id.tv_sleep_total);
        mTvSleepDeep = findViewById(R.id.tv_sleep_deep);
        mTvSleepShallow = findViewById(R.id.tv_sleep_shallow);

        findViewById(R.id.btn_select)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectDate();
                    }
                });

        showSleepData(new Date());
    }


    private void showSleepData(Date date) {
        mTvDate.setText(mSimpleDateFormat.format(date));

        List<SleepDayData> sleepDayDataList = SleepDataHelper.getSleepDetail(this, date);
        int totalTime = 0, deepTime = 0, shallowTime = 0;

        if (sleepDayDataList == null || sleepDayDataList.size() <= 0) {//no sleep detail
            mSleepDayView.setVisibility(View.GONE);
            mTvNoSleepDetail.setVisibility(View.VISIBLE);

            //If don't have detail data , try to get the total data for display.
            SleepTotalData sleepTotalData = SleepDataHelper.getSleepTotalData(this, date);
            if (sleepTotalData != null) {
                deepTime = sleepTotalData.getDeepSleep();
                shallowTime = sleepTotalData.getShallow();
                totalTime = deepTime + shallowTime;
            }
        } else {
            mSleepDayView.setVisibility(View.VISIBLE);
            mTvNoSleepDetail.setVisibility(View.GONE);
            SleepDayData[] arrs = new SleepDayData[sleepDayDataList.size()];
            for (int i = 0; i < arrs.length; i++) {
                arrs[i] = sleepDayDataList.get(i);
            }
            mSleepDayView.setSleepDayDatas(arrs);

            for (int i = 0; i < sleepDayDataList.size(); i++) {
                SleepDayData dayData = sleepDayDataList.get(i);
                int t = dayData.endTime - dayData.startTime;
                if (dayData.value == SyncRawData.SLEEP_STATUS_DEEP) {
                    deepTime += t;
                } else if (dayData.value == SyncRawData.SLEEP_STATUS_SHALLOW) {
                    shallowTime += t;
                }
            }
            totalTime = deepTime + shallowTime;
        }

        mTvSleepTotal.setText(getString(R.string.last_night_total_sleep, getHour(totalTime), getMinute(totalTime)));
        mTvSleepDeep.setText(getString(R.string.last_night_deep_sleep, getHour(deepTime), getMinute(deepTime)));
        mTvSleepShallow.setText(getString(R.string.last_night_shallow_sleep, getHour(shallowTime), getMinute(shallowTime)));
    }

    public static int getHour(int seconds) {
        return seconds / 3600;
    }

    public static int getMinute(int seconds) {
        return (seconds % 3600) / 60;
    }


    private void selectDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.set(Calendar.YEAR, i);
                calendar.set(Calendar.MONTH, i1);
                calendar.set(Calendar.DAY_OF_MONTH, i2);
                showSleepData(calendar.getTime());
            }
        }, 2017, 11, 12);
        datePickerDialog.show();
    }
}
