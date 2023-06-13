package com.yg;


import com.yg.init.AviatorSystemInit;
import com.yg.init.fmmanager.AviatorCacheManager;
import com.yg.util.ApplicationUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.SmartLifecycle;

@SpringBootApplication
public class App implements SmartLifecycle  {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    @Override
    public void start() {
        // 初始化公式执行环境 必须前置
        ApplicationUtil.getBean(AviatorSystemInit.class).initAll();

        // 测试编写公式
        AviatorCacheManager.getInstance().getAviatorInstance().execute("IFF(1==1,1,2)");
    }

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
        return false;
    }
}
