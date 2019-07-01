package com.backservice.yixin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YixinApplication {

    public static void main(String[] args) {
        SpringApplication.run(YixinApplication.class, args);
    }

}
