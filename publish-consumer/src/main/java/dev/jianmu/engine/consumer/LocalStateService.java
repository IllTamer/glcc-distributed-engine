package dev.jianmu.engine.consumer;

public interface LocalStateService {

    /**
     * @return CPU使用率，内存使用率
     * */
    LocalState info();

}
