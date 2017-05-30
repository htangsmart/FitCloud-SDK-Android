package com.htsmart.wristband.sample.config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htsmart.wristband.WristbandApplication;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.bean.WristbandVersion;
import com.htsmart.wristband.bean.config.PageConfig;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.performer.PerformerListener;
import com.htsmart.wristband.sample.R;
import com.htsmart.wristband.sample.SimplePerformerListener;
import com.htsmart.wristband.sample.util.FastViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Page config.
 */
public class ConfigActivity extends AppCompatActivity {

    private static final int[] PAGE_FLAGS = new int[]{
            PageConfig.FLAG_TIME,
            PageConfig.FLAG_STEP,
            PageConfig.FLAG_DISTANCE,
            PageConfig.FLAG_CALORIES,
            PageConfig.FLAG_SLEEP,
            PageConfig.FLAG_HEART_RATE,
            PageConfig.FLAG_OXYGEN,
            PageConfig.FLAG_BLOOD_PRESSURE,
            PageConfig.FLAG_WEATHER,
            PageConfig.FLAG_FIND_PHONE,
            PageConfig.FLAG_ID,
    };

    private String[] PAGE_NAMES = null;


    private IDevicePerformer mDevicePerformer = WristbandApplication.getDevicePerformer();
    private PageConfig mPageConfig;
    private List<PageConfigItem> mDatas;
    private PageConfigAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        PAGE_NAMES = getResources().getStringArray(R.array.page_names);

        ListView listView = (ListView) findViewById(R.id.list_view);
        mDatas = new ArrayList<>();
        mAdapter = new PageConfigAdapter();
        listView.setAdapter(mAdapter);


        mDevicePerformer.addPerformerListener(mPerformerListener);
        mDevicePerformer.cmd_requestWristbandConfig();
    }


    private SimplePerformerListener mPerformerListener = new SimplePerformerListener() {
        @Override
        public void onResponseWristbandConfig(WristbandConfig config) {
            mDatas.clear();

            WristbandVersion wristbandVersion = config.getWristbandVersion();
            PageConfig pageConfig = config.getPageConfig();

            for (int i = 0; i < PAGE_FLAGS.length; i++) {
                int flag = PAGE_FLAGS[i];
                if (wristbandVersion.isPageSupport(flag)) {
                    PageConfigItem item = new PageConfigItem();
                    item.flag = flag;
                    item.name = PAGE_NAMES[i];
                    item.enable = pageConfig.isFlagEnable(flag);

                    mDatas.add(item);
                }
            }

            mAdapter.notifyDataSetChanged();

            mPageConfig = pageConfig;
        }


        @Override
        public void onCommandSend(boolean success, int commandType) {
            if (commandType == PerformerListener.TYPE_SET_PAGE_CONFIG) {
                if (success) {
                    Toast.makeText(ConfigActivity.this, "cmd_setPageConfig success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ConfigActivity.this, "cmd_setPageConfig failed", Toast.LENGTH_SHORT).show();
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
        if (mDatas.size() < 0) return;
        for (PageConfigItem item : mDatas) {
            mPageConfig.setFlagEnable(item.flag, item.enable);
        }

        if (!mDevicePerformer.cmd_setPageConfig(mPageConfig)) {
            Toast.makeText(this, "cmd_setPageConfig failed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }

    private class PageConfigItem {
        String name;
        int flag;
        boolean enable;
    }

    private class PageConfigAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.page_config_item, viewGroup, false);
            }

            TextView textView = FastViewHolder.get(view, R.id.text_view);
            CheckBox checkBox = FastViewHolder.get(view, R.id.check_box);

            textView.setText(mDatas.get(i).name);

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(mDatas.get(i).enable);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mDatas.get(i).enable = b;
                }
            });
            return view;
        }
    }

}
