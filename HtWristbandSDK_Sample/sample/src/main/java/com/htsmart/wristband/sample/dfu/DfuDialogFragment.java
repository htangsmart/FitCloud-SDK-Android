package com.htsmart.wristband.sample.dfu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.htsmart.wristband.dfu.DfuCallback;
import com.htsmart.wristband.dfu.DfuManager;
import com.htsmart.wristband.sample.R;


/**
 * Created by Kilnn on 2017/6/7.
 */
public class DfuDialogFragment extends PreventRestoreDialogFragment {

    public interface DfuSuccessListener {
        void onDfuSuccess();
    }

    private static final String EXTRA_URI = "uri";

    private DfuManager mDfuManager;

    private ProgressBar mProgressBar;
    private TextView mPercentageTv;
    private TextView mStateTv;

    private String mUri;

    /**
     * @param uri dfu file uri
     * @return DfuDialogFragment instance
     */
    public static DfuDialogFragment newInstance(String uri) {
        DfuDialogFragment dialog = new DfuDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URI, uri);
        dialog.setArguments(bundle);
        return dialog;
    }

    public DfuDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {
        super.onCreate(savedInstanceState, NULL_FOR_OVERRIDE);
        mDfuManager = new DfuManager(getActivity());
        mDfuManager.setDfuCallback(mDfuCallback);
        mDfuManager.init();

        mUri = getArguments().getString(EXTRA_URI);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_dfu, null);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mProgressBar.setIndeterminate(true);
        mPercentageTv = (TextView) view.findViewById(R.id.percentage_tv);
        mStateTv = (TextView) view.findViewById(R.id.state_tv);
        builder.setView(view);
        setCancelable(false);
        builder.setCancelable(false);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {
        mDfuManager.start(mUri);
        return super.onCreateView(inflater, container, savedInstanceState, NULL_FOR_OVERRIDE);
    }

    @Override
    public void onCancel(DialogInterface dialog, Object NULL_FOR_OVERRIDE) {
        super.onCancel(dialog, NULL_FOR_OVERRIDE);
        mDfuManager.cancel();
    }

    @Override
    public void onDismiss(DialogInterface dialog, Object NULL_FOR_OVERRIDE) {
        super.onDismiss(dialog, NULL_FOR_OVERRIDE);
    }

    @Override
    public void onDestroy(Object NULL_FOR_OVERRIDE) {
        super.onDestroy(NULL_FOR_OVERRIDE);
        mDfuManager.release();
    }

    private DfuCallback mDfuCallback = new DfuCallback() {
        @Override
        public void onError(int errorCode) {
            toastError(errorCode);
            dismissAllowingStateLoss();
        }

        @Override
        public void onStateChanged(int state, boolean cancelable) {
            switch (state) {
                case DfuManager.STATE_CHECK_DFU_FILE:
                    mStateTv.setText(R.string.where_dfu_state_dfu_file);
                    break;
                case DfuManager.STATE_CHECK_DFU_MODE:
                    mStateTv.setText(R.string.where_dfu_state_dfu_mode);
                    break;
                case DfuManager.STATE_FIND_DFU_DEVICE:
                    mStateTv.setText(R.string.where_dfu_state_dfu_device);
                    break;
                case DfuManager.STATE_DFU_ING:
                    mStateTv.setText(R.string.where_dfu_state_dfu_process);
                    break;
            }
            setCancelable(cancelable);
        }

        @Override
        public void onProgressChanged(int progress) {
            if (progress == 0) {
                mStateTv.setText(R.string.where_dfu_progress_start);
            } else if (progress == 100) {
                mStateTv.setText(R.string.where_dfu_progress_completed);
            }
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(progress);
            mPercentageTv.setText(getString(R.string.percentage, String.valueOf(progress)));
        }

        @Override
        public void onSuccess() {
            Toast.makeText(getContext(), R.string.where_dfu_success, Toast.LENGTH_SHORT).show();

            if (getActivity() != null && (getActivity() instanceof DfuSuccessListener)) {
                ((DfuSuccessListener) getActivity()).onDfuSuccess();
            }

            dismissAllowingStateLoss();
        }
    };

    private void toastError(int errorCode) {
        int toastId = 0;
        if (DfuManager.isErrorNormal(errorCode)) {
            if (errorCode == DfuManager.ERROR_BT_UNSUPPORT) {
                toastId = R.string.ble_not_support;
            } else if (errorCode == DfuManager.ERROR_BT_DISABLE) {
                toastId = R.string.bt_not_open;
            }
        } else if (DfuManager.isErrorDfuFile(errorCode)) {
            if (errorCode == DfuManager.ERROR_DFU_FILE_URI
                    || errorCode == DfuManager.ERROR_DFU_FILE_FORMAT) {
                toastId = R.string.where_dfu_error_file_format;
            } else if (errorCode == DfuManager.ERROR_DFU_FILE_NOT_EXIST) {
                toastId = R.string.where_dfu_error_file_not_exist;
            } else if (errorCode == DfuManager.ERROR_DFU_FILE_DOWNLOAD) {
                toastId = R.string.where_dfu_error_file_download;
            }
        } else if (DfuManager.isErrorDfuMode(errorCode)) {
            if (errorCode == DfuManager.ERROR_DFU_MODE_HARDWARE_INFO) {
                //do nothing
            } else if (errorCode == DfuManager.ERROR_DFU_MODE_LOW_BATTERY) {
                toastId = R.string.where_dfu_error_low_battery;
            } else if (errorCode == DfuManager.ERROR_DFU_MODE_ABORT) {
                toastId = R.string.device_disconnected;
            } else {
                toastId = R.string.where_dfu_error_enter_dfu_failed;
            }
        } else if (DfuManager.isErrorDfuDevice(errorCode)) {
            if (errorCode == DfuManager.ERROR_DFU_DEVICE_SCAN_FAILED) {
                toastId = R.string.where_dfu_error_scan_failed;
            } else if (errorCode == DfuManager.ERROR_DFU_DEVICE_NOT_FOUND) {
                toastId = R.string.where_dfu_error_device_not_found;
            }
        } else if (DfuManager.isErrorDfuProcess(errorCode)) {
            if (errorCode == DfuManager.ERROR_DFU_PROCESS_SERVICE_NOT_READY
                    || errorCode == DfuManager.ERROR_DFU_PROCESS_STARTUP_FAILED) {
                toastId = R.string.where_dfu_error_service_not_ready;
            }
        }

        if (toastId != 0) {
            Toast.makeText(getContext(), toastId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.where_dfu_failed) + "  errorCode:" + errorCode, Toast.LENGTH_SHORT).show();
        }
    }

}
