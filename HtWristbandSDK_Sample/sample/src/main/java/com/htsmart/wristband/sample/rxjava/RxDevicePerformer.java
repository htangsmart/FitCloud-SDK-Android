package com.htsmart.wristband.sample.rxjava;

import com.htsmart.wristband.bean.EcgBean;
import com.htsmart.wristband.bean.SleepTotalData;
import com.htsmart.wristband.bean.SyncRawData;
import com.htsmart.wristband.bean.TodayTotalData;
import com.htsmart.wristband.bean.WristbandAlarm;
import com.htsmart.wristband.bean.WristbandConfig;
import com.htsmart.wristband.bean.WristbandNotification;
import com.htsmart.wristband.bean.WristbandVersion;
import com.htsmart.wristband.bean.config.BloodPressureConfig;
import com.htsmart.wristband.bean.config.DrinkWaterConfig;
import com.htsmart.wristband.bean.config.FunctionConfig;
import com.htsmart.wristband.bean.config.HealthyConfig;
import com.htsmart.wristband.bean.config.NotificationConfig;
import com.htsmart.wristband.bean.config.PageConfig;
import com.htsmart.wristband.bean.config.SedentaryConfig;
import com.htsmart.wristband.bean.config.TurnWristLightingConfig;
import com.htsmart.wristband.performer.IDevicePerformer;
import com.htsmart.wristband.performer.PerformerListener;
import com.htsmart.wristband.sample.rxjava.exception.PerformerBusyException;
import com.htsmart.wristband.sample.rxjava.exception.PerformerCmdFailedException;
import com.htsmart.wristband.sample.rxjava.exception.PerformerDisconnectException;
import com.htsmart.wristband.sample.rxjava.result.RequestBatteryResult;
import com.htsmart.wristband.sample.rxjava.result.RequestEnterOTAResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.NewThreadScheduler;

/**
 * Created by Kilnn on 2017/9/2.
 */

class RxDevicePerformer implements Action, Consumer<Throwable> {
    private static final String TAG = RxDevicePerformer.class.getSimpleName();
    private static final Object RESULT = new Object();//无意义，仅仅用于RxJava发送事件

    private IDevicePerformer mDevicePerformer;
    private ReentrantLock mReentrantLock = new ReentrantLock(true);
    private Condition mStateCondition = mReentrantLock.newCondition();
    private ObservableEmitter mCurrentEmitter;

    private Scheduler mScheduler = new NewThreadScheduler();

    RxDevicePerformer(IDevicePerformer devicePerformer) {
        mDevicePerformer = devicePerformer;
    }

    void init() {
        mDevicePerformer.addPerformerListener(mPerformerListener);
    }

    void release() {
        mDevicePerformer.removePerformerListener(mPerformerListener);
    }

    private Observable<?> awaitObservable() {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Exception {
                mReentrantLock.lock();
                try {
                    int awaitTime = 0;
                    while (mCurrentEmitter != null && awaitTime < 10) {
                        mStateCondition.await(1, TimeUnit.SECONDS);
                        awaitTime++;
                    }
                    if (mCurrentEmitter != null) {
                        emitter.onError(new PerformerBusyException());
                    } else {
                        emitter.onNext(RESULT);
                        emitter.onComplete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mReentrantLock.unlock();
                }
            }
        });
    }

    @Override
    public void run() throws Exception {
        operationUnLock();
    }

    @Override
    public void accept(@NonNull Throwable throwable) throws Exception {
        operationUnLock();
    }

    private void operationUnLock() {
        mReentrantLock.lock();
        try {
            mCurrentEmitter = null;
            mStateCondition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mReentrantLock.unlock();
        }
    }

    private void operationLock(ObservableEmitter emitter) {
        mReentrantLock.lock();
        try {
            mCurrentEmitter = emitter;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mReentrantLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void operationPost(Object object, boolean complete) {
        mReentrantLock.lock();
        try {
            if (mCurrentEmitter != null) {
                if (object instanceof Throwable) {
                    mCurrentEmitter.onError((Throwable) object);
                } else {
                    mCurrentEmitter.onNext(object);
                    if (complete) {
                        mCurrentEmitter.onComplete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mReentrantLock.unlock();
        }
    }

    private Observable<?> performerCmdAction(final CmdAction action) {
        final Observable<?> observable =
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                        operationLock(e);
                        if (!action.run()) {
                            operationPost(new PerformerDisconnectException(), false);
                        }
                    }
                }).doOnDispose(this)
                        .doOnComplete(this)
                        .doOnError(this);
        return awaitObservable()
                .subscribeOn(mScheduler)
                .flatMap(new Function<Object, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull Object o) throws Exception {
                        return observable;
                    }
                });
    }

    private interface CmdAction {
        boolean run();
    }

    /**
     * 请求进入OTA
     */
    @SuppressWarnings("unchecked")
    public Observable<RequestEnterOTAResult> requestEnterOTA() {
        return (Observable<RequestEnterOTAResult>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestEnterOTA();
            }
        });
    }

    /**
     * 设置闹钟列表
     */
    public Observable<?> setAlarmList(final List<WristbandAlarm> list) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setAlarmList(list);
            }
        });
    }

    /**
     * 请求闹钟列表
     */
    @SuppressWarnings("unchecked")
    public Observable<List<WristbandAlarm>> requestAlarmList() {
        return (Observable<List<WristbandAlarm>>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestAlarmList();
            }
        });
    }

    /**
     * 请求通知配置
     */
    @SuppressWarnings("unchecked")
    public Observable<NotificationConfig> requestNotificationConfig() {
        return (Observable<NotificationConfig>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestNotificationConfig();
            }
        });
    }

    /**
     * 设置通知配置
     */
    public Observable<?> setNotificationConfig(final NotificationConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setNotificationConfig(config);
            }
        });
    }

    /**
     * 设置用户信息
     */
    public Observable<?> setUserInfo(final boolean sex, final Date birthday, final int height, final int weight) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setUserInfo(sex, birthday, height, weight);
            }
        });
    }

    /**
     * 请求版本信息
     */
    @SuppressWarnings("unchecked")
    public Observable<WristbandVersion> requestWristbandVersion() {
        return (Observable<WristbandVersion>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestWristbandVersion();
            }
        });
    }

    /**
     * 设置佩戴方式
     */
    public Observable<?> setWearWay(final boolean wearLeftHand) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setWearWay(wearLeftHand);
            }
        });
    }

    /**
     * 请求所有配置
     */
    @SuppressWarnings("unchecked")
    public Observable<WristbandConfig> requestWristbandConfig() {
        return (Observable<WristbandConfig>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestWristbandConfig();
            }
        });
    }

    /**
     * 请求电量
     */
    @SuppressWarnings("unchecked")
    public Observable<RequestBatteryResult> requestBattery() {
        return (Observable<RequestBatteryResult>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_requestBattery();
            }
        });
    }

    /**
     * 设置PageConfig
     */
    public Observable<?> setPageConfig(final PageConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setPageConfig(config);
            }
        });
    }

    /**
     * 设置FunctionConfig
     */
    public Observable<?> setFunctionConfig(final FunctionConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setFunctionConfig(config);
            }
        });
    }

    /**
     * 设置HealthyConfig
     */
    public Observable<?> setHealthyConfig(final HealthyConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setHealthyConfig(config);
            }
        });
    }

    /**
     * 设置DrinkWaterConfig
     */
    public Observable<?> setDrinkWaterConfig(final DrinkWaterConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setDrinkWaterConfig(config);
            }
        });
    }

    /**
     * 设置SedentaryConfig
     */
    public Observable<?> setSedentaryConfig(final SedentaryConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setSedentaryConfig(config);
            }
        });
    }

    /**
     * 设置BloodPressureConfig
     */
    public Observable<?> setBloodPressureConfig(final BloodPressureConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setBloodPressureConfig(config);
            }
        });
    }

    /**
     * 设置TurnWristLightingConfig
     */
    public Observable<?> setTurnWristLightingConfig(final TurnWristLightingConfig config) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setTurnWristLightingConfig(config);
            }
        });
    }

    /**
     * 查找手环
     */
    public Observable<?> findWristband() {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_findWristband();
            }
        });
    }

    /**
     * 设置天气
     */
    public Observable<?> setWeather(final int temperature, final int weatherCode, final String city) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setWeather(temperature, weatherCode, city);
            }
        });
    }

    /**
     * 设置相机状态
     */
    public Observable<?> setCameraStatus(final boolean enter) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setCameraStatus(enter);
            }
        });
    }

    /**
     * 退出睡眠监测
     */
    public Observable<?> exitSleepMonitor() {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_exitSleepMonitor();
            }
        });
    }

    /**
     * 设置语言
     */
    public Observable<?> setLanguage(final byte languageType) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_setLanguage(languageType);
            }
        });
    }

    /**
     * 发送通知
     */
    public Observable<?> sendWristbandNotification(final WristbandNotification notification) {
        return performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.cmd_sendWristbandNotification(notification);
            }
        });
    }

    /**
     * 解绑用户，返回是否成功
     */
    @SuppressWarnings("unchecked")
    public Observable<Boolean> userUnBind() {
        return (Observable<Boolean>) performerCmdAction(new CmdAction() {
            @Override
            public boolean run() {
                return mDevicePerformer.userUnBind();
            }
        });
    }

//    boolean openHealthyRealTimeData(int var1);
//
//    boolean closeHealthyRealTimeData(int var1);
//
//    boolean openGSensor();
//
//    boolean closeGSensor();
//
//    boolean syncData();

    private PerformerListener mPerformerListener = new PerformerListener() {
        @Override
        public void onCommandSend(boolean success, @CommandType int commandType) {
            if (success) {
                switch (commandType) {
                    case PerformerListener.TYPE_SET_ALARM_LIST:
                    case PerformerListener.TYPE_SET_NOTIFICATION_CONFIG:
                    case PerformerListener.TYPE_SET_USER_INFO:
                    case PerformerListener.TYPE_SET_WEAR_WAY:
                    case PerformerListener.TYPE_SET_PAGE_CONFIG:
                    case PerformerListener.TYPE_SET_FUNCTION_CONFIG:
                    case PerformerListener.TYPE_SET_HEALTHY_CONFIG:
                    case PerformerListener.TYPE_SET_DRINK_WATER_CONFIG:
                    case PerformerListener.TYPE_SET_SEDENTARY_CONFIG:
                    case PerformerListener.TYPE_SET_BLOOD_PRESSURE_CONFIG:
                    case PerformerListener.TYPE_SET_TURN_WRIST_CONFIG:
                    case PerformerListener.TYPE_FIND_WRISTBAND:
                    case PerformerListener.TYPE_SET_WEATHER:
                    case PerformerListener.TYPE_SET_CAMERA_STATUS:
                    case PerformerListener.TYPE_EXIT_SLEEP_MONITOR:
                    case PerformerListener.TYPE_SET_LANGUAGE:
                    case PerformerListener.TYPE_SEND_WRISTBAND_NOTIFICATION:
                        operationPost(RESULT, true);
                        break;
                }
            } else {
                operationPost(new PerformerCmdFailedException(), false);
            }
        }

        @Override
        public void onResponseEnterOTA(boolean enter, int failedReason) {
            operationPost(new RequestEnterOTAResult(enter, failedReason), true);
        }

        @Override
        public void onResponseAlarmList(List<WristbandAlarm> list) {
            operationPost(list, true);
        }

        @Override
        public void onResponseNotificationConfig(NotificationConfig notificationConfig) {
            operationPost(notificationConfig, true);
        }

        @Override
        public void onResponseWristbandVersion(WristbandVersion wristbandVersion) {
            operationPost(wristbandVersion, true);
        }

        @Override
        public void onResponseWristbandConfig(WristbandConfig wristbandConfig) {
            operationPost(wristbandConfig, true);
        }

        @Override
        public void onResponseBattery(int percentage, int charging) {
            operationPost(new RequestBatteryResult(percentage, charging), true);
        }

        @Override
        public void onUserUnBind(boolean success) {
            operationPost(success, true);
        }

        @Override
        public void onFindPhone() {
            //查找手机的主动回调
        }

        @Override
        public void onCameraTakePhoto() {
            //拍照的主动回调
        }

        @Override
        public void onHungUpPhone() {
            //挂断电话的主动回调
        }


        @Override
        public void onOpenHealthyRealTimeData(int i, boolean b) {

        }

        @Override
        public void onCloseHealthyRealTimeData(int i) {

        }

        @Override
        public void onResultHealthyRealTimeData(int i, int i1, int i2, int i3, int i4) {

        }

        @Override
        public void onResultEcgRealTimeData(byte[] bytes) {

        }

        @Override
        public void onOpenGSensor(boolean b) {

        }

        @Override
        public void onCloseGSensor() {

        }

        @Override
        public void onResultGSensor(byte[] bytes) {

        }

        @Override
        public void onSyncDataStart(int i) {

        }

        @Override
        public void onSyncDataEnd(boolean b) {

        }

        @Override
        public void onSyncDataResult(List<SyncRawData> list) {
            //do nothing
        }

        @Override
        public void onSyncDataTodayTotalData(TodayTotalData todayTotalData) {
            //do nothing
        }

        @Override
        public void onSyncDataSleepTotalData(List<SleepTotalData> list) {
            //do nothing
        }

        @Override
        public void onSyncDataEcgResult(List<EcgBean> list) {
            //do nothing
        }
    };
}
