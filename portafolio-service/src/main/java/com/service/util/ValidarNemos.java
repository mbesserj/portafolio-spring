package com.service.util;

import org.springframework.stereotype.Component;

@Component
public class ValidarNemos {

    public String normalizarInstrumentoNemo(String nemo, String tipoClase) {
        if (nemo == null || nemo.trim().equals("--")) {
            if ("C".equals(tipoClase) || "S".equals(tipoClase) || "T".equals(tipoClase)) {
                return "Movimiento Caja";
            }
        }
        return nemo;
    }
}