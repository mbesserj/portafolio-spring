package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

/**
 * DTO que representa el resultado de un proceso de costeo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoCosteoDto {

    private boolean exitoso;
    private int gruposProcesados;
    private int errores;
    private Duration duracion;
    private String mensaje;

    /**
     * Crea un resultado exitoso.
     */
    public static ResultadoCosteoDto exitoso(int gruposProcesados, Duration duracion, String mensaje) {
        return ResultadoCosteoDto.builder()
                .exitoso(true)
                .gruposProcesados(gruposProcesados)
                .errores(0)
                .duracion(duracion)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Crea un resultado fallido.
     */
    public static ResultadoCosteoDto fallido(String mensajeError) {
        return ResultadoCosteoDto.builder()
                .exitoso(false)
                .gruposProcesados(0)
                .errores(1)
                .duracion(Duration.ZERO)
                .mensaje(mensajeError)
                .build();
    }
}