package com.berlin.aetherflow.system.monitor.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动完成后输出品牌化成功横幅。
 */
@Slf4j
@Component
public class AetherFlowStartupBanner {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 在应用就绪后输出启动成功信息与本地访问地址。
     */
    @Order()
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("""

                 /\\
                /  \\      A E T H E R F L O W
               / /\\ \\     warehouse and asset flow platform
              / ____ \\
             /_/    \\_\\

             application : {}
             local       : http://127.0.0.1:{}
             swagger     : http://127.0.0.1:{}/swagger-ui.html

             status      : ready
               \s""",
                applicationName,
                serverPort,
                serverPort
        );
    }
}
