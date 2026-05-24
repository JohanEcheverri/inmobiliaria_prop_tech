package uniquindio.edu.co.inmobiliaria.comportamiento;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

/**
 * Interfaz JPA para operaciones básicas de base de datos de RegistroComportamientoAtipico.
 */
public interface RegistroComportamientoAtipicoJpaRepository extends CrudRepository<RegistroComportamientoAtipico, Long> {
    List<RegistroComportamientoAtipico> findAllByOrderByFechaDeteccionDesc();
    List<RegistroComportamientoAtipico> findByNivelAtencionOrderByFechaDeteccionDesc(NivelAtencion nivelAtencion);
    List<RegistroComportamientoAtipico> findByReferenciaIdOrderByFechaDeteccionDesc(String referenciaId);
}
