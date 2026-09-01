package com.example.gooha.miniproject.sharding;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ShardingAspect {
    private static final ThreadLocal<Long> threadLocalChatId = new ThreadLocal<>();

    @Around("@annotation(sharding)")
    public Object handleSharding(ProceedingJoinPoint joinPoint, Sharding sharding) throws Throwable {
        Long chatRoomId = extractChatRoomId(joinPoint);
        threadLocalChatId.set(chatRoomId);
        try {
            return joinPoint.proceed();
        } finally {
            threadLocalChatId.remove();
        }
    }

    private Long extractChatRoomId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            return Long.valueOf(String.valueOf(args[0]));
        }
        return 0L;
    }

    public static Long getCurrentThreadChatId() {
        return threadLocalChatId.get();
    }

    public static void setCurrentThreadChatId(Long chatRoomId) {
        threadLocalChatId.set(chatRoomId);
    }
    public static void clearCurrentThreadChatId() {
        threadLocalChatId.remove();
    }


}
