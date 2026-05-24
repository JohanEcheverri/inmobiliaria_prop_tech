package uniquindio.edu.co.inmobiliaria.comportamiento;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

/**
 * Repositorio wrapper para HistorialPrecio que integra la persistencia de base de datos
 * con una estructura de datos personalizada en memoria (DynamicArrayList) para optimizar consultas rápidas.
 */
@Repository
public class HistorialPrecioRepository {

    private final HistorialPrecioJpaRepository jpaRepository;
    private final DynamicArrayList<HistorialPrecio> cache;

    public HistorialPrecioRepository(HistorialPrecioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.cache = new DynamicArrayList<>();
    }

    @PostConstruct
    private void initCache() {
        jpaRepository.findAllByOrderByFechaCambioDesc().forEach(cache::add);
    }

    public HistorialPrecio save(HistorialPrecio hp) {
        HistorialPrecio saved = jpaRepository.save(hp);
        cache.add(0, saved); // Se agrega al inicio porque es el más reciente
        return saved;
    }

    public DynamicArrayList<HistorialPrecio> findAll() {
        return cache;
    }

    public DynamicArrayList<HistorialPrecio> findByCodigoInmueble(String codigoInmueble) {
        DynamicArrayList<HistorialPrecio> result = new DynamicArrayList<>();
        for (int i = 0; i < cache.size(); i++) {
            HistorialPrecio item = cache.get(i);
            if (item.getCodigoInmueble() != null && item.getCodigoInmueble().equals(codigoInmueble)) {
                result.add(item);
            }
        }
        return result;
    }
}
