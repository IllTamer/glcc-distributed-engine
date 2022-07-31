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

    private String uuid;

    private Map<String, String> workerIdMap;

}
