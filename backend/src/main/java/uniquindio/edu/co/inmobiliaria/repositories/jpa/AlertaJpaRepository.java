package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para alertas que expone consultas necesarias para el cache
 * y filtrado por tipo, prioridad y estado de atención.
 */
public interface AlertaJpaRepository extends CrudRepository<Alerta, String> {

    List<Alerta> findAllByOrderByFechaGeneracionDesc();

    List<Alerta> findByAtendidaFalseOrderByFechaGeneracionDesc();

    boolean existsByTipoAndReferenciaIdAndAtendidaFalse(TipoAlerta tipo, String referenciaId);

    Optional<Alerta> findFirstByTipoAndReferenciaIdAndAtendidaFalseOrderByFechaGeneracionDesc(
            TipoAlerta tipo,
            String referenciaId);

    List<Alerta> findByTipoOrderByFechaGeneracionDesc(TipoAlerta tipo);

    List<Alerta> findByPrioridadOrderByFechaGeneracionDesc(PrioridadAlerta prioridad);

    List<Alerta> findByReferenciaIdOrderByFechaGeneracionDesc(String referenciaId);

    long countByAtendidaFalse();

    long countByTipo(TipoAlerta tipo);
}

