package dev.jianmu.engine.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.jianmu.engine.api.mapper.EngineLockMapper;
import dev.jianmu.engine.api.pojo.EngineLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 分布式悲观锁
 * */
@Service
public class PessimisticLockService extends ServiceImpl<EngineLockMapper, EngineLock> {

    /**
     * 持续尝试获得锁
     * @throws InterruptedException 最多等待 500ms
     * */
    public EngineLock tryLock(String businessCode) throws InterruptedException {
        EngineLock lock;
        int times = 0;
        while ((lock = lock(businessCode)) == null) {
            Thread.sleep(50);
            ++ times;
            if (times == 10) throw new InterruptedException();
        }
        return lock;
    }

    /**
     * 尝试获得锁
     * @param businessCode 加锁业务代码
     * @return 分布式引擎锁对象 为空则未获取到锁
     * */
    @Transactional
    @Nullable
    public EngineLock lock(String businessCode) {
        final EngineLock locked = super.baseMapper.selectOne(
                new LambdaQueryWrapper<EngineLock>()
                        .eq(EngineLock::getBusinessCode, businessCode)
        );
        if (locked != null)
            return null;
        EngineLock lock = new EngineLock(null, businessCode, new Date());
        super.baseMapper.insert(lock);
        return lock;
    }

    /**
     * 尝试解锁
     * */
    @Transactional
    public boolean unlock(@NotNull EngineLock lock) {
        final int delete = getBaseMapper().delete(
                new LambdaQueryWrapper<EngineLock>()
                        .eq(EngineLock::getId, lock.getId())
                        .eq(EngineLock::getBusinessCode, lock.getBusinessCode())
        );
        return delete != -1;
    }

}
