package com.example.employee.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EmployeeRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeeRedisApplication.class, args);
    }
}