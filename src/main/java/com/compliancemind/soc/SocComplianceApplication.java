package com.compliancemind.soc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SOC 合规自动化后端入口：启用组件扫描并在 {@code com.compliancemind.soc} 包下扫描 MyBatis Mapper。
 */
@SpringBootApplication
@MapperScan("com.compliancemind.soc")
public class SocComplianceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocComplianceApplication.class, args);
    }
}
