package com.climalert.service;

import com.climalert.model.WeatherRecord;
import com.climalert.repository.WeatherRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Punto 2 del enunciado: cada 1 minuto analiza la ultima informacion
 * disponible del clima. Si se cumplen las condiciones criticas
 * (temperatura > 35 C y humedad > 60%), genera una alerta por correo.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final WeatherRecordRepository repository;
    private final EmailService emailService;

    @Value("${climalert.alert.temperature-threshold}")
    private double temperatureThreshold;

    @Value("${climalert.alert.humidity-threshold}")
    private double humidityThreshold;

    public AlertService(WeatherRecordRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    /** fixedRate = 60000 ms = 1 minuto. */
    @Scheduled(fixedRate = 60_000)
    public void analyzeLatestWeather() {
        repository.findTopByOrderByFetchedAtDesc().ifPresentOrElse(
                this::evaluate,
                () -> log.info("Aun no hay registros de clima para analizar."));
    }

    private void evaluate(WeatherRecord record) {
        Double temperature = record.getTemperature();
        Integer humidity = record.getHumidity();

        if (temperature == null || humidity == null) {
            log.warn("El registro #{} no tiene datos completos, se omite el analisis.", record.getId());
            return;
        }

        boolean critical = temperature > temperatureThreshold && humidity > humidityThreshold;

        if (!critical) {
            log.info("Registro #{} analizado -> Temp: {} C, Humedad: {}%. Condiciones normales.",
                    record.getId(), temperature, humidity);
            return;
        }

        // Evita reenviar la misma alerta cada minuto para el mismo registro.
        if (record.isAlertSent()) {
            log.info("Condiciones criticas en el registro #{}, pero la alerta ya fue enviada.",
                    record.getId());
            return;
        }

        log.warn("CONDICIONES CRITICAS en registro #{} -> Temp: {} C (umbral {}), Humedad: {}% (umbral {}). Enviando alerta...",
                record.getId(), temperature, temperatureThreshold, humidity, humidityThreshold);

        try {
            emailService.sendAlert(record);
            record.setAlertSent(true);
            repository.save(record);
        } catch (Exception e) {
            // Si el envio falla, NO se marca como enviada: se reintenta en el proximo ciclo.
            log.error("No se pudo enviar el correo de alerta: {}", e.getMessage());
        }
    }
}
