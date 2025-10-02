package com.service.interfaces;

public interface CargaProcesoService {

    /**
     * Orquesta el proceso de leer la tabla de staging y crear las transacciones finales.
     * @param esCargaInicial
     */
    void procesar(boolean esCargaInicial);
    
}