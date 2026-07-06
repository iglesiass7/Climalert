package com.climalert.service;

import com.climalert.client.WeatherApiClient;
import com.climalert.dto.WeatherApiResponse;
import com.climalert.model.WeatherRecord;
import com.climalert.repository.WeatherRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Punto 1 del enunciado: cada 5 minutos obtiene los datos climaticos actuales
 * desde WeatherAPI y los guarda en la base local (H2) como registro historico.
 */
@Service
public class WeatherFetchService {

    private static final Logger log = LoggerFactory.getLogger(WeatherFetchService.class);

    private final WeatherApiClient weatherApiClient;
    private final WeatherRecordRepository repository;

    public WeatherFetchService(WeatherApiClient weatherApiClient,
                               WeatherRecordRepository repository) {
        this.weatherApiClient = weatherApiClient;
        this.repository = repository;
    }

    /**
     * fixedRate = 300000 ms = 5 minutos.
     * La primera ejecucion ocurre apenas arranca la aplicacion.
     * Si la API falla (sin internet, key invalida, etc.) se loguea el error
     * y el servicio sigue vivo para reintentar en el proximo ciclo.
     */
    @Scheduled(fixedRate = 300_000)
    public void fetchAndStoreCurrentWeather() {
        try {
            WeatherApiResponse response = weatherApiClient.getCurrentWeather();

            String location = response.location().name() + ", " + response.location().country();
            WeatherApiResponse.Current current = response.current();

            WeatherRecord record = new WeatherRecord(
                    LocalDateTime.now(),
                    location,
                    current.lastUpdated(),
                    current.tempC(),
                    current.feelsLikeC(),
                    current.humidity(),
                    current.windKph(),
                    current.condition() != null ? current.condition().text() : null
            );

            WeatherRecord saved = repository.save(record);
            log.info("Registro #{} guardado -> {} | Temp: {} C | Humedad: {}% | {}",
                    saved.getId(), location, current.tempC(), current.humidity(),
                    saved.getConditionText());

        } catch (Exception e) {
            log.error("Error al consultar/guardar el clima: {}", e.getMessage());
        }
    }
}
