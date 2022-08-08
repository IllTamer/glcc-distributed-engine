# 接口文档

## web-api

基于[[分布式任务引擎架构设计]](../README.md)，本服务设有 `register` 与 `monitor` 层 web-api 接口

### register

register 层的根路径为 `/task/register`，其主要用于服务的提交注册。该层设有两个开放的接口

|  end-point  |  description  |  parameter  |  return  | 
|  :----:  |  :----:  |  :----:  |  :----:  |
| `/obtain`  |  获取最新任务Id  |  -  |  最新任务Id(Long)  |
| `/submit`  |  提交任务 |  任务对象([TaskDTO](../api/src/main/java/dev/jianmu/engine/api/dto/TaskDTO.java))  |  已发布任务对象([TaskPublishVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskPublishVO.java))  |

> 为了确保任务不会被重复提交执行，在调用 `/submit` 接口之前需要先调用 `/obtain` 接口获取获取最新任务Id _(transactionId)_，
> 并用此Id标记即将发布的任务，随后调用 `/submit` 接口对具有唯一标识的任务进行提交发布。

**示例**
```http request
// 获取最新 transactionId
#GET http://localhost/task/register/obtain

// 提交任务
POST http://localhost/task/register/submit
Content-Type: application/json

{
  // task content
}
```

### monitor

monitor 层的根路径为 `/task/monitor`，其主要用于监控与动态操作已提交任务的状态。该层设有三个开放的接口。

|  end-point   |  description  |  parameter  |  return  |
|  :----:  |  :----:  |  :----:  |  :----:  |
|  `/pause`  |  暂停任务  |  任务UUID(String)  |  暂停状态(Boolean)  |
|  `/continue`  |  恢复暂停任务  |  任务UUID(String)  |  已发布任务对象([TaskPublishVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskPublishVO.java))  |
|  `/progress`  |  查询任务调度过程  |  任务UUID(String)  |  任务调度对象([TaskProcessVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskProcessVO.java))  |
