package com.htsmart.wristband_smaple.alarmclock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.htsmart.wristband.bean.WristbandAlarm;
import com.htsmart.wristband_smaple.R;

import cn.imengya.wheelview.WheelView;
import cn.imengya.wheelview.adapters.NumericWheelAdapter;


/**
 * Add or edit alarm clock.
 */
public class AlarmClockEditActivity extends AppCompatActivity {

    public static final String EXTRA_ALARM = "alarm";//Alarm clock
    public static final String EXTRA_ALARM_ID = "alarm_id";//Alarm clock id
    public static final int RESULT_COMPLETED = 100;
    public static final int RESULT_DELETE = 101;

    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;
    private TextView mRepeatTv;

    private WristbandAlarm mAlarm;
    private boolean mEdit;//Edit or add

    private CharSequence[] mDayValuesSimple = null;

    private int[] mRepeatFlags = new int[]{
            WristbandAlarm.REPEAT_FLAG_MON,
            WristbandAlarm.REPEAT_FLAG_TUE,
            WristbandAlarm.REPEAT_FLAG_WED,
            WristbandAlarm.REPEAT_FLAG_THU,
            WristbandAlarm.REPEAT_FLAG_FRI,
            WristbandAlarm.REPEAT_FLAG_SAT,
            WristbandAlarm.REPEAT_FLAG_SUN,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock_edit);

        getSupportActionBar().setTitle(R.string.alarm_clock_edit);

        mDayValuesSimple = new CharSequence[]{
                getString(R.string.repeat_00_simple),
                getString(R.string.repeat_01_simple),
                getString(R.string.repeat_02_simple),
                getString(R.string.repeat_03_simple),
                getString(R.string.repeat_04_simple),
                getString(R.string.repeat_05_simple),
                getString(R.string.repeat_06_simple),
        };

        int alarmId = 0;
        if (getIntent() != null) {
            mAlarm = getIntent().getParcelableExtra(EXTRA_ALARM);
            if (mAlarm == null) {
                alarmId = getIntent().getIntExtra(EXTRA_ALARM_ID, 0);
            }
        }

        if (mAlarm == null) {
            mEdit = false;
            mAlarm = new WristbandAlarm();
            mAlarm.setAlarmId(alarmId);
            mAlarm.setEnable(true);
        } else {
            mEdit = true;
        }

        mHourWheelView = (WheelView) findViewById(R.id.hour_wheel_view);
        mMinuteWheelView = (WheelView) findViewById(R.id.minute_wheel_view);
        mRepeatTv = (TextView) findViewById(R.id.repeat_tv);

        findViewById(R.id.repeat_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmClockEditActivity.this, AlarmClockRepeatActivity.class);
                intent.putExtra(AlarmClockRepeatActivity.EXTRA_REPEAT, mAlarm.getRepeat());
                startActivityForResult(intent, 1);
            }
        });

        View delete_layout = findViewById(R.id.delete_layout);
        if (mEdit) {
            delete_layout.setVisibility(View.VISIBLE);
            delete_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog();
                }
            });
        } else {
            delete_layout.setVisibility(View.GONE);
        }

        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(this, 0, 23, "%02d");
        NumericWheelAdapter minuteAdapter = new NumericWheelAdapter(this, 0, 59, "%02d");

        mHourWheelView.setVisibleItems(7);
        mHourWheelView.setWheelBackground(android.R.color.white);
        mHourWheelView.setViewAdapter(hourAdapter);

        mMinuteWheelView.setVisibleItems(7);
        mMinuteWheelView.setWheelBackground(android.R.color.white);
        mMinuteWheelView.setViewAdapter(minuteAdapter);

        updateUI();
    }

    private void updateUI() {
        int hour = mAlarm.getHour();
        int minute = mAlarm.getMinute();

        if (hour == 24 && minute == 0) {
            mHourWheelView.setCurrentItem(23);
            mMinuteWheelView.setCurrentItem(59);
        } else {
            mHourWheelView.setCurrentItem(hour);
            mMinuteWheelView.setCurrentItem(minute);
        }

        mRepeatTv.setText(repeatToSimpleStr(mAlarm.getRepeat()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one_text, menu);
        menu.findItem(R.id.menu_text1).setTitle(R.string.save);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_text1) {
            completed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.prompt)
                .setMessage(R.string.delete_clock_tip_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                    }
                })
                .create().show();
    }

    private void delete() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ALARM_ID, mAlarm.getAlarmId());
        setResult(RESULT_DELETE, intent);
        finish();
    }

    private void completed() {
        Intent intent = new Intent();
        mAlarm.setHour(mHourWheelView.getCurrentItem());
        mAlarm.setMinute(mMinuteWheelView.getCurrentItem());
        intent.putExtra(EXTRA_ALARM, mAlarm);
        setResult(RESULT_COMPLETED, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            mAlarm.setRepeat(data.getIntExtra(AlarmClockRepeatActivity.EXTRA_REPEAT, 0));
            mRepeatTv.setText(repeatToSimpleStr(mAlarm.getRepeat()));
        }
    }


    //和AlarmListActivity里的方法一致
    private String repeatToSimpleStr(int repeat) {
        String text = null;
        int sumDays = 0;
        String resultString = "";
        for (int i = 0; i < mRepeatFlags.length; i++) {
            if (WristbandAlarm.isRepeatEnable(repeat, mRepeatFlags[i])) {
                sumDays++;
                resultString += (mDayValuesSimple[i] + " ");
            }
        }
        if (sumDays == 7) {
            text = getString(R.string.every_day);
        } else if (sumDays == 0) {
            text = getString(R.string.never);
        } else if (sumDays == 5) {
            boolean sat = !WristbandAlarm.isRepeatEnable(repeat, WristbandAlarm.REPEAT_FLAG_SAT);
            boolean sun = !WristbandAlarm.isRepeatEnable(repeat, WristbandAlarm.REPEAT_FLAG_SUN);
            if (sat && sun) {
                text = getString(R.string.working_days);
            }
        }

        if (text == null) {
            text = resultString;
        }

        return text;
    }
}
