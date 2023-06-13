package com.yg.init.dsmanager;

import java.util.HashMap;
import java.util.Map;

public class DataSourcePool {

    private DataSourcePool() {
    }

    /**
     * 全局数据源缓存
     */
    private static Map<Integer, ShardingDataSource> pool = new HashMap<>();

    private static volatile DataSourcePool instance = null;

    public static DataSourcePool getInstance() {
        if (instance == null) {
            synchronized (DataSourcePool.class) {
                if (instance == null) {
                    instance = new DataSourcePool();
                }
            }
        }
        return instance;
    }

    /**
     * 连接池初始化
     * @param pool
     */
    public void initPool(Map<Integer, ShardingDataSource> pool) {
        DataSourcePool.pool.putAll(pool);
    }

    /**
     * 删除指定数据源
     * @param sourceId
     */
    public void delete(Integer sourceId) {
        DataSourcePool.pool.remove(sourceId);
    }

    /**
     * 获取指定数据源
     * @param dataSourceId
     * @return
     */
    public ShardingDataSource getSubPool(Integer dataSourceId) {
        return pool.get(dataSourceId);
    }
}
