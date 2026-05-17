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

    public List<Alerta> obtenerAlertasPendientes() {
        return alertaRepository.findPending();
    }

    public List<Alerta> obtenerHistorial() {
        return alertaRepository.findAll();
    }

    public Optional<Alerta> atenderAlerta(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return alertaRepository.markAsAttended(codigo);
    }
}
