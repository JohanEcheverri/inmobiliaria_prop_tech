package uniquindio.edu.co.inmobiliaria.comportamiento;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

/**
 * Interfaz JPA para operaciones básicas de base de datos de HistorialPrecio.
 */
public interface HistorialPrecioJpaRepository extends CrudRepository<HistorialPrecio, Long> {
    List<HistorialPrecio> findByCodigoInmuebleOrderByFechaCambioDesc(String codigoInmueble);
    List<HistorialPrecio> findAllByOrderByFechaCambioDesc();
}
