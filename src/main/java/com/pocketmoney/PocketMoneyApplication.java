package com.pocketmoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PocketMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocketMoneyApplication.class, args);
    }
}
