package uniquindio.edu.co.inmobiliaria.comportamiento;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import java.util.Optional;

/**
 * Repositorio wrapper para RegistroComportamientoAtipico que integra la persistencia de base de datos
 * con una estructura de datos personalizada en memoria (DynamicArrayList) para optimizar consultas rápidas.
 */
@Repository
public class RegistroComportamientoAtipicoRepository {

    private final RegistroComportamientoAtipicoJpaRepository jpaRepository;
    private final DynamicArrayList<RegistroComportamientoAtipico> cache;

    public RegistroComportamientoAtipicoRepository(RegistroComportamientoAtipicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.cache = new DynamicArrayList<>();
    }

    @PostConstruct
    private void initCache() {
        jpaRepository.findAllByOrderByFechaDeteccionDesc().forEach(cache::add);
    }

    public RegistroComportamientoAtipico save(RegistroComportamientoAtipico registro) {
        RegistroComportamientoAtipico saved = jpaRepository.save(registro);
        int existingIndex = -1;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() != null && cache.get(i).getId().equals(saved.getId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            cache.set(existingIndex, saved);
        } else {
            cache.add(0, saved); // Se agrega al inicio porque es el más reciente
        }
        return saved;
    }

    public DynamicArrayList<RegistroComportamientoAtipico> findAll() {
        return cache;
    }

    public Optional<RegistroComportamientoAtipico> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        for (int i = 0; i < cache.size(); i++) {
            RegistroComportamientoAtipico r = cache.get(i);
            if (id.equals(r.getId())) {
                return Optional.of(r);
            }
        }
        return jpaRepository.findById(id);
    }

    public DynamicArrayList<RegistroComportamientoAtipico> findByNivelAtencion(NivelAtencion nivelAtencion) {
        DynamicArrayList<RegistroComportamientoAtipico> result = new DynamicArrayList<>();
        for (int i = 0; i < cache.size(); i++) {
            RegistroComportamientoAtipico r = cache.get(i);
            if (r.getNivelAtencion() == nivelAtencion) {
                result.add(r);
            }
        }
        return result;
    }

    public DynamicArrayList<RegistroComportamientoAtipico> findByReferenciaId(String referenciaId) {
        DynamicArrayList<RegistroComportamientoAtipico> result = new DynamicArrayList<>();
        for (int i = 0; i < cache.size(); i++) {
            RegistroComportamientoAtipico r = cache.get(i);
            if (r.getReferenciaId() != null && r.getReferenciaId().equals(referenciaId)) {
                result.add(r);
            }
        }
        return result;
    }
}
