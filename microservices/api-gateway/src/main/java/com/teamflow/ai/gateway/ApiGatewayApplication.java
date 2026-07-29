package com.teamflow.ai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * TeamFlow AI - API Gateway
 * <p>
 * Simple Spring Cloud Gateway that routes incoming requests to the
 * downstream microservices registered in Eureka. No business logic,
 * no authentication - infrastructure only.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
