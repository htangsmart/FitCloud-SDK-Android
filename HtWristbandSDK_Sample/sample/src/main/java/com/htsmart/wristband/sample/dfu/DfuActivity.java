package com.htsmart.wristband.sample.dfu;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.htsmart.wristband.dfu.DfuCallback;
import com.htsmart.wristband.dfu.DfuManager;
import com.htsmart.wristband.sample.ConnectActivity;
import com.htsmart.wristband.sample.R;

/**
 * Dfu.
 */
public class DfuActivity extends AppCompatActivity {

    private DfuManager mDfuManager;

    private Button mSelectFileBtn;
    private Button mCancelBtn;
    private TextView mDfuStateTv;
    private ProgressBar mOTAProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu);

        mSelectFileBtn = (Button) findViewById(R.id.select_file_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mDfuStateTv = (TextView) findViewById(R.id.dfu_state_tv);
        mOTAProgressBar = (ProgressBar) findViewById(R.id.ota_progress_bar);

        mDfuManager=new DfuManager(this);
        mDfuManager.init();
        mDfuManager.setDfuCallback(new DfuCallback() {
            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case ERROR_FILE:
                        mDfuStateTv.setText("Dfu failed:File error");
                        break;

                    case ERROR_ENTER_OTA_FAILED_COMMAND:
                    case ERROR_ENTER_OTA_FAILED_LOW_BATTERY:
                    case ERROR_ENTER_OTA_FAILED_UNKNOWN:
                        mDfuStateTv.setText("Dfu failed:Enter OTA failed");
                        break;

                    case ERROR_OTA_SERVICE_NOT_READY:
                    case ERROR_OTA_START:
                    case ERROR_OTA_PROGRESS:
                        mDfuStateTv.setText("Dfu failed:OTA process error");
                        break;
                }
                resetUI();
            }

            @Override
            public void onProgressChanged(int progress) {
                if (progress < 0) {
                    switch (progress) {
                        case DfuCallback.PROGRESS_REQUEST_ENTER_OTA:
                            mDfuStateTv.setText("Request Enter OTA");
                            break;
                        case DfuCallback.PROGRESS_SEARCH_OTA_DEVICE:
                            mDfuStateTv.setText("Search OTA Device");
                            break;
                        case DfuCallback.PROGRESS_SEND_OTA_PACKET:
                            mDfuStateTv.setText("Send OTA Packet");
                            mOTAProgressBar.setVisibility(View.VISIBLE);
                            mOTAProgressBar.setIndeterminate(true);
                            break;
                        case DfuCallback.PROGRESS_SUCCESS:
                            mDfuStateTv.setText("Dfu Success");
                            mSelectFileBtn.setVisibility(View.VISIBLE);
                            mCancelBtn.setVisibility(View.GONE);
                            mOTAProgressBar.setVisibility(View.GONE);

                            //Connect device auto.
                            sendBroadcast(new Intent(ConnectActivity.ACTION_CONNECT_DEVICE));
                            break;
                    }
                } else {
                    mOTAProgressBar.setIndeterminate(false);
                    mOTAProgressBar.setProgress(progress);
                }
            }
        });

        mSelectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //去选择文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), SELECT_FILE_REQ);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DfuActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDfuManager.cancelDfu()) {
                    resetUI();
                } else {
                    Toast.makeText(DfuActivity.this, "Can't be canceled.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void resetUI() {
        mSelectFileBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.GONE);
        mOTAProgressBar.setVisibility(View.GONE);
    }

    private static final int SELECT_FILE_REQ = 1;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case SELECT_FILE_REQ: {
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = getPath(this, uri);

                    //Start try DFU.
                    mSelectFileBtn.setVisibility(View.GONE);
                    mCancelBtn.setVisibility(View.VISIBLE);

                    if (!mDfuManager.startDfu(path)) {
                        Toast.makeText(DfuActivity.this, "Previous dfu process is not finished.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }


    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDfuManager.destroy();
    }


}
