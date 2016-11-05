package com.htsmart.wristband_smaple.alarmclock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandAlarm;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.performer.PerformerListener;
import com.htsmart.wristband_smaple.R;
import com.htsmart.wristband_smaple.SimplePerformerListener;
import com.htsmart.wristband_smaple.util.FastViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Alarm Clocks.
 */
public class AlarmClocksActivity extends AppCompatActivity {

    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();

    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<WristbandAlarm> mAlarmList;

    private CharSequence[] mDayValuesSimple;

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
        setContentView(R.layout.activity_alarm_clocks);

        getSupportActionBar().setTitle(R.string.alarm_clock);

        mDayValuesSimple = new CharSequence[]{
                getString(R.string.repeat_00_simple),
                getString(R.string.repeat_01_simple),
                getString(R.string.repeat_02_simple),
                getString(R.string.repeat_03_simple),
                getString(R.string.repeat_04_simple),
                getString(R.string.repeat_05_simple),
                getString(R.string.repeat_06_simple),
        };

        mListView = (ListView) findViewById(R.id.list_view);

        mAlarmList = new ArrayList<>();
        mAdapter = new ListViewAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AlarmClocksActivity.this, AlarmClockEditActivity.class);
                intent.putExtra(AlarmClockEditActivity.EXTRA_ALARM, mAlarmList.get(position));
                startActivityForResult(intent, 1);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(mAlarmList.get(position).getAlarmId());
                return true;
            }
        });

        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Find available alarmId
                int alarmId = -1;
                for (int i = WristbandAlarm.ALARM_ID_MIN; i <= WristbandAlarm.ALARM_ID_MAX; i++) {
                    boolean used = false;
                    for (WristbandAlarm alarm : mAlarmList) {
                        if (i == alarm.getAlarmId()) {
                            used = true;
                            break;
                        }
                    }
                    if (!used) {
                        alarmId = i;
                        break;
                    }
                }
                if (alarmId == -1) {//No available alarmId
                    Toast.makeText(AlarmClocksActivity.this, R.string.clock_limit_8, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(AlarmClocksActivity.this, AlarmClockEditActivity.class);
                intent.putExtra(AlarmClockEditActivity.EXTRA_ALARM_ID, alarmId);
                startActivityForResult(intent, 1);
            }
        });

        mDevicePerformer.addPerformerListener(mPerformerListener);

        if (!mDevicePerformer.cmd_requestAlarmList()) {
            Toast.makeText(this, "cmd_requestAlarmList failed", Toast.LENGTH_SHORT).show();
        }
    }

    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onCommandSend(boolean success, int commandType) {
            if (commandType == PerformerListener.TYPE_REQUEST_ALARM_LIST) {
                if (!success) {
                    Toast.makeText(AlarmClocksActivity.this, "cmd_requestAlarmList failed", Toast.LENGTH_SHORT).show();
                }
            } else if (commandType == PerformerListener.TYPE_SET_ALARM_LIST) {
                if (success) {
                    Toast.makeText(AlarmClocksActivity.this, "cmd_setAlarmList success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AlarmClocksActivity.this, "cmd_setAlarmList failed", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onResponseAlarmList(List<WristbandAlarm> alarms) {
            Toast.makeText(AlarmClocksActivity.this, "cmd_requestAlarmList success", Toast.LENGTH_SHORT).show();

            if (alarms != null && alarms.size() > 0) {
                mAlarmList.clear();
                mAlarmList.addAll(alarms);
                sortAlarmList();
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }

    private void showDeleteDialog(final int alarmId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.prompt)
                .setMessage(R.string.delete_clock_tip_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteByAlarmId(alarmId);
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .create().show();
    }

    private void deleteByAlarmId(int alarmId) {
        for (WristbandAlarm alarm : mAlarmList) {
            if (alarm.getAlarmId() == alarmId) {
                mAlarmList.remove(alarm);
                break;
            }
        }
    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlarmList.size();
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
                convertView = LayoutInflater.from(AlarmClocksActivity.this).inflate(R.layout.alarm_item, parent, false);
            }
            TextView time_tv = FastViewHolder.get(convertView, R.id.time_tv);
            TextView repeat_tv = FastViewHolder.get(convertView, R.id.repeat_tv);
            SwitchCompat switchCompat = FastViewHolder.get(convertView, R.id.open_switch);
            final WristbandAlarm alarm = mAlarmList.get(position);
            time_tv.setText(alarm.getHour() + ":" + String.format("%02d", alarm.getMinute()));
            repeat_tv.setText(repeatToSimpleStr(alarm.getRepeat()));
            switchCompat.setOnCheckedChangeListener(null);

            //No repeat, then to judge whether it is overdue
            if (alarm.getRepeat() == 0) {
                Date date = new Date();
                Date alarmDate = new Date(alarm.getYear() - 1900, alarm.getMonth(), alarm.getDay(), alarm.getHour(), alarm.getMinute(), 0);
                if (date.getTime() > alarmDate.getTime()) {//Overdue, set enable false.
                    alarm.setEnable(false);
                }
            }

            switchCompat.setChecked(alarm.isEnable());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    alarm.setEnable(isChecked);
                }
            });
            return convertView;
        }
    }


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && data != null) {
            if (resultCode == AlarmClockEditActivity.RESULT_COMPLETED) {
                WristbandAlarm newAlarm = data.getParcelableExtra(AlarmClockEditActivity.EXTRA_ALARM);
                if (newAlarm != null) {
                    deleteByAlarmId(newAlarm.getAlarmId());
                    mAlarmList.add(newAlarm);
                    sortAlarmList();
                    mAdapter.notifyDataSetChanged();
                }
            } else if (resultCode == AlarmClockEditActivity.RESULT_DELETE) {
                int alarmId = data.getIntExtra(AlarmClockEditActivity.EXTRA_ALARM_ID, -1);
                if (alarmId != -1) {
                    deleteByAlarmId(alarmId);
                    sortAlarmList();
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void sortAlarmList() {
        if (mAlarmList.size() > 0) {
            Collections.sort(mAlarmList, alarmComparator);
        }
    }

    private Comparator<WristbandAlarm> alarmComparator = new Comparator<WristbandAlarm>() {
        @Override
        public int compare(WristbandAlarm lhs, WristbandAlarm rhs) {
            int v1 = lhs.getHour() * 60 + lhs.getMinute();
            int v2 = rhs.getHour() * 60 + rhs.getMinute();
            if (v1 > v2) {
                return 1;
            } else if (v1 < v2) {
                return -1;
            } else {
                return lhs.getAlarmId() - rhs.getAlarmId();
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
            setAlarmList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAlarmList() {
        for (WristbandAlarm alarm : mAlarmList) {
            GregorianCalendar calendar = new GregorianCalendar();

            /**
             * If not set repeat,
             */
            if (alarm.getRepeat() == 0) {//没有循环，那么只设置了一次
                int minute = alarm.getHour() * 60 + alarm.getMinute();
                int currentMinute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                if (minute <= currentMinute) {
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
            }

            alarm.setYear(calendar.get(Calendar.YEAR));
            alarm.setMonth(calendar.get(Calendar.MONTH));
            alarm.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        }

        if (!mDevicePerformer.cmd_setAlarmList(mAlarmList)) {
            Toast.makeText(AlarmClocksActivity.this, "cmd_setAlarmList failed", Toast.LENGTH_SHORT).show();
        }

    }

}
