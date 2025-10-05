package com.costing.engine;

import com.model.entities.KardexEntity;
import com.model.entities.TransaccionEntity;
import com.model.enums.TipoEnumsCosteo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Factory para crear entidades de Kardex con la configuración correcta.
 * Encapsula la lógica de construcción de registros de Kardex.
 */
@Component
public class KardexFactory {

    private static final int ROUNDING_SCALE = 6;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Crea un registro de Kardex para una transacción de INGRESO.
     *
     * @param transaccion La transacción de ingreso
     * @param saldoCantidad Saldo de cantidad después del ingreso
     * @param saldoValor Saldo de valor después del ingreso
     * @param claveAgrupacion Clave del grupo de costeo
     * @return Entidad de Kardex lista para persistir
     */
    public KardexEntity createFromIngreso(
            TransaccionEntity transaccion,
            BigDecimal saldoCantidad,
            BigDecimal saldoValor,
            String claveAgrupacion) {

        BigDecimal cantidad = transaccion.getCantidad();
        BigDecimal costoUnitario = calcularCostoUnitarioIngreso(transaccion);
        BigDecimal costoTotal = cantidad.multiply(costoUnitario)
                .setScale(ROUNDING_SCALE, ROUNDING_MODE);

        return KardexEntity.builder()
                .transaccion(transaccion)
                .claveAgrupacion(claveAgrupacion)
                .fechaTransaccion(transaccion.getFechaTransaccion())
                .folio(transaccion.getFolio())
                .tipoContable(TipoEnumsCosteo.INGRESO)
                .cantidad(cantidad)
                .costoUnitario(costoUnitario)
                .costoTotal(costoTotal)
                .saldoCantidad(saldoCantidad)
                .saldoValor(saldoValor)
                .cantidadDisponible(cantidad) // Todo está disponible para consumo FIFO
                .empresa(transaccion.getEmpresa())
                .cuenta(transaccion.getCuenta())
                .custodio(transaccion.getCustodio())
                .instrumento(transaccion.getInstrumento())
                .build();
    }

    /**
     * Crea un registro de Kardex para una transacción de EGRESO.
     *
     * @param transaccion La transacción de egreso
     * @param cantidadUsada Cantidad usada de un lote específico
     * @param costoParcial Costo de la cantidad usada
     * @param saldoCantidad Saldo de cantidad después del egreso
     * @param saldoValor Saldo de valor después del egreso
     * @param claveAgrupacion Clave del grupo de costeo
     * @return Entidad de Kardex lista para persistir
     */
    public KardexEntity createFromEgreso(
            TransaccionEntity transaccion,
            BigDecimal cantidadUsada,
            BigDecimal costoParcial,
            BigDecimal saldoCantidad,
            BigDecimal saldoValor,
            String claveAgrupacion) {

        BigDecimal costoUnitario = calcularCostoUnitarioEgreso(cantidadUsada, costoParcial);

        return KardexEntity.builder()
                .transaccion(transaccion)
                .claveAgrupacion(claveAgrupacion)
                .fechaTransaccion(transaccion.getFechaTransaccion())
                .folio(transaccion.getFolio())
                .tipoContable(TipoEnumsCosteo.EGRESO)
                .cantidad(cantidadUsada)
                .costoUnitario(costoUnitario)
                .costoTotal(costoParcial)
                .saldoCantidad(saldoCantidad)
                .saldoValor(saldoValor)
                .cantidadDisponible(null) // Los egresos no tienen cantidad disponible
                .empresa(transaccion.getEmpresa())
                .cuenta(transaccion.getCuenta())
                .custodio(transaccion.getCustodio())
                .instrumento(transaccion.getInstrumento())
                .build();
    }

    /**
     * Calcula el costo unitario de un ingreso.
     * Fórmula: (Cantidad × Precio + Comisión + Gastos + IVA) / Cantidad
     */
    private BigDecimal calcularCostoUnitarioIngreso(TransaccionEntity tx) {
        BigDecimal cantidad = tx.getCantidad();
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal precio = tx.getPrecio() != null ? tx.getPrecio() : BigDecimal.ZERO;
        BigDecimal comision = tx.getComision() != null ? tx.getComision() : BigDecimal.ZERO;
        BigDecimal gastos = tx.getGastos() != null ? tx.getGastos() : BigDecimal.ZERO;
        BigDecimal iva = tx.getIva() != null ? tx.getIva() : BigDecimal.ZERO;

        // Costo base
        BigDecimal costoBase = cantidad.multiply(precio);
        
        // Costo total incluyendo cargos adicionales
        BigDecimal costoTotal = costoBase.add(comision).add(gastos).add(iva);
        
        // Costo unitario
        return costoTotal.divide(cantidad, ROUNDING_SCALE, ROUNDING_MODE);
    }

    /**
     * Calcula el costo unitario de un egreso.
     * Fórmula: Costo Parcial / Cantidad Usada
     */
    private BigDecimal calcularCostoUnitarioEgreso(BigDecimal cantidadUsada, BigDecimal costoParcial) {
        if (cantidadUsada == null || cantidadUsada.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        if (costoParcial == null) {
            return BigDecimal.ZERO;
        }

        return costoParcial.divide(cantidadUsada, ROUNDING_SCALE, ROUNDING_MODE);
    }
}