package com.example.gooha.miniproject.sharding;

public final class ShardContext {
    private static final ThreadLocal<Integer> SHARD_KEY = new ThreadLocal<>();

    public static void setShardKey(Integer shardKey) {
        SHARD_KEY.set(shardKey);
    }

    public static Integer getShardKey() {
        return SHARD_KEY.get();
    }

    public static void clear() {
        SHARD_KEY.remove();
    }

}
