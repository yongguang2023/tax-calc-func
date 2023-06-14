package com.yg;


import com.yg.init.AviatorSystemInit;
import com.yg.init.fmmanager.AviatorCacheManager;
import com.yg.util.ApplicationUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.SmartLifecycle;

import java.io.File;
import java.net.URL;
import java.util.*;

@SpringBootApplication
public class App implements SmartLifecycle  {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    @Override
    public void start() {
        // 初始化公式执行环境 必须前置
        ApplicationUtil.getBean(AviatorSystemInit.class).initAll();

        // 测试公式
        Map<String, Object> params = new HashMap<>();
        params.put("$ZTDM$", 1); // 帐套
        AviatorCacheManager.getInstance().getAviatorInstance().execute("IF(1==1,1,2)", params);
    }

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
        return false;
    }
}
