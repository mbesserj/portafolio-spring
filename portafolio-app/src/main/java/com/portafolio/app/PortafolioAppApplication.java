package com.portafolio.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// 1. Anotación principal de Spring Boot
@SpringBootApplication

// 2. Anotaciones CRÍTICAS para un proyecto multi-módulo
@EntityScan("com.portafolio.model.entities")
@EnableJpaRepositories("com.portafolio.model.repositories")
public class PortafolioAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortafolioAppApplication.class, args);
    }
}