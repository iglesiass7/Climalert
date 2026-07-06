package com.climalert.service;

import com.climalert.model.WeatherRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Punto 3 del enunciado: al generarse una alerta se envia un correo
 * a todos los destinatarios configurados, con el detalle completo del clima.
 *
 * Los destinatarios se leen de application.properties
 * (climalert.mail.recipients, separados por coma).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JavaMailSender mailSender;

    @Value("${climalert.mail.from}")
    private String from;

    @Value("${climalert.mail.recipients}")
    private String[] recipients;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(WeatherRecord record) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipients);
        message.setSubject("ALERTA CLIMATICA - " + record.getLocation());
        message.setText(buildBody(record));

        mailSender.send(message);
        log.info("Alerta enviada por correo a: {}", String.join(", ", recipients));
    }

    /** Arma el cuerpo del correo con el detalle completo del clima (text block de Java 21). */
    private String buildBody(WeatherRecord r) {
        return """
                ALERTA METEOROLOGICA - CLIMALERT
                ================================

                Se detectaron condiciones climaticas criticas en %s.

                DETALLE COMPLETO DEL CLIMA
                --------------------------
                Ubicacion:           %s
                Fecha del dato:      %s
                Temperatura:         %s C
                Sensacion termica:   %s C
                Humedad:             %s %%
                Viento:              %s km/h
                Condicion:           %s

                Consultado por Climalert el: %s

                Este es un mensaje automatico generado por Climalert.
                """.formatted(
                r.getLocation(),
                r.getLocation(),
                r.getLastUpdated(),
                r.getTemperature(),
                r.getFeelsLike(),
                r.getHumidity(),
                r.getWindKph(),
                r.getConditionText(),
                r.getFetchedAt().format(FORMATTER));
    }
}
