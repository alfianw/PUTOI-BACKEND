package com.putoi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PutoiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PutoiBackendApplication.class, args);
        System.out.println("Server is running");
    }

}
