package com.climalert.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA: cada fila de la tabla weather_records es una "foto" del clima
 * tomada por el sistema. Cumple el requisito de "almacenar localmente para
 * registro historico y analisis posterior".
 */
@Entity
@Table(name = "weather_records")
public class WeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Momento en que Climalert consulto la API. */
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    private String location;      // ej: "Buenos Aires, Argentina"
    private String lastUpdated;   // fecha/hora del dato segun WeatherAPI
    private Double temperature;   // temp_c
    private Double feelsLike;     // feelslike_c (sensacion termica)
    private Integer humidity;     // humedad en %
    private Double windKph;       // viento en km/h
    private String conditionText; // ej: "Parcialmente nublado"

    /**
     * Marca si ya se envio la alerta correspondiente a este registro.
     * El analisis corre cada 1 minuto pero los datos se renuevan cada 5:
     * sin esta marca se enviarian ~5 correos identicos por cada registro critico.
     */
    private boolean alertSent = false;

    /** Constructor vacio requerido por JPA. */
    protected WeatherRecord() {
    }

    public WeatherRecord(LocalDateTime fetchedAt, String location, String lastUpdated,
                         Double temperature, Double feelsLike, Integer humidity,
                         Double windKph, String conditionText) {
        this.fetchedAt = fetchedAt;
        this.location = location;
        this.lastUpdated = lastUpdated;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windKph = windKph;
        this.conditionText = conditionText;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public String getLocation() {
        return location;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Double getWindKph() {
        return windKph;
    }

    public String getConditionText() {
        return conditionText;
    }

    public boolean isAlertSent() {
        return alertSent;
    }

    public void setAlertSent(boolean alertSent) {
        this.alertSent = alertSent;
    }
}
