package com.yg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器(如: 异步发送mq消息)
 */
@Configuration
public class TaskExecutorAutoConfiguration {

    @Value("${fetch.thread.corePoolSize:10}")
    private Integer fetchThreadCorePoolSize;

    @Value("${fetch.thread.maxPoolSize:20}")
    private Integer fetchThreadMaxPoolSize;

    @Value("${fetch.thread.queueCapacity:9999}")
    private Integer fetchThreadQueueCapacity;

    @Bean(name = "formulaExecuteTask")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(fetchThreadCorePoolSize);
        taskExecutor.setMaxPoolSize(fetchThreadMaxPoolSize);
        taskExecutor.setQueueCapacity(fetchThreadQueueCapacity);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setThreadGroupName("fetch_data_group");
        taskExecutor.setThreadNamePrefix("fetch_data_thread_");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}
