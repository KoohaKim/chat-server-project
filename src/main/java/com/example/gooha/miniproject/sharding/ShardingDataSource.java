package com.example.gooha.miniproject.sharding;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

public class ShardingDataSource extends AbstractRoutingDataSource {

    public ShardingDataSource(Map<Object, Object> dataSourceMap, MessageShardKeySelector messageShardKeySelector) {
        super.setTargetDataSources(dataSourceMap);
        this.afterPropertiesSet();
    }


    @Override
    protected Object determineCurrentLookupKey() {
        Integer key = ShardContext.getShardKey();
        System.out.println(">>> RoutingDataSource determineCurrentLookupKey");
        System.out.println(">>> shardKey from ShardContext = " + key);

        if (key == null) {
            throw new IllegalStateException("ShardKey not set");
        }

        System.out.println("SHARD_KEY: " + key);
        return key;
    }
}
