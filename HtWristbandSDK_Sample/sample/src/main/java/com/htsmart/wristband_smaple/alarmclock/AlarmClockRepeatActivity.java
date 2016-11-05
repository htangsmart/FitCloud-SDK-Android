package com.htsmart.wristband_smaple.alarmclock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.htsmart.wristband.bean.WristbandAlarm;
import com.htsmart.wristband_smaple.R;
import com.htsmart.wristband_smaple.util.FastViewHolder;

/**
 * 闹钟周期
 * Created by taowencong on 15-11-9.
 */
public class AlarmClockRepeatActivity extends AppCompatActivity {

    public static final String EXTRA_REPEAT = "repeat";

    private int mRepeat;
    private InnerAdapter mAdapter;
    private CharSequence[] mDayValues = null;

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
        setContentView(R.layout.activity_alarm_clock_repeat);

        mDayValues = new CharSequence[]{
                getString(R.string.repeat_00),
                getString(R.string.repeat_01),
                getString(R.string.repeat_02),
                getString(R.string.repeat_03),
                getString(R.string.repeat_04),
                getString(R.string.repeat_05),
                getString(R.string.repeat_06),
        };

        if (getIntent() != null) {
            mRepeat = getIntent().getIntExtra(EXTRA_REPEAT, 0);
        }

        ListView listView = (ListView) findViewById(R.id.list_view);
        mAdapter = new InnerAdapter();
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRepeatEnable = WristbandAlarm.isRepeatEnable(mRepeat, mRepeatFlags[position]);
                mRepeat = WristbandAlarm.setRepeatEnable(mRepeat, mRepeatFlags[position], !isRepeatEnable);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class InnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AlarmClockRepeatActivity.this).inflate(R.layout.alarm_repeat_item, parent, false);
            }
            TextView text_tv = FastViewHolder.get(convertView, R.id.text_tv);
            ImageView select_img = FastViewHolder.get(convertView, R.id.select_img);
            text_tv.setText(mDayValues[position]);
            select_img.setVisibility(WristbandAlarm.isRepeatEnable(mRepeat, mRepeatFlags[position]) ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        completed();
    }

    private void completed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REPEAT, mRepeat);
        setResult(RESULT_OK, intent);
        finish();
    }

}
