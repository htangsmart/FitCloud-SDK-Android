package com.htsmart.wristband.sample;

import com.htsmart.wristband.bean.EcgBean;
import com.htsmart.wristband.bean.SleepTotalData;
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
    public void onRestartWristband(boolean success) {

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
    public void onResultEcgRealTimeData(byte[] data) {

    }

    @Override
    public void onOpenGSensor(boolean success) {

    }

    @Override
    public void onCloseGSensor() {

    }

    @Override
    public void onResultGSensor(byte[] data) {

    }

    @Override
    public void onCameraTakePhoto() {

    }

    @Override
    public void onHungUpPhone() {

    }

    @Override
    public void onSyncDataStart(@SyncResult int result) {

    }

    @Override
    public void onSyncDataEnd(boolean success) {

    }

    @Override
    public void onSyncDataTodayTotalData(TodayTotalData data) {

    }

    @Override
    public void onSyncDataSleepTotalData(List<SleepTotalData> datas) {

    }

    @Override
    public void onSyncDataEcgResult(List<EcgBean> ecgBeanList) {

    }

    @Override
    public void onSyncDataResult(List<SyncRawData> datas) {

    }
}
