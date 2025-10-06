package com.portafolio.ui.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio dedicado a limpiar las tablas de negocio antes de una carga inicial.
 * Utiliza la gestión de transacciones de Spring para garantizar la atomicidad.
 */
@Service
public class BorrarContenidoTablasService {

    private static final Logger logger = LoggerFactory.getLogger(BorrarContenidoTablasService.class);

    // Spring inyecta el EntityManager, que nos da acceso directo a la base de datos.
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Vacía todas las tablas de negocio dentro de una única transacción segura.
     * La anotación @Transactional reemplaza la necesidad del antiguo 'executeInTransaction'.
     * Si alguna de las operaciones falla, Spring revertirá todos los cambios.
     */
    @Transactional
    public void limpiarDatosDeNegocio() {
        logger.info("Iniciando limpieza de tablas de negocio...");

        try {
            // Desactivamos temporalmente la revisión de llaves foráneas.
            logger.debug("Revisión de llaves foráneas desactivada.");
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0;").executeUpdate();

            // Se vacían TODAS las tablas de negocio.
            logger.debug("Truncando tablas: detalle_costeos, kardex, saldos_kardex, transacciones, saldos_diarios, saldos, carga_transacciones...");
            entityManager.createNativeQuery("TRUNCATE TABLE detalle_costeos").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE kardex").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE saldos_kardex").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE transacciones").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE saldos_diarios").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE saldos").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE carga_transacciones").executeUpdate();

        } finally {
            // Es CRUCIAL volver a activar la revisión de llaves foráneas.
            // El bloque 'finally' garantiza que esta línea se ejecute siempre.
            logger.debug("Revisión de llaves foráneas reactivada.");
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1;").executeUpdate();
        }

        logger.info("Limpieza de tablas completada exitosamente.");
    }
}