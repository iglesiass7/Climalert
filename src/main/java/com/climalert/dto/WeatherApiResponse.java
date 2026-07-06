package com.climalert.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapea la respuesta JSON del endpoint /current.json de WeatherAPI.
 * Usamos records de Java 21: son inmutables y Jackson los deserializa directo.
 * Solo declaramos los campos que nos interesan; el resto del JSON se ignora
 * gracias a @JsonIgnoreProperties(ignoreUnknown = true).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(Location location, Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            String name,
            String region,
            String country,
            String localtime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            @JsonProperty("last_updated") String lastUpdated,
            @JsonProperty("temp_c") Double tempC,
            @JsonProperty("feelslike_c") Double feelsLikeC,
            @JsonProperty("humidity") Integer humidity,
            @JsonProperty("wind_kph") Double windKph,
            Condition condition) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Condition(String text) {
    }
}
