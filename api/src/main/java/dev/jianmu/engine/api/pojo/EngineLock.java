package dev.jianmu.engine.api.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 分布式锁对象
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("jianmu_engine_lock")
public class EngineLock {

    /**
     * 分布式锁 id
     * */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 加锁业务代码
     * */
    @TableField("translate_id")
    private Long translateId;

    /**
     * 更新时间
     * <p>
     * 二次校验处理 ABS 问题
     * */
    @TableField("update_time")
    private Date updateTime;

}
