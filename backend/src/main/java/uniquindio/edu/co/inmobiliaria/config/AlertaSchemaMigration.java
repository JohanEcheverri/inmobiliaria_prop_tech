package uniquindio.edu.co.inmobiliaria.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajusta columnas de {@code alerta} cuando la BD quedó con ENUMs antiguos
 * (Hibernate ddl-auto=update no siempre amplía valores ENUM en MariaDB).
 */
@Component
@Order(1)
public class AlertaSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AlertaSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public AlertaSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE alerta MODIFY COLUMN tipo VARCHAR(64) NOT NULL");
            jdbcTemplate.execute("ALTER TABLE alerta MODIFY COLUMN prioridad VARCHAR(16)");
            jdbcTemplate.execute("ALTER TABLE alerta MODIFY COLUMN descripcion TEXT");
            log.debug("Esquema de tabla alerta verificado/actualizado.");
        } catch (Exception e) {
            log.warn("No se pudo migrar esquema de alerta (puede ignorarse si ya está actualizado): {}", e.getMessage());
        }
    }
}
