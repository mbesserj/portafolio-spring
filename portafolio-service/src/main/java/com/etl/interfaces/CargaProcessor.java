package com.etl.interfaces;

public interface CargaProcessor<T> {
    void procesar(T dto);
}