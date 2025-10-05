package com.costing.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando se intenta costear un egreso
 * pero no hay suficiente inventario disponible.
 */
public class InsufficientBalanceException extends RuntimeException {

    private final BigDecimal cantidadRequerida;
    private final BigDecimal cantidadDisponible;
    private final String claveGrupo;

    public InsufficientBalanceException(
            String claveGrupo,
            BigDecimal cantidadRequerida,
            BigDecimal cantidadDisponible) {
        
        super(String.format(
                "Saldo insuficiente para el grupo '%s'. Requerido: %s, Disponible: %s",
                claveGrupo,
                cantidadRequerida,
                cantidadDisponible
        ));
        
        this.claveGrupo = claveGrupo;
        this.cantidadRequerida = cantidadRequerida;
        this.cantidadDisponible = cantidadDisponible;
    }

    public BigDecimal getCantidadRequerida() {
        return cantidadRequerida;
    }

    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }

    public String getClaveGrupo() {
        return claveGrupo;
    }
}