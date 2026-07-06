package com.climalert.client;

import com.climalert.dto.WeatherApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Punto 1 del enunciado: integracion via REST con WeatherAPI.
 *
 * Llama a GET https://api.weatherapi.com/v1/current.json?key=...&q=...&lang=es
 * y convierte el JSON de respuesta en un WeatherApiResponse.
 */
@Component
public class WeatherApiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String location;

    public WeatherApiClient(@Value("${climalert.weather-api.base-url}") String baseUrl,
                            @Value("${climalert.weather-api.key}") String apiKey,
                            @Value("${climalert.weather-api.location}") String location) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.location = location;
    }

    public WeatherApiResponse getCurrentWeather() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", location)
                        .queryParam("lang", "es")
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);
    }
}
