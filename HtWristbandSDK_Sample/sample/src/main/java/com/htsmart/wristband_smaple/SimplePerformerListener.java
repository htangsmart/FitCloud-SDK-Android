package com.htsmart.wristband_smaple;

import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.bean.TodayTotalData;
import com.htsmart.wristband.bean.WristbandAlarm;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.bean.WristbandVersion;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.performer.PerformerListener;

import java.util.List;

/**
 * Created by Kilnn on 16-10-26.
 */
public class SimplePerformerListener implements PerformerListener {
    @Override
    public void onCommandSend(boolean success, int commandType) {

    }

    @Override
    public void onResponseEnterOTA(boolean enter, int failedReason) {

    }

    @Override
    public void onResponseAlarmList(List<WristbandAlarm> alarms) {

    }

    @Override
    public void onResponseNotificationConfig(NotificationConfig config) {

    }

    @Override
    public void onResponseWristbandVersion(WristbandVersion version) {

    }

    @Override
    public void onResponseWristbandConfig(WristbandConfig config) {

    }

    @Override
    public void onResponseBattery(int percentage, int charging) {

    }

    @Override
    public void onFindPhone() {

    }

    @Override
    public void onUserUnBind(boolean success) {

    }

    @Override
    public void onOpenHealthyRealTimeData(int healthyType, boolean success) {

    }

    @Override
    public void onCloseHealthyRealTimeData(int healthyType) {

    }

    @Override
    public void onResultHealthyRealTimeData(int heartRate, int oxygen, int diastolicPressure, int systolicPressure, int respiratoryRate) {

    }

    @Override
    public void onCameraTakePhoto() {

    }

    @Override
    public void onSyncDataStart(boolean success) {

    }

    @Override
    public void onSyncDataEnd(boolean success) {

    }

    @Override
    public void onSyncDataTodayTotalData(TodayTotalData data) {

    }

    @Override
    public void onSyncDataResult(List<SyncRawData> datas) {

    }
}
