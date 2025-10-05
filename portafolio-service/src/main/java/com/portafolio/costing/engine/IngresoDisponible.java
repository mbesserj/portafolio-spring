package com.portafolio.costing.engine;

import com.portafolio.model.entities.KardexEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa un lote de inventario disponible para costeo FIFO.
 * Cada registro de kardex de ingreso se convierte en un "lote" que se consume de forma FIFO.
 */
@Getter
public class IngresoDisponible {

    private final KardexEntity kardexIngreso;
    private BigDecimal cantidadDisponible;

    /**
     * Constructor para crear un lote de inventario desde un registro de kardex.
     *
     * @param kardexIngreso El registro de kardex del ingreso
     */
    public IngresoDisponible(KardexEntity kardexIngreso) {
        this.kardexIngreso = kardexIngreso;
        this.cantidadDisponible = kardexIngreso.getCantidadDisponible();
    }

    /**
     * Consume una cantidad del lote disponible.
     *
     * @param cantidadAConsumir Cantidad a consumir
     * @return La cantidad efectivamente consumida
     */
    public BigDecimal consumir(BigDecimal cantidadAConsumir) {
        if (cantidadAConsumir.compareTo(cantidadDisponible) <= 0) {
            // Se puede consumir la cantidad completa
            cantidadDisponible = cantidadDisponible.subtract(cantidadAConsumir);
            return cantidadAConsumir;
        } else {
            // Solo se puede consumir lo que queda disponible
            BigDecimal consumido = cantidadDisponible;
            cantidadDisponible = BigDecimal.ZERO;
            return consumido;
        }
    }

    /**
     * Verifica si el lote aún tiene cantidad disponible.
     */
    public boolean tieneDisponible() {
        return cantidadDisponible.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Obtiene el costo total de una cantidad específica de este lote.
     */
    public BigDecimal getCostoTotal(BigDecimal cantidad) {
        BigDecimal costoUnitario = kardexIngreso.getCostoUnitario();
        return costoUnitario.multiply(cantidad).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format(
                "Lote[Kardex=%d, Disponible=%s, CostoUnit=%s, Fecha=%s]",
                kardexIngreso.getId(),
                cantidadDisponible,
                kardexIngreso.getCostoUnitario(),
                kardexIngreso.getFechaTransaccion()
        );
    }
}