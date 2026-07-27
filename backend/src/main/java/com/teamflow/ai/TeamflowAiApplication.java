package com.teamflow.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the TeamFlow AI backend application.
 * <p>
 * This is the foundation build only — no business features,
 * authentication, or domain logic are implemented yet.
 */
@SpringBootApplication
@EnableJpaAuditing
public class TeamflowAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamflowAiApplication.class, args);
    }

}
