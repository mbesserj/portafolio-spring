package com.portafolio.app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 1. Anotación principal de Spring Boot
@SpringBootApplication(scanBasePackages = "com.portafolio")
public class PortafolioAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortafolioAppApplication.class, args);
    }
}