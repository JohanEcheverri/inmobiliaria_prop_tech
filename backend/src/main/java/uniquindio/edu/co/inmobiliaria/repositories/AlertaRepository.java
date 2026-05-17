package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AlertaJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AlertaRepository {

    private final AlertaJpaRepository alertaJpaRepository;

    public AlertaRepository(AlertaJpaRepository alertaJpaRepository) {
        this.alertaJpaRepository = alertaJpaRepository;
    }

    public Alerta save(Alerta alerta) {
        Objects.requireNonNull(alerta, "La alerta no puede ser nula");
        if (alerta.getFechaGeneracion() == null) {
            alerta.setFechaGeneracion(LocalDateTime.now());
        }
        return alertaJpaRepository.save(alerta);
    }

    public List<Alerta> findAll() {
        return alertaJpaRepository.findAllByOrderByFechaGeneracionDesc();
    }

    public List<Alerta> findPending() {
        return alertaJpaRepository.findByAtendidaFalseOrderByFechaGeneracionDesc();
    }

    public Optional<Alerta> findByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return alertaJpaRepository.findById(codigo);
    }

    public boolean existsOpenAlert(TipoAlerta tipo, String referenciaId) {
        return alertaJpaRepository.existsByTipoAndReferenciaIdAndAtendidaFalse(tipo, referenciaId);
    }

    public Optional<Alerta> findOpenAlert(TipoAlerta tipo, String referenciaId) {
        return alertaJpaRepository.findFirstByTipoAndReferenciaIdAndAtendidaFalseOrderByFechaGeneracionDesc(
                tipo, referenciaId);
    }

    public Optional<Alerta> markAsAttended(String codigo) {
        Optional<Alerta> alerta = findByCodigo(codigo);
        if (alerta.isEmpty()) {
            return Optional.empty();
        }
        Alerta encontrada = alerta.get();
        if (!encontrada.isAtendida()) {
            encontrada.setAtendida(true);
            encontrada.setFechaAtencion(LocalDateTime.now());
            alertaJpaRepository.save(encontrada);
        }
        return Optional.of(encontrada);
    }
}
