package uniquindio.edu.co.inmobiliaria.alerts;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.repositories.AlertaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    /**
     * Genera una nueva alerta o retorna la existente abierta del mismo tipo y referencia.
     */
    public Optional<Alerta> generarAlerta(TipoAlerta tipo,
                                         PrioridadAlerta prioridad,
                                         String titulo,
                                         String descripcion,
                                         String referenciaId) {
        Objects.requireNonNull(tipo, "El tipo de alerta no puede ser nulo");
        Objects.requireNonNull(prioridad, "La prioridad no puede ser nula");
        Objects.requireNonNull(titulo, "El titulo no puede ser nulo");
        Objects.requireNonNull(descripcion, "La descripcion no puede ser nula");

        if (referenciaId == null || referenciaId.isBlank()) {
            throw new IllegalArgumentException("La referencia de la alerta no puede estar vacia");
        }

        if (alertaRepository.existsOpenAlert(tipo, referenciaId)) {
            return alertaRepository.findOpenAlert(tipo, referenciaId);
        }

        Alerta alerta = Alerta.builder()
                .tipo(tipo)
                .prioridad(prioridad)
                .titulo(titulo)
                .descripcion(descripcion)
                .fechaGeneracion(LocalDateTime.now())
                .atendida(false)
                .referenciaId(referenciaId)
                .build();

        return Optional.of(alertaRepository.save(alerta));
    }

    /**
     * Obtiene todas las alertas pendientes de atender, ordenadas por fecha.
     */
    public List<Alerta> obtenerAlertasPendientes() {
        return alertaRepository.findPending();
    }

    /**
     * Obtiene el historial completo de alertas.
     */
    public List<Alerta> obtenerHistorial() {
        return alertaRepository.findAll();
    }

    /**
     * Obtiene alertas por tipo específico.
     */
    public List<Alerta> obtenerAlertasPorTipo(TipoAlerta tipo) {
        return alertaRepository.findByTipo(tipo);
    }

    /**
     * Obtiene alertas por prioridad.
     */
    public List<Alerta> obtenerAlertasPorPrioridad(PrioridadAlerta prioridad) {
        return alertaRepository.findByPrioridad(prioridad);
    }

    /**
     * Obtiene todas las alertas relacionadas con una entidad específica.
     */
    public List<Alerta> obtenerAlertasPorReferencia(String referenciaId) {
        return alertaRepository.findByReferenciaId(referenciaId);
    }

    /**
     * Marca una alerta como atendida.
     */
    public Optional<Alerta> atenderAlerta(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return alertaRepository.markAsAttended(codigo);
    }

    /**
     * Obtiene una alerta por su código.
     */
    public Optional<Alerta> obtenerAlerta(String codigo) {
        return alertaRepository.findById(codigo);
    }

    /**
     * Elimina una alerta del sistema.
     */
    public void eliminarAlerta(String codigo) {
        alertaRepository.deleteById(codigo);
    }

    /**
     * Cuenta el número de alertas pendientes.
     */
    public long contarAlertasPendientes() {
        return alertaRepository.countPending();
    }

    /**
     * Cuenta el número de alertas de un tipo específico.
     */
    public long contarAlertasPorTipo(TipoAlerta tipo) {
        return alertaRepository.countByTipo(tipo);
    }

    public List<Alerta> obtenerContratosProximosAVencer() {
        return alertaRepository.findContractsProximosAVencer();
    }

    public Optional<Alerta> obtenerProximaAlertaContratoPorVencer() {
        return alertaRepository.peekNextContractAlert();
    }

    public Optional<Alerta> atenderProximaAlertaContratoPorVencer() {
        Optional<Alerta> alerta = alertaRepository.pollNextContractAlert();
        alerta.ifPresent(a -> {
            alertaRepository.markAsAttended(a.getCodigo());
        });
        return alerta;
    }

    /**
     * Obtiene resumen de alertas: pendientes y por prioridad.
     */
    public AlertaResumen obtenerResumen() {
        List<Alerta> pendientes = obtenerAlertasPendientes();
        long alertasAltas = pendientes.stream()
                .filter(a -> a.getPrioridad() == PrioridadAlerta.ALTA)
                .count();
        long alertasMedias = pendientes.stream()
                .filter(a -> a.getPrioridad() == PrioridadAlerta.MEDIA)
                .count();
        long alertasBasjas = pendientes.stream()
                .filter(a -> a.getPrioridad() == PrioridadAlerta.BAJA)
                .count();

        return new AlertaResumen(
                pendientes.size(),
                (int) alertasAltas,
                (int) alertasMedias,
                (int) alertasBasjas
        );
    }

    /**
     * DTO para el resumen de alertas.
     */
    public static class AlertaResumen {
        public final int totalPendientes;
        public final int alertasAltas;
        public final int alertasMedias;
        public final int alertasBasjas;

        public AlertaResumen(int totalPendientes, int alertasAltas, int alertasMedias, int alertasBasjas) {
            this.totalPendientes = totalPendientes;
            this.alertasAltas = alertasAltas;
            this.alertasMedias = alertasMedias;
            this.alertasBasjas = alertasBasjas;
        }

        @Override
        public String toString() {
            return "AlertaResumen{" +
                    "totalPendientes=" + totalPendientes +
                    ", alertasAltas=" + alertasAltas +
                    ", alertasMedias=" + alertasMedias +
                    ", alertasBasjas=" + alertasBasjas +
                    '}';
        }
    }
}

