package com.portafolio.masterdata.implement;

import com.portafolio.persistence.repositorio.TransaccionRepository;
import com.portafolio.persistence.repositorio.InstrumentoRepository;
import com.portafolio.persistence.repositorio.CustodioRepository;
import com.portafolio.persistence.repositorio.TipoMovimientoRepository;
import com.portafolio.persistence.repositorio.EmpresaRepository;
import com.portafolio.model.dto.TransaccionDto;
import com.portafolio.mapper.TransaccionMapper;
import com.portafolio.masterdata.interfaces.TransaccionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    // Repositorios necesarios para las operaciones de negocio
    private final EmpresaRepository empresaRepository;
    private final CustodioRepository custodioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;

    @Autowired
    public TransaccionServiceImpl(TransaccionRepository transaccionRepository,
            TransaccionMapper transaccionMapper,
            EmpresaRepository empresaRepository,
            CustodioRepository custodioRepository,
            InstrumentoRepository instrumentoRepository,
            TipoMovimientoRepository tipoMovimientoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
        this.empresaRepository = empresaRepository;
        this.custodioRepository = custodioRepository;
        this.instrumentoRepository = instrumentoRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionDto findByIdWithRelations(Long id) {
        return transaccionRepository.findByIdWithRelations(id)
                .map(transaccionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransaccionDto> findByBusinessKey(String folio, LocalDate fecha, String cuenta) {
        return transaccionRepository.findByFolioAndFechaAndCuenta(folio, fecha, cuenta)
                .map(transaccionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionDto> findAllOrderedByDate() {
        return transaccionRepository.findAllByOrderByFechaAsc().stream()
                .map(transaccionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionDto> findUncostedTransactions() {
        return transaccionRepository.findByCosteadoFalseOrderByFechaAsc().stream()
                .map(transaccionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionDto> findTransactionsForReview() {
        return transaccionRepository.findByParaRevisionTrueOrderByFechaAsc().stream()
                .map(transaccionMapper::toDto)
                .collect(Collectors.toList());
    }
}