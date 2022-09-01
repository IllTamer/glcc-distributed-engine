# 接口文档

## web-api

基于[[分布式任务引擎架构设计]](../README.md)，本服务设有 `register` 与 `monitor` 层 web-api 接口。接口层应用 Restful 风格。

为方便前端调用，接口返回值统一封装为 Ajax 风格的 json 数据，其格式如下：[实体类](../api/src/main/java/dev/jianmu/engine/api/pojo/AjaxResult.java)

|  name  |  value  |  comment  |
|  :----:  |  :----:  |  :----:  |
| status  |  0 / 301 / 500  |  状态码 - 0成功；301警告；500错误 |
| msg  |  "操作成功"  |  返回消息 - 失败时为错误信息  |
| data  |  json  |  返回数据 - 具体看各接口返回值  |

```json
{
  "status": 0,
  "msg": "",
  "data": {}
}
```

### register

register 层的根路径为 `/task/register`，其主要用于服务的提交注册。该层设有两个开放的接口

|  end-point  |  method  |  description  |  parameter  |  return  | 
|  :----:  |  :----:  |  :----:  |  :----:  |  :----:  |
| `/`  |  POST  |  提交任务 |  任务对象([TaskDTO](../api/src/main/java/dev/jianmu/engine/api/dto/TaskDTO.java))  |  已发布任务对象([TaskPublishVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskPublishVO.java))  |
| `/obtain`  |  GET  |  获取最新任务Id  |  -  |  最新任务Id(Long)  |

> 为了确保任务不会被重复提交执行，在调用 `/submit` 接口之前需要先调用 `/obtain` 接口获取获取最新任务Id _(transactionId)_，
> 并用此Id标记即将发布的任务，随后调用 `/submit` 接口对具有唯一标识的任务进行提交发布。

#### 测试用例

- 获取最新 transactionId
    ```http request
    GET http://localhost/task/register/obtain
    ```
    
    ```json
    {
      "status": 0,
      "msg": "success",
      "data": 5
    }
    ```

- 提交任务

    ```http request
    POST http://localhost/task/register
    Content-Type: application/json
    
    {
      "transactionId": 5,
      "type": "dispatch",
      "priority": 100,
      "cron": null,
      "script": ["cd ~", "echo Hello World"]
    }
    ```
    
    ```json
    {
      "status": 0,
      "msg": "success",
      "data": {
        "uuid": "86b84e6c-2c85-499a-9df5-9bf034f3555d",
        "dispatchInfos": [
          {
            "host": "localhost",
            "port": 2333,
            "workerId": "961fbac8-238e-4155-bfe5-42518e1ce8b3",
            "status": "dispatched"
          }
        ]
      }
    }
    ```

### monitor

monitor 层的根路径为 `/task/monitor`，其主要用于监控与动态操作已提交任务的状态。该层设有三个开放的接口。

|  end-point  |  method  |  description  |  parameter  |  return  |
|  :----:  |  :----:  |  :----:  |  :----:  |  :----:  |
|  `/`  |  GET  |  查询任务调度过程  |  任务UUID(String)  |  任务调度对象([TaskProcessVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskProcessVO.java))  |
|  `/pause`  |  PUT  |  暂停任务  |  任务UUID(String)  |  暂停状态(Boolean)  |
|  `/continue`  |  PUT  |  恢复暂停任务  |  任务UUID(String)  |  已发布任务对象([TaskPublishVO](../api/src/main/java/dev/jianmu/engine/api/vo/TaskPublishVO.java))  |

- 查询任务调度过程

    ```http request
    GET http://localhost/task/monitor/86b84e6c-2c85-499a-9df5-9bf034f3555d
    ```
    
    ```json
    {
      "status": 0,
      "msg": "success",
      "data": {
        "uuid": "86b84e6c-2c85-499a-9df5-9bf034f3555d",
        "transactionId": 5,
        "type": "dispatch",
        "workerId": "961fbac8-238e-4155-bfe5-42518e1ce8b3",
        "status": "EXECUTION_SUCCEEDED",
        "startTime": "2022-09-01T20:11:17",
        "endTime": "2022-09-01T20:11:18"
      }
    }
    ```

- 暂停任务

    ```http request
    PUT http://localhost/task/monitor/pause/86b84e6c-2c85-499a-9df5-9bf034f3555d
    ```
    
    ```json
    {
      "status": 0,
      "msg": "success",
      "data": false
    }
    ```

- 恢复暂停任务

    ```http request
    PUT http://localhost/task/monitor/continue/86b84e6c-2c85-499a-9df5-9bf034f3555d
    ```
    
    ```json
    {
      "status": 500,
      "msg": "Wrong task status: EXECUTION_SUCCEEDED",
      "data": null
    }
    ```