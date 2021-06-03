# IIoTDataGate
主要收集整理工业物联网环境中各种硬件的通信协议.
目前还在持续开发过程中,欢迎参与

### 参考产品

1. [plc4x](https://github.com/apache/plc4x)
2. [hslcommunication](http://www.hslcommunication.cn/)
3. [西门子S7协议java实现](https://github.com/s7connector/s7connector)

### TODO 

1. 使用Quartz做定时服务
2. 完成写入功能的设计
3. 整体测试上位链路协议的定时读取和写入
4. 链接超时,失败重试,断开重连

### 下个版本优化项

* 使用注解将某些代码中写死的配置关系解耦出来
  * 每个协议对应的ChannelHandler装配过程

