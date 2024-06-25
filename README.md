# 重要提示
再做了若干实际工业数据采集项目后,决定不再继续更新此项目. 而是转为推动 [DataShark](https://github.com/chzhm159/DataShark) 项目的开发.原因如下:
1. 希望实现从单机到无中心化的数据采集服务系统. 单纯基于Netty,以无法满足.如果自行开发对应功能,工作量太大.
2. Java不支持 无符号数字类型. 需要自行处理.
3. 工业领域 C# 还是具有一定优势的. 不少开源 C# 项目代码可以借鉴.包括很多硬件厂商都是提供的 C# 版SDK.
4. 再就是本项目一些先天设计不足和设计逻辑不清楚也需要大的重构

新项目 [DataShark](https://github.com/chzhm159/DataShark) 基于 Akka.Net. 使用 .Net8 开发(我也想Go,我也想Rust. 不过个人势单力薄).  重新设计采集架构.欢迎提意见.

# IIoTDataGate
主要收集整理工业物联网环境中各种硬件的通信协议.
目前还在持续开发过程中,欢迎参与

另外当前版本的抽象层并未仔细推敲,还属于初期快速迭代阶段,各种想法不断

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

