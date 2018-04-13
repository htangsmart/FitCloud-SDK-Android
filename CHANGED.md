# V1.0.7(2018-04-13)
## SDK:
1. 解决同步时指令错乱导致同步失败的bug

# V1.0.6(2017-12-15)
## SDK:
1. 语言设置添加日语
2. FunctionConfig添加温度单位的设置
3. 添加重启手环功能
4. SleepTotalData和TodayTotalData中lightSleep字段重命名为shallowSleep，其代表的功能不变。
5. WristbandVersion中部分方法名称优化，功能不变。

## Sample:
1. 添加重启手环的例子
2. 添加对睡眠数据的解析

# V1.0.5(2017-09-26)
## SDK:
1. 发送通知配置的方法由`sendWristbandNotification`改为`cmd_sendWristbandNotification`，并且在`PerformerListener#onCommandSend()`里有发送结果的回调。
2. 如果集成SDK不需要DFU功能，可以不再添加 Lib_Dfu-release.aar

# V1.0.4(2017-08-26)
## SDK:
1. 解决实时数据血压值错误的bug
2. 新增设置手环语言的功能，支持部分语言的设置，IDevicePerformer#cmd_setLanguage(byte)，一般不需要再调用，因为SDK内部会根据手机系统自动设置
3. 喝水提醒和翻腕亮屏功能变更
4. 通知提醒新增对Kakao和Skype的支持
5. 心电数据同步功能. (Beta)


# V1.0.3(2017-06-19)
## SDK:
1. DFU库更新
2. 新增心电图功能

## Sample:
1. DFU实例更新
2. 新增心电图实例

# V1.0.2(2017-05-15)
## SDK:
1. FunctionConfig添加十二小时和英制单位设置

## Sample:
1. 完善Sample的部分功能


# V1.0.1(2016-12-15)
## SDK:
1. 添加DFU功能的支持

## Sample:
1. 完善DFU的示例


# V1.0.0(2016-10-26)
## SDK:
1. IDeviceConnector 添加设置重试策略的方法 : setTryTimeStrategy(TryTimeStrategy)
2. IDeviceConnector 中 connect** 方法不在持有 IWristbandUser 的引用，而是拷贝它的值。
3. WristbandAlarm 实现了 Cloneable 和 Parcelable 接口。并添加了 ALARM_ID_MIN 和 ALARM_ID_MAX 两个静态域
4. WristbandConfig 中实例域 deviceVersion 改为 wristbandVersion,对应的 get 和 set 方法也改变了。
5. SyncRawData 中新增了睡眠状态的三个静态域：SLEEP_STATUS_DEEP，SLEEP_STATUS_SHALLOW，SLEEP_STATUS_SOBER。
6. ShowConfig 重命名为 PageConfig, 一些对应的方法名字和静态域名字也相应改变了。
7. WristbandVersion 中 pageShow 字段重命名为 pageSupport,并添加了相应的判断方法 isPageSupport(@PageConfig.Flag int flag)。

## Sample:
1. 完善闹钟列表示例
2. 完善消息通知示例
3. 完善实时数据示例
4. 完善拍照控制示例
5. 完善手环配置的示例

