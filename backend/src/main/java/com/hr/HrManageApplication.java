package com.hr;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * 人力资源管理服务平台 - 启动类
 *
 * 知识点：
 * 1. @SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
 *    默认扫描「启动类所在包及其子包」，所以启动类必须放在 com.hr 根包下。
 * 2. @MapperScan 告诉 MyBatis 去哪个包找 Mapper 接口，省去每个接口都写 @Mapper。
 * 3. main 方法：SpringApplication.run 启动内嵌 Tomcat + 初始化 Spring 容器。
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.hr.mapper")
public class HrManageApplication {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(HrManageApplication.class, args).getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        log.info("""

                ----------------------------------------------------------
                  人力资源管理服务平台后端启动成功
                  接口地址: http://localhost:{}{}/
                  健康检查: http://localhost:{}{}/api/health
                  当前环境: {}
                ----------------------------------------------------------
                """, port, contextPath, port, contextPath, String.join(",", env.getActiveProfiles()));
    }
}
