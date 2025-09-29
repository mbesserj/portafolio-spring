package com.portafolio.app.controllers;

import com.portafolio.app.services.CargaTransaccionService;
import com.portafolio.model.dto.CargaTransaccionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cargaTransaccion")
public class CargaTransaccionController {

    @Autowired
    private CargaTransaccionService cargaTransaccionService;

    @GetMapping
    public ResponseEntity<List<CargaTransaccionDto>> getAllCargaTransaccion() {
        return ResponseEntity.ok(cargaTransaccionService.findAll());
    }

    @PostMapping
    public ResponseEntity<CargaTransaccionDto> createEmpresa(@RequestBody CargaTransaccionDto cargaTransaccionDto) {
        return ResponseEntity.ok(cargaTransaccionService.save(cargaTransaccionDto));
    }
}