package com.htsmart.wristband.sample.dfu;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Kilnn on 2017/6/8.
 * 被重载的方法是安全可靠的，未被重载的方法请小心使用
 */

public class PreventRestoreDialogFragment extends DialogFragment {

    private boolean mDirty;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Kilnn", "Parent onCreate:" + this);
        mDirty = savedInstanceState != null;
        if (mDirty) {
            FragmentManager manager = getFragmentManager();
            if (manager != null) {
                try {
                    manager.beginTransaction().remove(this).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            onCreate(null, null);
        }
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mDirty) {
            return super.onCreateDialog(savedInstanceState);
        } else {
            return onCreateDialog(savedInstanceState, null);
        }
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mDirty) {
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            return onCreateView(inflater, container, savedInstanceState, null);
        }
    }

    @Override
    public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mDirty) {
            onViewCreated(view, savedInstanceState, null);
        }
    }


    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        if (!mDirty) {
            onDestroyView(null);
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        if (!mDirty) {
            onStart(null);
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (!mDirty) {
            onResume(null);
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
        if (!mDirty) {
            onPause(null);
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        if (!mDirty) {
            onStop(null);
        }
    }

    @Override
    public final void onDestroy() {
        Log.e("Kilnn", "Parent onDestroy:" + this);
        super.onDestroy();
        if (!mDirty) {
            onDestroy(null);
        }
    }

    @Override
    public final void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (!mDirty) {
            onCancel(dialog, null);
        }
    }

    @Override
    public final void onDismiss(DialogInterface dialog) {
        Log.e("Kilnn", "Parent onDismiss:" + this);
        super.onDismiss(dialog);
        if (!mDirty) {
            onDismiss(dialog, null);
        }
    }

    //重载的方法
    public void onCreate(@Nullable Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, Object NULL_FOR_OVERRIDE) {

    }

    public void onDestroyView(Object NULL_FOR_OVERRIDE) {

    }

    public void onStart(Object NULL_FOR_OVERRIDE) {

    }

    public void onResume(Object NULL_FOR_OVERRIDE) {

    }

    public void onPause(Object NULL_FOR_OVERRIDE) {

    }

    public void onStop(Object NULL_FOR_OVERRIDE) {

    }

    public void onDestroy(Object NULL_FOR_OVERRIDE) {

    }

    public void onCancel(DialogInterface dialog, Object NULL_FOR_OVERRIDE) {

    }

    public void onDismiss(DialogInterface dialog, Object NULL_FOR_OVERRIDE) {

    }

}
