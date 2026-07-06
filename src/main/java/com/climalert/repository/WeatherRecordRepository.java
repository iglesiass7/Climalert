package com.climalert.repository;

import com.climalert.model.WeatherRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos. Spring Data JPA genera la implementacion
 * automaticamente a partir del nombre de los metodos.
 */
public interface WeatherRecordRepository extends JpaRepository<WeatherRecord, Long> {

    /**
     * Devuelve el registro mas reciente, es decir,
     * "la ultima informacion disponible del clima" que pide el enunciado.
     */
    Optional<WeatherRecord> findTopByOrderByFetchedAtDesc();
}
