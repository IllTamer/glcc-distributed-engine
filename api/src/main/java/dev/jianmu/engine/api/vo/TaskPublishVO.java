package dev.jianmu.engine.api.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 任务发布信息 VO
 * */
@Data
@Builder
public class TaskPublishVO {

    /**
     * 任务的uuid
     * */
    private String uuid;

    /**
     * 注册节点的信息
     *     Key: hostName,
     *     Value: workerId
     * */
    private Map<String, String> workerIdMap;

}
