package com.example.ruiji;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.example.ruiji.mapper")
public class RuijiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuijiApplication.class, args);
        log.info("启动项目");
    }

}
