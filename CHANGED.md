# V1.0.3(2016-06-19)
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

