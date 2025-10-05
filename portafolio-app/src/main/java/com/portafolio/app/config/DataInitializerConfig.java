package com.portafolio.app.config;

import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.MovimientoContableEntity;
import com.portafolio.model.entities.TipoMovimientoEntity;
import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.persistence.repositorio.CustodioRepository;
import com.portafolio.persistence.repositorio.EmpresaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuración de inicialización de datos base para la aplicación.
 * Esta clase reemplaza a DataInitializer.java del sistema antiguo.
 * 
 * Se ejecuta automáticamente al iniciar Spring Boot usando CommandLineRunner.
 */
@Configuration
public class DataInitializerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializerConfig.class);
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Bean que se ejecuta al inicio de la aplicación.
     * CommandLineRunner es la forma "Spring Boot" de ejecutar código al arranque.
     * @param empresaRepository
     * @param custodioRepository
     * @return crea la base de datos en el caso que no exista.
     */
    @Bean
    @Transactional
    public CommandLineRunner initDatabase(
            EmpresaRepository empresaRepository,
            CustodioRepository custodioRepository) {
        
        return args -> {
            logger.info("=== Iniciando verificación de datos base ===");
            
            try {
                // 1. Verificar si ya existen datos
                if (datosYaExisten()) {
                    logger.info("Los datos base ya existen. No se requiere inicialización.");
                    return;
                }
                
                // 2. Verificar y crear vistas de base de datos
                verificarYCrearVistas();
                
                // 3. Crear datos maestros
                logger.info("Creando datos base iniciales...");
                crearDatosMaestros();
                
                logger.info("✓ Datos base creados exitosamente!");
                
            } catch (Exception e) {
                logger.error("✗ Error durante la inicialización de datos", e);
                throw new RuntimeException("No se pudo inicializar los datos base", e);
            }
        };
    }
    
    /**
     * Verifica si los datos maestros ya existen en la base de datos.
     * En Spring, NO manejamos transacciones manualmente.
     */
    private boolean datosYaExisten() {
        try {
            entityManager.createQuery(
                "SELECT tm FROM TipoMovimientoEntity tm WHERE tm.tipoMovimiento = :nombre", 
                TipoMovimientoEntity.class)
                .setParameter("nombre", "COMPRA")
                .getSingleResult();
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }
    
    /**
     * Verifica y crea las vistas necesarias en la base de datos.
     */
    private void verificarYCrearVistas() {
        if (!verificarVistaExiste("kardex_view")) {
            logger.info("La vista 'kardex_view' no existe. Creándola...");
            crearVistaKardex();
        } else {
            logger.info("✓ La vista 'kardex_view' ya existe");
        }
        
        if (!verificarVistaExiste("saldos_view")) {
            logger.info("La vista 'saldos_view' no existe. Creándola...");
            crearVistaSaldos();
        } else {
            logger.info("✓ La vista 'saldos_view' ya existe");
        }
    }
    
    /**
     * Crea los datos maestros (Movimientos Contables y Tipos de Movimiento).
     * Con @Transactional en el CommandLineRunner, NO necesitamos begin/commit manual.
     */
    private void crearDatosMaestros() {
        // Crear Movimientos Contables
        MovimientoContableEntity ingreso = crearMovimientoContable(
            TipoEnumsCosteo.INGRESO, 
            "Movimiento de Ingreso General"
        );
        MovimientoContableEntity egreso = crearMovimientoContable(
            TipoEnumsCosteo.EGRESO, 
            "Movimiento de Egreso General"
        );
        MovimientoContableEntity cargo = crearMovimientoContable(
            TipoEnumsCosteo.CARGO, 
            "Movimiento de Cargo de caja"
        );
        MovimientoContableEntity dividendo = crearMovimientoContable(
            TipoEnumsCosteo.DIVIDENDO, 
            "Movimiento de dividendo"
        );
        MovimientoContableEntity retorno = crearMovimientoContable(
            TipoEnumsCosteo.RETORNO, 
            "Movimiento de Devolución capital"
        );
        crearMovimientoContable(
            TipoEnumsCosteo.NO_COSTEAR, 
            "Movimiento que no afecta costeo"
        );

        // Crear Tipos de Movimiento
        crearTipoMovimiento("COMPRA", "Compra de activos", ingreso);
        crearTipoMovimiento("VENTA", "Venta de activos", egreso);
        crearTipoMovimiento("AJUSTE INGRESO", "Ajuste manual por faltante de inventario", ingreso);
        crearTipoMovimiento("AJUSTE EGRESO", "Ajuste manual por sobrante de inventario", egreso);
        crearTipoMovimiento("AJUSTE_AUTO_TOLERANCIA", "Ajuste automático por diferencia dentro de la tolerancia", ingreso);
        crearTipoMovimiento("SALDO INICIAL", "Saldo inicial", ingreso);
        
        logger.info("✓ Movimientos contables y tipos de movimiento creados");
    }
    
    // ========================================================================
    // MÉTODOS DE CREACIÓN DE ENTIDADES
    // ========================================================================
    
    private MovimientoContableEntity crearMovimientoContable(TipoEnumsCosteo tipo, String descripcion) {
        MovimientoContableEntity mc = new MovimientoContableEntity();
        mc.setTipoContable(tipo);
        mc.setDescripcionContable(descripcion);
        entityManager.persist(mc);
        return mc;
    }

    private TipoMovimientoEntity crearTipoMovimiento(
            String nombre, 
            String descripcion, 
            MovimientoContableEntity mc) {
        TipoMovimientoEntity tm = new TipoMovimientoEntity();
        tm.setTipoMovimiento(nombre);
        tm.setDescripcion(descripcion);
        tm.setMovimientoContable(mc);
        entityManager.persist(tm);
        return tm;
    }

    private EmpresaEntity crearEmpresa(String razonSocial, String rut) {
        EmpresaEntity empresa = new EmpresaEntity();
        empresa.setRazonSocial(razonSocial);
        empresa.setRut(rut);
        entityManager.persist(empresa);
        return empresa;
    }

    private CustodioEntity crearCustodio(String nombre) {
        CustodioEntity custodio = new CustodioEntity();
        custodio.setNombreCustodio(nombre);
        entityManager.persist(custodio);
        return custodio;
    }

    // ========================================================================
    // MÉTODOS DE VERIFICACIÓN Y CREACIÓN DE VISTAS
    // ========================================================================
    
    private boolean verificarVistaExiste(String vistaNombre) {
        String sql = "SELECT COUNT(*) FROM information_schema.VIEWS " +
                     "WHERE TABLE_NAME = :vistaNombre AND TABLE_SCHEMA = DATABASE()";
        Object result = entityManager.createNativeQuery(sql)
                .setParameter("vistaNombre", vistaNombre)
                .getSingleResult();
        return ((Number) result).intValue() > 0;
    }

    private void crearVistaSaldos() {
        String sql = """
            CREATE OR REPLACE VIEW saldos_view AS
            SELECT 
                `k`.`nemo_id` AS `nemo_id`,
                `k`.`empresa_id` AS `empresa_id`,
                `k`.`custodio_id` AS `custodio_id`,
                SUM(`k`.`saldo_cantidad`) AS `saldo_cantidad`,
                SUM(`k`.`saldo_valor`) AS `costo_total`,
                (CASE
                    WHEN (SUM(`k`.`saldo_cantidad`) > 0) THEN (SUM(`k`.`saldo_valor`) / SUM(`k`.`saldo_cantidad`))
                    ELSE 0
                END) AS `costo_unitario`,
                SUM(`s`.`cantidad`) AS `cantidad_mercado`,
                SUM(`s`.`saldo_valor`) AS `valor_mercado`
            FROM
                ((SELECT 
                    `kardex_view`.`empresa_id` AS `empresa_id`,
                        `kardex_view`.`custodio_id` AS `custodio_id`,
                        `kardex_view`.`nemo_id` AS `nemo_id`,
                        `kardex_view`.`saldo_cantidad` AS `saldo_cantidad`,
                        `kardex_view`.`saldo_valor` AS `saldo_valor`
                FROM
                    `kardex_view`
                WHERE
                    (`kardex_view`.`fecha_tran` = (SELECT 
                            MAX(`kv`.`fecha_tran`)
                        FROM
                            `kardex_view` `kv`
                        WHERE
                            ((`kv`.`empresa_id` = `kardex_view`.`empresa_id`)
                                AND (`kv`.`custodio_id` = `kardex_view`.`custodio_id`)
                                AND (`kv`.`nemo_id` = `kardex_view`.`nemo_id`))))) `k`
                LEFT JOIN (SELECT 
                    `saldos`.`empresa_id` AS `empresa_id`,
                        `saldos`.`custodio_id` AS `custodio_id`,
                        `saldos`.`instrumento_id` AS `nemo_id`,
                        SUM(`saldos`.`cantidad`) AS `cantidad`,
                        SUM(`saldos`.`monto_clp`) AS `saldo_valor`
                FROM
                    `saldos`
                WHERE
                    (`saldos`.`fecha` = (SELECT 
                            MAX(`s2`.`fecha`)
                        FROM
                            `saldos` `s2`
                        WHERE
                            ((`s2`.`empresa_id` = `saldos`.`empresa_id`)
                                AND (`s2`.`custodio_id` = `saldos`.`custodio_id`)
                                AND (`s2`.`instrumento_id` = `saldos`.`instrumento_id`))))
                GROUP BY `saldos`.`empresa_id` , `saldos`.`custodio_id` , `saldos`.`instrumento_id`) `s` ON (((`k`.`empresa_id` = `s`.`empresa_id`)
                    AND (`k`.`custodio_id` = `s`.`custodio_id`)
                    AND (`k`.`nemo_id` = `s`.`nemo_id`))))
            GROUP BY `k`.`nemo_id` , `k`.`empresa_id` , `k`.`custodio_id`
            HAVING (`saldo_cantidad` > 0)
        """;

        entityManager.createNativeQuery(sql).executeUpdate();
        logger.info("✓ Vista 'saldos_view' creada exitosamente");
    }
    
    private void crearVistaKardex() {
        String sql = """
            CREATE OR REPLACE VIEW `kardex_view` AS
                SELECT 
                    `t`.`id` AS `id`,
                    `t`.`empresa_id` AS `empresa_id`,
                    `t`.`custodio_id` AS `custodio_id`,
                    `t`.`cuenta` AS `cuenta`,
                    `i`.`nemo` AS `nemo`,
                    `i`.`id` AS `nemo_id`,
                    `k`.`fecha_transaccion` AS `fecha_tran`,
                    `tc`.`tipo_contable` AS `tipo_oper`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'INGRESO') THEN `k`.`cantidad`
                        ELSE NULL
                    END) AS `cant_compra`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'INGRESO') THEN `t`.`precio`
                        ELSE NULL
                    END) AS `precio_compra`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'INGRESO') THEN (`t`.`precio` * `k`.`cantidad`)
                        ELSE NULL
                    END) AS `monto_compra`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN `k`.`cantidad`
                        ELSE NULL
                    END) AS `total_fact`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN `dc`.`cantidad_usada`
                        ELSE NULL
                    END) AS `cant_usada`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN `ti`.`fecha`
                        ELSE NULL
                    END) AS `fecha_compra`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN `ti`.`precio`
                        ELSE NULL
                    END) AS `costo_fifo`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN `t`.`precio`
                        ELSE NULL
                    END) AS `precio_venta`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN (`dc`.`cantidad_usada` * `ti`.`precio`)
                        ELSE NULL
                    END) AS `costo_oper`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN (`t`.`precio` - `ti`.`precio`)
                        ELSE NULL
                    END) AS `margen`,
                    (CASE
                        WHEN (`tc`.`tipo_contable` = 'EGRESO') THEN ((`t`.`precio` - `ti`.`precio`) * `dc`.`cantidad_usada`)
                        ELSE NULL
                    END) AS `utilidad`,
                    `k`.`saldo_cantidad` AS `saldo_cantidad`,
                    `k`.`saldo_valor` AS `saldo_valor`
                FROM
                    ((((((`kardex` `k`
                    JOIN `transacciones` `t` ON ((`k`.`transaccion_id` = `t`.`id`)))
                    JOIN `instrumentos` `i` ON ((`t`.`instrumento_id` = `i`.`id`)))
                    JOIN `tipo_movimientos` `tm` ON ((`t`.`movimiento_id` = `tm`.`id`)))
                    JOIN `tipos_contables` `tc` ON ((`tm`.`movimiento_contable_id` = `tc`.`id`)))
                    LEFT JOIN `detalle_costeos` `dc` ON ((`t`.`id` = `dc`.`egreso_id`)))
                    LEFT JOIN `transacciones` `ti` ON ((`dc`.`ingreso_id` = `ti`.`id`)))
                ORDER BY `t`.`empresa_id` , `t`.`custodio_id` , `t`.`cuenta` , `i`.`id` , `k`.`fecha_transaccion` , `dc`.`ingreso_id`
            """;

        entityManager.createNativeQuery(sql).executeUpdate();
        logger.info("✓ Vista 'kardex_view' creada exitosamente");
    }
}