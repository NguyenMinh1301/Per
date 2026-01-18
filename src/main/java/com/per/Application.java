package com.per;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {

    public static void main(String[] args) {

        System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");

        SpringApplication.run(Application.class, args);
    }
}
