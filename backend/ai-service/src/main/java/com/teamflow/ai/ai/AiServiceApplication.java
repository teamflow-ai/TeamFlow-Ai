package com.teamflow.ai.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import com.teamflow.ai.ai.repository.EmployeeProfileRepository;

@EnableAsync
@EnableCaching
@EnableScheduling
@EnableFeignClients
@EnableDiscoveryClient
@EnableMongoRepositories(basePackages = "com.teamflow.ai.ai.repository")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }

}
