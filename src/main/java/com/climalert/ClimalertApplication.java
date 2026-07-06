package com.climalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de Climalert.
 *
 * @EnableScheduling activa las tareas programadas (@Scheduled):
 * - WeatherFetchService: consulta y guarda el clima cada 5 minutos.
 * - AlertService: analiza el ultimo registro cada 1 minuto.
 */
@SpringBootApplication
@EnableScheduling
public class ClimalertApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClimalertApplication.class, args);
    }
}
