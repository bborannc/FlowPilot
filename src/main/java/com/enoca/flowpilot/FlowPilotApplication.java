package com.enoca.flowpilot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlowPilotApplication {

    public static void main(String[] args) {
        // .env dosyasını oku ve sistem özelliklerine (System.setProperty) aktar
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Sunucuda .env yoksa (gerçek env var varsa) patlamasın
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(FlowPilotApplication.class, args);
    }
}