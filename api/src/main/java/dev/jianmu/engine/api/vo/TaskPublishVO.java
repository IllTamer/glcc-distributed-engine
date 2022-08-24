package dev.jianmu.engine.api.vo;

import dev.jianmu.engine.register.DispatchInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
     * 调度信息列表
     * */
    private List<DispatchInfo> dispatchInfos;

}
