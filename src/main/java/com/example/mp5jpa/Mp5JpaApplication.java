package com.example.mp5jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Mp5JpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mp5JpaApplication.class, args);
        System.out.println("Application Started. H2 Console at: http://localhost:8080/h2-console");
    }

}