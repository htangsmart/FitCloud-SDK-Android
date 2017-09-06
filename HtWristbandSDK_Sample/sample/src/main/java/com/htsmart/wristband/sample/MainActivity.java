package com.htsmart.wristband.sample;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.htsmart.wristband.sample.rxjava.RxConnectActivity;
import com.htsmart.wristband.scanner.IDeviceScanner;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.ArrayList;
import java.util.List;

import cn.imengya.bluetoothle.scanner.ScanDeviceWrapper;
import cn.imengya.bluetoothle.scanner.ScannerListener;

/**
 * MainActivity. Be used for scan bluetooth device
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Scanner
     */
    private IDeviceScanner mDeviceScanner = MyApplication.getDeviceScanner();

    /**
     * Views and Datas
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DeviceListAdapter mAdapter;
    private List<ScanDeviceWrapper> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //Add ScannerListener in onCreate. And you should Remove ScannerListener int onDestroy.
        mDeviceScanner.addScannerListener(mScannerListener);

        AndPermission
                .with(this)
                .requestCode(REQUEST_PERMISSION_CODE)
                .permission(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.PROCESS_OUTGOING_CALLS
                ).rationale(new RationaleListener() {
            @Override
            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                AndPermission.rationaleDialog(MainActivity.this, rationale);
            }
        }).callback(new PermissionListener() {
            @Override
            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                //do nothing
            }

            @Override
            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                    AndPermission.defaultSettingDialog(MainActivity.this);
                }
            }
        }).start();
    }

    private static final int REQUEST_PERMISSION_CODE = 1;

    /**
     * Judge whether the device is  already exists in the list.
     *
     * @param wrapDevice device
     * @return True indicates in the list. False not.
     */
    private boolean existDevice(ScanDeviceWrapper wrapDevice) {
        if (mDevices.size() <= 0) return false;
        for (ScanDeviceWrapper device : mDevices) {
            if (device.getDevice().getAddress().equals(wrapDevice.getDevice().getAddress())) {
                return true;
            }
        }
        return false;
    }

    private ScannerListener mScannerListener = new ScannerListener() {

        /**
         * A device is found
         * @param scanDeviceWrapper The device be found
         */
        @Override
        public void onScan(ScanDeviceWrapper scanDeviceWrapper) {
            if (!existDevice(scanDeviceWrapper)) {
                mDevices.add(scanDeviceWrapper);
                mAdapter.notifyDataSetChanged();
            }
        }

        /**
         * Scan has stopped
         */
        @Override
        public void onStop() {
            stopScanning();
        }
    };

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScanning();
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);

        mDevices = new ArrayList<>(10);
        mAdapter = new DeviceListAdapter();
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mDevices.get(i).getDevice();
                Intent intent;
                if (BuildConfig.RXJAVA) {
                    intent = new Intent(MainActivity.this, RxConnectActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, ConnectActivity.class);
                }
                intent.putExtra(ConnectActivity.EXTRA_DEVICE, device);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Remove ScannerListener
        mDeviceScanner.removeScannerListener(mScannerListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_scan);
        if (mDeviceScanner.isScanning()) {
            menuItem.setTitle(R.string.stop_scan);
        } else {
            menuItem.setTitle(R.string.scan);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_scan) {
            boolean scanning = mDeviceScanner.isScanning();
            if (scanning) {
                stopScanning();
            } else {
                startScanning();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start scan
     */
    private void startScanning() {
        mDevices.clear();
        mAdapter.notifyDataSetChanged();

        mDeviceScanner.start();
        mSwipeRefreshLayout.setRefreshing(true);
        invalidateOptionsMenu();
    }

    /**
     * Stop scan
     */
    private void stopScanning() {
        mDeviceScanner.stop();
        mSwipeRefreshLayout.setRefreshing(false);
        invalidateOptionsMenu();
    }

    private class DeviceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mDevices.get(i).getDevice();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.device_list_item, viewGroup, false);
                holder = new ViewHolder();
                holder.address_tv = (TextView) view.findViewById(R.id.address_tv);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.rssi_tv = (TextView) view.findViewById(R.id.rssi_tv);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            ScanDeviceWrapper device = mDevices.get(i);
            holder.address_tv.setText("MAC : " + device.getDevice().getAddress());
            holder.name_tv.setText("NAME : " + device.getDevice().getName());
            holder.rssi_tv.setText("RSSI : " + String.valueOf(device.getRssi()));
            return view;
        }

        class ViewHolder {
            TextView address_tv;
            TextView name_tv;
            TextView rssi_tv;
        }
    }

}
