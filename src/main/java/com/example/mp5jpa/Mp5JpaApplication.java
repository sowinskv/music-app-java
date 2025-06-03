package com.example.mp5jpa;

import com.example.mp5jpa.GUI.MainApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class Mp5JpaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Mp5JpaApplication.class)
                .headless(false).run(args);

        SwingUtilities.invokeLater(() -> {
            MainApp mainApp = context.getBean(MainApp.class);
            mainApp.createAndShowGUI();
        });

        System.out.println("Application Started. H2 Console at: http://localhost:8080/h2-console");
    }
}