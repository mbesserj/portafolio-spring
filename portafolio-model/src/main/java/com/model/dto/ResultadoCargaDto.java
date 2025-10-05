package com.model.dto;

import lombok.Getter;
import java.time.Duration;

@Getter
public class ResultadoCargaDto {

    private final boolean exitoso;
    private final int filasProcesadas;
    private final Duration duracion;
    private final String mensaje;

    private ResultadoCargaDto(boolean exitoso, int filasProcesadas, Duration duracion, String mensaje) {
        this.exitoso = exitoso;
        this.filasProcesadas = filasProcesadas;
        this.duracion = duracion;
        this.mensaje = mensaje;
    }

    public static ResultadoCargaDto exitoso(int filasProcesadas, Duration duracion, String mensaje) {
        return new ResultadoCargaDto(true, filasProcesadas, duracion, mensaje);
    }

    public static ResultadoCargaDto fallido(String mensajeDeError) {
        return new ResultadoCargaDto(false, 0, Duration.ZERO, mensajeDeError);
    }
}