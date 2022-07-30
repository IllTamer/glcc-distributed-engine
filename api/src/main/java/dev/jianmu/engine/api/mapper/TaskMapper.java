package dev.jianmu.engine.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.jianmu.engine.provider.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

//    /**
//     * 更新 Task 记录
//     * <p>
//     * 根据 transactionId 更新 Task
//     * @apiNote consumer 模块中进行
//     * */
//    @Insert("UPDATE jianmu_engine_task SET uuid WHERE")
//    boolean update(Task task);
//
//    /**
//     * 创建 Task 记录
//     * */
//    boolean create(Task task);

    /**
     * 查询最大的 transactionId
     * */
    @Select("SELECT transaction_id FROM jianmu_engine_task ORDER BY transaction_id DESC LIMIT 1")
    Long getMaxTransactionId();

}
