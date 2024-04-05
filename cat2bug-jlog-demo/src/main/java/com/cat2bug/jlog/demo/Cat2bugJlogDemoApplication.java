package com.cat2bug.jlog.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Cat2bugJlogDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cat2bugJlogDemoApplication.class, args);
    }

}
