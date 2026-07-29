package com.teamflow.ai.serviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * TeamFlow AI - Service Registry (Eureka Server)
 * <p>
 * Simple, single-node Eureka server used for service discovery
 * in the TeamFlow AI microservices setup. No business logic here.
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
