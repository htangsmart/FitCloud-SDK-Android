package com.htsmart.wristband.sample.dfu;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.htsmart.wristband.bean.UpdateVersionInfo;
import com.htsmart.wristband.dfu.DfuManager;
import com.htsmart.wristband.sample.R;

/**
 * Dfu.
 */
public class DfuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu);

        findViewById(R.id.check_remote_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final UpdateVersionInfo info = DfuManager.checkUpdate();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (info == null) {
                                    Toast.makeText(DfuActivity.this, "No Updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    startDfu(info.getHardwareUrl());
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        findViewById(R.id.select_local_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO check permission
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
    }

    private void startDfu(String uri) {
        DfuDialogFragment.newInstance(uri).show(getSupportFragmentManager(), null);
    }

    private static final int SELECT_FILE_REQ = 1;
    private String mSelectFile;

    @Override
    protected void onResume() {
        super.onResume();
        if (mSelectFile != null) {
            startDfu(mSelectFile);
            mSelectFile = null;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case SELECT_FILE_REQ: {
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    mSelectFile = getPath(this, uri);
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

}
