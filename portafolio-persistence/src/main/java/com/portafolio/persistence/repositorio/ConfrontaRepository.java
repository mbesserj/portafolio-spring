package com.portafolio.persistence.repositorio;

import com.portafolio.model.dto.ConfrontaSaldoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfrontaRepository extends JpaRepository<Object, Long> { 

    /**
     * Obtiene la fecha más reciente de la tabla de saldos.
     * Spring Data JPA ejecutará esta consulta y devolverá el resultado.
     * @return Un Optional<LocalDate> con la fecha más reciente.
     */
    @Query("SELECT MAX(s.fecha) FROM SaldoEntity s")
    Optional<LocalDate> findUltimaFechaDeSaldos();

    /**
     * Ejecuta una consulta nativa compleja directamente contra la vista 'saldos_view'
     * para encontrar discrepancias entre el saldo de costo (Kárdex) y el saldo de mercado.
     * Mapea los resultados directamente al DTO ConfrontaSaldoDto.
     *
     * @return Una lista de DTOs, cada uno representando una diferencia encontrada.
     */
    @Query(name = "ConfrontaSaldo.obtenerDiferencias", nativeQuery = true)
    List<ConfrontaSaldoDto> obtenerDiferenciasDeSaldos();

}