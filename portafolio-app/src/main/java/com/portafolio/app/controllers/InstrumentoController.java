package com.portafolio.app.controllers;

import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.app.services.InstrumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    @Autowired
    private InstrumentoService instrumentoService;

    @GetMapping
    public ResponseEntity<List<InstrumentoDto>> getAllInstrumentos() {
        return ResponseEntity.ok(instrumentoService.findAll());
    }

    @PostMapping
    public ResponseEntity<InstrumentoDto> createInstrumento(@RequestBody InstrumentoDto dto) {
        return ResponseEntity.ok(instrumentoService.save(dto));
    }
}