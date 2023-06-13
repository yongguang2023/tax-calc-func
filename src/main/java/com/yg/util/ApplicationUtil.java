package com.yg.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        ApplicationUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(String name) {
        return (T)applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz){
        if (clazz == null) return null;
        return applicationContext.getBean(clazz);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 公用发布事件方法(不要再用sevice实现ApplicationContextAware来发布事件了！！！)
     * added by zhangchengbin  date[2016-10-28]
     * @param event
     */
    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext == null) return;
        applicationContext.publishEvent(event);
    }
}
