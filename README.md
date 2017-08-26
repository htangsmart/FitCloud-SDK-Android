
# 和唐手环Android集成文档



## 一、使用范围

Android版本要求：4.4以上
手环硬件版本要求：xxx.xxx.xxx
该文档为指导Android开发人员集成和唐手环SDK，主要为一些关键的使用示例，具体的详细API，请参考JavaDoc文档。


## 二、文档介绍
文档第三部分：基础使用。主要介绍如何集成SDK，完成扫描、连接、发送指令和响应应答。内容如下：    

1. 权限设置
2. 初始化
3. 配置
4. 扫描
5. 连接
6. 指令操作


文档第四部分：手环功能和对应API介绍。针对手环的特性，列举了SDK支持的所有功能，并详细的介绍每一个功能如何应当如何操作。内容如下：

1. 闹钟设置
2. 消息通知
3. 设置用户信息
4. 设置佩戴方式
5. 请求电量
6. 查找手环 和 查找手机
7. 设置天气
8. 实时数据
9. 拍照控制
10. 数据同步
11. 解绑用户
12. 手环配置
13. DFU

这两部分都是介绍使用SDK的方法和需要注意的事项，具体详细的API，请参考JavaDoc文档。


## 三、基础使用
### 1.权限设置

```
<!--大部分情况下,你需要保证设备支持BLE-->
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true"/>
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<!--Android6.0及以上，蓝牙扫描需要下面的两个权限,你需要在运行时申请-->
<uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission-sdk-23 android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### 2.初始化

使用SDK的第一步，需要完成初始工作。你可以直接让你自己的`Application`类继承`WristbandApplication`，或者在`onCreate`方法中初始化它。

```
public class YourApplication extends WristbandApplication{
}
```

或者

```
public class YourApplication extends Application{
      public void onCreate() {
          super.onCreate();
          WristbandApplication.init(this);
      }
}
```

### 3.配置

需要配置的内容并不多，而且也不是必要的，你可以在完成初始化后，选择配置部分内容。

```
public class YourApplication extends Application{
      public void onCreate() {
          super.onCreate();
          WristbandApplication.init(this);
	/*配置部分*/
	
	//显示debug日志，默认不显示。
	WristbandApplication.setDebugEnable(true);
	
	//设置每次扫描15秒，默认10S。
	WristbandApplication.getDeviceScanner().setScanPeriods(15*1000);

	/*设置尝试连接间隔时间。当手环被动断开连接的时候，根据此设置的时间间隔
	来决定尝试连接的频率。如果不设置，将使用默认的设置*/
	WristbandApplication.
	getDeviceConnector().
	setTryTimeStrategy(new TryTimeStrategy(){
							//….
					   });
}

```


### 4.扫描

获取 `IDeviceScanner` 对象，进行扫描蓝牙设备。

```
    private IDeviceScanner mDeviceScanner = WristbandApplication.getDeviceScanner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //添加扫描监听器，对应的需要在onDestroy()中移除
        mDeviceScanner.addScannerListener(mScannerListener);
    }
    private ScannerListener mScannerListener = new ScannerListener() {
        @Override
        public void onScan(ScanDeviceWrapper scanDeviceWrapper) {
            //处理扫描的设备
        }
        @Override
        public void onStop() {
            //扫描结束回调
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除扫描监听器
        mDeviceScanner.removeScannerListener(mScannerListener);
}
```


需要注意的是` mDeviceScanner` 扫描并不会区分是不是手环设备，而是针对附近所有的蓝牙设备。
而且开始扫描`start()`和结束扫描`stop()`必须是在主线程调用，另外`mScannerListener`的回调也都是在主线程调用的。

### 5.连接

获取`IDeviceConnector`进行连接手环。

```
public class ConnectActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE = "device";
    private BluetoothDevice mBluetoothDevice;
    private IDeviceConnector mIDeviceConnector = WristbandApplication.getDeviceConnector();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mBluetoothDevice = getIntent().getParcelableExtra(EXTRA_DEVICE);
        mIDeviceConnector.addConnectorListener(mListener);
        User user=new User();//User class which implemented IwristbandUser interface.
        user.setId(1);
        user.setSex(true);
        user.setHeight(170);
        user.setWeight(60);
        user.setBirthday(new Date());
       mIDeviceConnector.connectWithBind(mBluetoothDevice, user);
    }

    private ConnectorListener mListener = new ConnectorListener() {
        @Override
        public void onConnect(WristbandConfig config) {
            //连接成功回调
        }

        @Override
        public void onDisconnect(boolean isCloseActive, boolean reconnect) {
            //断开连接回调
        }

        @Override
        public void onConnectFailed(int failedCode) {
            //连接失败回调
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIDeviceConnector.close();
        mIDeviceConnector.removeConnectorListener(mListener);
    }
}
```


`IDeviceConnector`的连接操作有4个:

```
connecWithBind(BluetoothDevice,IWristbandUser)
connecWithBind(String,IWristbandUser)
connecWithLogin(BluetoothDevice,IWristbandUser)
connecWithLogin(BluetoothDevice,IWristbandUser)
```

可以看出，这里的连接不仅仅是蓝牙设备的连接，在蓝牙连接成功后，还会验证该设备是不是我们的手环设备。如果是，那么就使用传入的 `IWristbandUser`参数，绑定或者登录手环，并完成手环的一些基础设置。
`IWristbandUser`是一个接口，具体方法请参照JavaDoc文档。传入的`IWristbandUser`参数，内部并不会持有它的引用，而是会复制并缓存它的每一个值。当手环被动断开的时，内部进行重连操作，会再次使用这些缓存的值。有多种情况会导致缓存的值发生改变。上面列举的4种连接操作，都会清空缓存值，并复制新的` IWristbandUser`参数值。另外`IDevicePerformer`有下面3个方法会改变这些缓存值：

```
IDevicePerformer#cmd_setWearWay(boolean leftHand);
IDevicePerformer#cmd_setBloodPressureConfig(BloodPressureConfig config);
IDevicePerformer#cmd_setUserInfo(boolean sex, Date birthday, int height, int weight);
```

注意登录或者绑定成功后，`IWristbandUser#wristbandUserId()`值不会改变，除非重新登录或者绑定。

绑定和登录两者有一些差别，对于一个新的用户，第一次连接手环时，你需要选择绑定操作。绑定成功之后，下一次连接手环时，你需要选择登录操作。如果手环当前绑定的用户ID是1000，那么当你尝试使用ID为1001的用户进行登录时，登录会失败。某个用户是否绑定过，SDK内部并没有记录，你需要自己去处理这个逻辑。

如果当前绑定的用户ID为1000，尝试使用用户ID为1001重新绑定，仍然可以绑定成功。每次绑定成功，手环将清除之前的用户数据，包括运动、睡眠、心率等所有数据。

当连接成功后， `ConnectorListener#onConnect(WristbandConfig config)` 方法会回调，并且将已经获取的手环配置信息 `WristbandConfig`一起返回。配置信息如何使用，请参考12、参数配置。


### 6.指令操作

获取`IDevicePerformer`执行指令操作。

```
public class PerformerActivity extends AppCompatActivity {

    private IDevicePerformer mIDevicePerformer = WristbandApplication.getDevicePerformer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performer);
        mIDevicePerformer.addPerformerListener(mListener);
//请求手环版本信息
mIDevicePerformer.cmd_requestWristbandVersion();
    }

    private PerformerListener mListener = new PerformerListener() {
		...
                     /**
		 * 回复手环版本信息
		 */
		public void onResponseWristbandVersion(WristbandVersion version) {
			//处理返回的手环版本信息
		}

		...
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIDevicePerformer.removePerformerListener(mListener);
    }
}
```




## 四、手环功能和对应API介绍

### 1.闹钟设置

手环只支持8个闹钟，每一个闹钟以`WristbandAlarm`中的`alarmId`作为唯一标志，所以`alarmId`的值为0-7。
闹钟的时间信息为 年(year)，月(month)，日(day)，时(hour)，分(minute)。

闹钟的重复周期使用`repeat`来标志。如果`repeat`为0，表示不重复，那么它只会在设置的时刻生效一次。如果`repeat`不为0，那么年、月、日会被忽略，它会在设置的某天的某个时刻多次生效。闹钟是否开启使用`enable`来表示。值得注意的是，如果`repeat`为0，并且设置的时间小于当前时间，那么你应该强制的认为`enable`为false。

通过`IDevicePerformer#cmd_requestAlarmList()`来请求闹钟，对应的返回在`PerformerListener#onResponseAlarmList(List<WristbandAlarm> alarms)`中。

通过`IDevicePerformer#cmd_setAlarmList(List<WristbandAlarm> alarms)`来设置闹钟。需要注意的是，你必须同时设置所有你希望保存的闹钟，所以这里需要传入的是一个List。如果只设置一个闹钟，那么其他的闹钟信息将全部丢失。



### 2.消息通知

使用`IDevicePerformer# sendWristbandNotification(WristbandNotification notification) `可以对手环发送消息通知，该方法没有响应应答，你不需要关系消息通知是否发送成功。

`WristbandNotification`为发送给手环的消息实体。你可以给手环发送一共15种不同的消息通知，如QQ，微信，Facebook等。具体的参考JavaDoc文档。

如果你要发送某一个类型的消息通知，那么首先需要保证手环的消息通知配置`NotificationConfig`中，该类型的通知已经启用。否则手环即使收到了消息通知，也不会震动提示。

`NotificationConfig`中一共有15项配置，但是并不是和`WristbandNotification`中15中消息类型一一对应。`NotificationConfig`中的前13项配置对应`WristbandNotification`中15种消息类型。这是因为电话类型的通知有3种类型：来电，接听和挂断。其他的两项配置如下：

```
/**
 * Flag of phone waring when disconnect。
 */
private static final int FLAG_PHONE_WARING = 14;
/**
 * Flag of device waring when disconnect。
 */
private static final int FLAG_DEVICE_WARING = 15;
```

这两项配置分别为 连接断开时设备提醒 和 连接断开时手机提醒。其中连接断开时手机提醒需要开发者自己实现（如果你需要此项功能的话，如果不需要，可以忽略）。

你可以通过`IDevicePerformer#cmd_requestNotificationConfig()`请求消息通知配置，对应的返回在`PerformerListener#onResponseNotificationConfig(NotificationConfig config)`中。
也可以用过`IDevicePerformer#cmd_requestWristbandConfig()`请求所有配置，对应的返回在`PerformerListener#onResponseWristbandConfig(WristbandConfig config)`中。

如果你想要改变通知配置，使用`IDevicePerformer#cmd_setNotificationConfig(NotificationConfig config)`进行设置。

因为Android本身并不支持ANCS，所以这些通知消息需要自己捕获，具体请参考Sample工程。


### 3.设置用户信息
当绑定或登录手环的时候，已经将用户信息缓存到内部，如果用户信息有更新，那么可以调用`IDevicePerformer#cmd_cmd_setUserInfo(boolean sex, Date birthday, int height, int weight)`来更新用户信息。
请注意这里的生日值不能为Null，否则会崩溃。


### 4.设置佩戴方式

当绑定或登录手环的时候，已经将佩戴方式缓存到内部，如果佩戴方式有更新，那么可以调用`IDevicePerformer#cmd_setWearWay(boolean leftHand)`来更新佩戴方式。


### 5.请求电量

调用`IDevicePerformer#cmd_requestBattery()`来请求电量信息。


### 6.查找手环 和 查找手机

手机主动查找手环，调用`IDevicePerformer#cmd_findWristband()`。
手环主动查找手机，`PerformerListener#onFindPhone()`会主动回调。


### 7.设置天气

调用`IDevicePerformer#cmd_setWeather(int temperature, int weatherCode, String city)`来设置天气。
在使用设置天气之前，需要保证`WristbandVersion#isWeatherEnable()`为true，即手环支持天气功能。

设置天气的方法有3个参数，第一个参数为当前温度，第二个参数为天气代码，第三个参数为城市名称。
手环支持的天气代码如下：

```
晴天：					           0x01;
多云：							   0x02;
阴天：							   0x03；
阵雨：							   0x04；
雷阵雨、雷阵雨伴有冰雹：             0x05；
小雨：							   0x06；
中雨、大雨、暴雨：		           0x07；
雨加雪、冻雨：					    0x08；
小雪：							   0x09；
大雪、暴雪：			       	     0x0a;
沙尘暴、浮尘：					    0x0b;
雾、雾霾：						  0x0c;
```


### 8.实时数据

SDK支持4种实时数据的测试，但是能不能使用，还要取决于手环是否有该项功能模块。使用`WristbandVersion`检测手环中该功能模块是否存在。具体如何检测请参考12、手环配置。

使用`IDevicePerformer#openHealthyRealTimeData(@HealthyType int healthyType)`开启某个实时数据，开启的结果在`PerformerListener#onOpenHealthyRealTimeData(int, boolean)`返回。

使用`IDevicePerformer#closeHealthyRealTimeData(@HealthyType int healthyType)`关闭某个实时数据，关闭的结果在`PerformerListener#onCloseHealthyRealTimeData(int)`返回。

成功开启实时数据后，实时数据检测将持续2分钟的时间，并且每隔5秒左右，会返回一次实时数据测量值，返回结果在`PerformerListener#onResultHealthyRealTimeData(int heartRate, int oxygen, int diastolicPressure, int systolicPressure, int respiratoryRate)`回调。
如果2分钟之内，没有主动调用关闭的方法，那么会自动关闭，并回调`PerformerListener#onCloseHealthyRealTimeData(int)`。

ADD:新增ECG(心电)实时数据，使用类似于其他四种实时数据，但是数据的返回在一个单独的方法`PerformerListener#onResultEcgRealTimeData(byte[])`中。返回的数据为原始数据，每包数据为20个字节。2个字节代表心电，2个字节代表心率，一直交替直到20字节。

### 9.拍照控制

手环实现的拍照功能并不能控制Android系统的相机，你必须自己实现相机拍照功能。
在进入相机界面，调用`mDevicePerformer.cmd_setCameraStatus(true)`通知手环已经准备好拍照控制。此时晃动手环，手环就会主动调用拍照，然后你需要在`PerformerListener#onCameraTakePhoto()`里调用拍照。

在退出相机的时候，务必调用`mDevicePerformer.cmd_setCameraStatus(false)`通知手环已经退出拍照控制。


### 10.数据同步

SDK支持6个功能模块的数据同步，其中步数和睡眠是必定存在的，其他功能模块能不能使用，还要取决于手环是否有该项功能模块。
使用`WristbandVersion`检测手环中该功能模块是否存在。具体如何检测请参考`12、手环配置`。

使用`IDevicePerformer#syncData()`开始数据同步，开始的结果在`PerformerListener#onSyncDataStart(int result)`中返回。
如果开启成功，SDK内部会根据手环的配置，依次去同步手环所支持功能模块的数据。顺序为步数->睡眠->紫外线(如果可用)->血氧(如果可用)->血压(如果可用)->呼吸频率(如果可用)->心率(如果可用)。如果某一个模块存在数据，那么就会回调一次`PerformerListener#onSyncDataResult(List<SyncRawData> datas)`方法。
当所有功能模块同步完成后，SDK还会去获取心电数据(如果心电可用的话)，结果会在`PerformerListener#onSyncDataEcgResult(List<EcgBean>)`中返回。
最后，SDK内部还会去获取当天手环的统计数据和睡眠7天历史数据，结果在`PerformerListener#onSyncDataTodayTotalData(TodayTotalData data)`和`PerformerListener#onSyncDataSleepTotalData(List<SleepTotalData>)`中返回。
当同步完成或者失败，`PerformerListener#onSyncDataEnd(boolean success)`会被回调。可以根据此回调的参数来判断此次同步数据是否成功。
要注意失败的是，即使同步失败，中间的同步数据流程依然可能返回数据，请记得处理这些数据。


### 11.解绑用户

使用`IDevicePerformer#userUnBind()`解绑用户，解绑的结果在`PerformerListener#onUserUnBind(boolean success)`中返回。


### 12.手环配置

在SDK中，`WristbandConfig`作为手环配置信息的实体类，里面包含了手环所有的配置信息和功能参数。
1. WristbandVersion 固件信息
2. NotificationConfig 通知配置
3. BloodPressureConfig 血压配置
4. DrinkWaterConfig 喝水提醒配置，V1.0.4新增了开始时间，结束时间和间隔时间的设置
5. FunctionConfig 辅助功能配置
6. HealthyConfig 健康数据的实时检测配置
7. SedentaryConfig 久坐提醒配置
8. PageConfig 手环页面配置

V1.0.4新增
9. TurnWristLightingConfig 新翻腕亮屏配置，代替旧版本 FunctionConfig 中的翻腕亮屏(FLAG_TURN_WRIST_LIGHTING)。如果固件非最新版本，那么此字段可能为NULL，请使用FunctionConfig配置此功能。

    
在连接手环成功后，`ConnectorListener#onConnect(WristbandConfig)`会返回`WristbandConfig`。
你可以在此时通过`WristbandConfig#getBytes()`获取对应的字节码，缓存到本地。之后可以通过`WristbandConfig#newInstance(byte[])`重新生成实例。
注意：虽然`WristbandConfig`的构造方法是公开的，但是最好不要在外部调用，避免出现不可预料的错误。

另外，可以使用`IDevicePerformer#cmd_requestWristbandConfig()`来获取配置信息，获取的结果在`PerformerListener# onResponseWristbandConfig(WristbandConfig config)`中回调。
也可以使用`IDevicePerformer#cmd_requestWristbandVersion()`和`IDevicePerformer#cmd_requestNotificationConfig()`来请求单独的两项配置信息。当然你也可以不使用这两个方法，直接获取所有配置。

如果想要获取`WristbandConfig`中某一项配置的字节码，同样可以使用该项配置的`getBytes()`方法，如`PageConfig#getBytes()`.


#### 12.1 WristbandVersion

WristbandVersion里的信息主要分为三部分：

1. 硬件、固件、flash等版本信息，用于以后固件升级时的版本判断。

```
private String project;
private String hardware;
private String patch;
private String flash;
private String app;
private String serial;
```

2. 功能模块信息，用于判断手环所支持的功能。

```
private boolean heartRateEnable;
private boolean ultravioletRaysEnable;
private boolean weatherEnable;
private boolean oxygenEnable;
private boolean bloodPressureEnable;
private boolean respiratoryRateEnable;
```


3. 页面支持信息，用于判断手环上可显示的页面，结合PageConfig使用。具体参考PageConfig的用法。

```
private int pageShow;
```


#### 12.2 NotificationConfig
用于配置消息通知。


#### 12.3 BloodPressureConfig
用于配置血压，包括两个值：收缩压和舒张压。请设置正常范围内的血压值。

#### 12.4 DrinkWaterConfig
用于提醒用户按时喝水。
在V1.0.4之后，新增了开始时间，结束时间和间隔时间的设置。这些设置对旧版本的手环无效。手环是否有这部分功能，可以根据`WristbandVersion#isNewTurnWristLightingEnabled()`判断

#### 12.5 FunctionConfig
用于配置手表的辅助功能，目前有四个设置
    
1. 翻腕亮屏(FLAG_TURN_WRIST_LIGHTING)，true为开启，false为关闭
2. 加强测量(FLAG_STRENGTHEN_TEST)，true为开启，false为关闭
3. 十二小时制(FLAG_HOUR_STYLE_12)，true为十二小时制，false为二十四小时制
4. 英制单位(FLAG_IMPERIAL_UNITS)，true为英制单位，false为公制单位

#### 12.6 HealthyConfig
用于配置是否开启健康数据的实时检测，并设置开始和结束的时间。

#### 12.7 SedentaryConfig
用于配置是否在用户久坐的时候提醒用户，并设置开始和结束时间，以及免打扰设置。
免打扰如果开启，那么固定的免打扰时间为12:00-2:00。

#### 12.8 PageConfig
PageConfig用于配置手表上的显示的界面，一共提供了11种配置的界面。但是在设置配置之前，请先检测使用WristbandVersion#isPageSupport(int flag)检测手环是否支持该页面。具体参考Sample工程。

#### 12.9 TurnWristLightingConfig
V1.0.4新增。可以根据`WristbandVersion#isNewTurnWristLightingEnabled()`判断手环是否支持此功能。
如果支持，务必使用该Config设置翻腕亮屏功能，如果不支持，请使用`FuncConfig#FLAG_TURN_WRIST_LIGHTING`设置。

### 13.DFU升级
使用DfuManager可以对手表硬件进行OTA升级。DfuManager所完成的工作如下：

 1. 检查DFU文件是否正确。如果传入文件url，会自动下载，请确保app拥有网络权限
 2. 进入DFU模式。
 3. 搜索DFU设备。
 4. 发送升级数据包。
 5. 升级成功。

具体细节，请参考javaDoc文档和示例。


