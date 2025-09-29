package com.portafolio.model.interfaces;

import com.portafolio.model.utiles.LibraryInitializer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Consumer;

/**
 * Repositorio base con manejo robusto de transacciones y recursos.
 * Versión compatible con el módulo portafolio-costing.
 */
public abstract class AbstractRepository {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);
    
    // Campo protegido para acceso directo cuando sea necesario
    protected EntityManager em;

    /**
     * Ejecuta una operación de solo lectura de forma segura.
     */
    protected <T> T executeReadOnly(Function<EntityManager, T> operation) {
        EntityManager entityManager = null;
        try {
            entityManager = LibraryInitializer.getEntityManager();
            return operation.apply(entityManager);
        } catch (Exception e) {
            logger.error("Error en operación de solo lectura", e);
            throw new RuntimeException("Error en consulta: " + e.getMessage(), e);
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * Ejecuta una operación transaccional de forma segura.
     */
    protected <T> T executeInTransaction(Function<EntityManager, T> operation) {
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        
        try {
            entityManager = LibraryInitializer.getEntityManager();
            tx = entityManager.getTransaction();
            tx.begin();

            T result = operation.apply(entityManager);
            
            tx.commit();
            logger.debug("Transacción completada exitosamente");
            return result;

        } catch (Exception e) {
            logger.error("Error en transacción, realizando rollback", e);
            rollbackTransaction(tx);
            throw new RuntimeException("Error en transacción: " + e.getMessage(), e);
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * Ejecuta una operación transaccional sin valor de retorno.
     */
    protected void executeInTransaction(Consumer<EntityManager> operation) {
        executeInTransaction(em -> {
            operation.accept(em);
            return null;
        });
    }

    /**
     * Ejecuta una operación transaccional que retorna un resultado específico.
     */
    protected <T> T executeInTransactionWithResult(Function<EntityManager, T> operation) {
        EntityManager entityManager = null;
        EntityTransaction tx = null;
        
        try {
            entityManager = LibraryInitializer.getEntityManager();
            tx = entityManager.getTransaction();
            tx.begin();

            T result = operation.apply(entityManager);
            
            tx.commit();
            logger.debug("Transacción con resultado completada exitosamente");
            return result;

        } catch (Exception e) {
            logger.error("Error en transacción con resultado, realizando rollback", e);
            rollbackTransaction(tx);
            
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException("Error en transacción: " + e.getMessage(), e);
            }
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     *  -- Para mantener compatibilidad con código existente ---
     */
    protected <T> T execute(Function<EntityManager, T> operation) {
        return executeReadOnly(operation);
    }

    /**
     * Cierra un EntityManager de forma segura.
     */
    private void closeEntityManager(EntityManager entityManager) {
        if (entityManager != null && entityManager.isOpen()) {
            try {
                entityManager.close();
                logger.trace("EntityManager cerrado exitosamente");
            } catch (Exception e) {
                logger.warn("Error al cerrar EntityManager", e);
            }
        }
    }

    /**
     * Realiza rollback de una transacción de forma segura.
     */
    private void rollbackTransaction(EntityTransaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
                logger.debug("Rollback realizado exitosamente");
            } catch (Exception rollbackException) {
                logger.error("Error durante rollback de transacción", rollbackException);
            }
        }
    }
}