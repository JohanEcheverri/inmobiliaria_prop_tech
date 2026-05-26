package uniquindio.edu.co.inmobiliaria.repositories;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AlertaJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.PriorityQueue;
import uniquindio.edu.co.inmobiliaria.structures.Queue;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repositorio que mantiene en memoria un historial de alertas, una cola de
 * alertas pendientes y una cola priorizada para contratos próximos a vencer.
 * Sincroniza con AlertaJpaRepository y proporciona operaciones de consulta y
 * manipulación con semántica de cache.
 */
public class AlertaRepository {

    private final AlertaJpaRepository alertaJpaRepository;
    private final SinglyLinkedList<Alerta> historialAlertas;
    private final Queue<Alerta> alertasPendientes;
    private final PriorityQueue<Alerta> contratosProximosAVencer;

    public AlertaRepository(AlertaJpaRepository alertaJpaRepository) {
        this.alertaJpaRepository = alertaJpaRepository;
        this.historialAlertas = new SinglyLinkedList<>();
        this.alertasPendientes = new Queue<>();
        this.contratosProximosAVencer = new PriorityQueue<>(
                Comparator.comparingInt((Alerta alerta) -> prioridadValue(alerta.getPrioridad()))
                        .thenComparing(Alerta::getFechaGeneracion)
        );
    }

    @PostConstruct
    private void initCache() {
        alertaJpaRepository.findAllByOrderByFechaGeneracionDesc()
                .forEach(alerta -> cacheAlerta(alerta, false));
    }

    private static int prioridadValue(PrioridadAlerta prioridad) {
        if (prioridad == null) {
            return Integer.MAX_VALUE;
        }
        return switch (prioridad) {
            case ALTA -> 0;
            case MEDIA -> 1;
            case BAJA -> 2;
        };
    }

    private void cacheAlerta(Alerta alerta) {
        cacheAlerta(alerta, true);
    }

    private void cacheAlerta(Alerta alerta, boolean addToFront) {
        if (alerta == null) {
            return;
        }
        historialAlertas.remove(alerta);
        if (addToFront) {
            historialAlertas.addFirst(alerta);
        } else {
            historialAlertas.addLast(alerta);
        }
        if (alerta.isAtendida()) {
            alertasPendientes.remove(alerta);
            contratosProximosAVencer.remove(alerta);
            return;
        }
        if (!alertasPendientes.contains(alerta)) {
            alertasPendientes.enqueue(alerta);
        }
        if (alerta.getTipo() == TipoAlerta.CONTRATO_PROXIMO_A_VENCER && !contratosProximosAVencer.contains(alerta)) {
            contratosProximosAVencer.enqueue(alerta);
        }
    }

    /**
     * Persiste una alerta y la coloca en el cache/local queues para atención.
     * Valida nulidad y actualiza estructuras auxiliares.
     *
     * @param alerta alerta a persistir
     * @return alerta persistida
     */
    public Alerta save(Alerta alerta) {
        if (alerta == null) {
            throw new IllegalArgumentException("La alerta no puede ser nula");
        }
        Alerta persisted = alertaJpaRepository.save(alerta);
        cacheAlerta(persisted);
        return persisted;
    }

    public boolean existsOpenAlert(TipoAlerta tipo, String referenciaId) {
        if (tipo == null || referenciaId == null || referenciaId.isBlank()) {
            return false;
        }
        return alertaJpaRepository.existsByTipoAndReferenciaIdAndAtendidaFalse(tipo, referenciaId);
    }

    public Optional<Alerta> findOpenAlert(TipoAlerta tipo, String referenciaId) {
        if (tipo == null || referenciaId == null || referenciaId.isBlank()) {
            return Optional.empty();
        }
        return alertaJpaRepository.findFirstByTipoAndReferenciaIdAndAtendidaFalseOrderByFechaGeneracionDesc(tipo, referenciaId);
    }

    /**
     * Retorna la lista de alertas pendientes encoladas para atención.
     *
     * @return lista de alertas pendientes
     */
    public List<Alerta> findPending() {
        return alertasPendientes.toList();
    }

    /**
     * Retorna el historial completo de alertas (orden reciente primero).
     *
     * @return lista completa de alertas
     */
    public List<Alerta> findAll() {
        return historialAlertas.toList();
    }

    /**
     * Busca alertas por su tipo y las ordena por fecha de generación descendente.
     *
     * @param tipo tipo de alerta
     * @return lista de alertas que coinciden con el tipo
     */
    public List<Alerta> findByTipo(TipoAlerta tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de alerta no puede ser nulo");
        }
        return alertaJpaRepository.findByTipoOrderByFechaGeneracionDesc(tipo);
    }

    public List<Alerta> findByPrioridad(PrioridadAlerta prioridad) {
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad de alerta no puede ser nula");
        }
        return alertaJpaRepository.findByPrioridadOrderByFechaGeneracionDesc(prioridad);
    }

    public List<Alerta> findByReferenciaId(String referenciaId) {
        if (referenciaId == null || referenciaId.isBlank()) {
            throw new IllegalArgumentException("La referencia de la alerta no puede estar vacia");
        }
        return alertaJpaRepository.findByReferenciaIdOrderByFechaGeneracionDesc(referenciaId);
    }

    public Optional<Alerta> findById(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return alertaJpaRepository.findById(codigo);
    }

    /**
     * Marca una alerta como atendida, registra fecha de atención y actualiza las
     * colas internas (pendientes y contratos próximos).
     *
     * @param codigo código de la alerta a marcar
     * @return Optional con la alerta actualizada
     */
    @Transactional
    public Optional<Alerta> markAsAttended(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return alertaJpaRepository.findById(codigo)
                .map(alerta -> {
                    alerta.setAtendida(true);
                    alerta.setFechaAtencion(LocalDateTime.now());
                    Alerta updated = alertaJpaRepository.save(alerta);
                    alertasPendientes.remove(updated);
                    contratosProximosAVencer.remove(updated);
                    return updated;
                });
    }

    public void deleteById(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la alerta no puede estar vacio");
        }
        alertaJpaRepository.findById(codigo).ifPresent(alerta -> {
            alertasPendientes.remove(alerta);
            contratosProximosAVencer.remove(alerta);
            historialAlertas.remove(alerta);
        });
        alertaJpaRepository.deleteById(codigo);
    }

    public long countPending() {
        return alertasPendientes.size();
    }

    public long countByTipo(TipoAlerta tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de alerta no puede ser nulo");
        }
        return alertaJpaRepository.countByTipo(tipo);
    }

    public List<Alerta> findContractsProximosAVencer() {
        return contratosProximosAVencer.toList();
    }

    public Optional<Alerta> peekNextContractAlert() {
        if (contratosProximosAVencer.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(contratosProximosAVencer.peek());
    }

    public Optional<Alerta> pollNextContractAlert() {
        if (contratosProximosAVencer.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(contratosProximosAVencer.dequeue());
    }
}
