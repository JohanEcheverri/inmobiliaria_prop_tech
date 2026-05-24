package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VisitaService {

    private final VisitasRepository visitasRepository;
    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final AsesorRepository asesorRepository;
    private final EventoHistorialService eventoHistorialService;

    public VisitaService(VisitasRepository visitasRepository,
                         ClienteRepository clienteRepository,
                         InmuebleRepository inmuebleRepository,
                         AsesorRepository asesorRepository,
                         EventoHistorialService eventoHistorialService) {
        this.visitasRepository = visitasRepository;
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
        this.eventoHistorialService = eventoHistorialService;
    }

    @Transactional
    public Visita scheduleVisit(String clienteId,
                                String inmuebleCodigo,
                                String asesorId,
                                LocalDate fecha,
                                LocalTime hora,
                                String observaciones) {
        Cliente cliente = obtenerCliente(clienteId);
        Inmueble inmueble = obtenerInmueble(inmuebleCodigo);
        Asesor asesor = obtenerAsesor(asesorId);
        validarFechaHora(fecha, hora);
        validarDisponibilidad(null, cliente.getId(), inmueble.getCodigo(), asesor.getId(), fecha, hora);

        Visita visita = new Visita(
                generarCodigo(),
                cliente,
                inmueble,
                fecha,
                hora,
                EstadoVisita.PENDIENTE,
                asesor,
                normalizarObservaciones(observaciones)
        );
        visitasRepository.save(visita);
        eventoHistorialService.registrarEvento(cliente, inmueble, TipoEventoHistorial.VISITA);
        return visita;
    }

    @Transactional
    public Visita rescheduleVisit(String codigoVisita,
                                  LocalDate nuevaFecha,
                                  LocalTime nuevaHora,
                                  String observaciones) {
        Visita visita = obtenerVisita(codigoVisita);
        validarVisitaModificable(visita);
        validarFechaHora(nuevaFecha, nuevaHora);

        String clienteId = visita.getCliente() != null ? visita.getCliente().getId() : null;
        String inmuebleCodigo = visita.getInmueble() != null ? visita.getInmueble().getCodigo() : null;
        String asesorId = visita.getAsesotAsignado() != null ? visita.getAsesotAsignado().getId() : null;
        validarDisponibilidad(visita.getCodigo(), clienteId, inmuebleCodigo, asesorId, nuevaFecha, nuevaHora);

        Visita reprogramada = copiarVisita(visita);
        reprogramada.setFecha(nuevaFecha);
        reprogramada.setHora(nuevaHora);
        reprogramada.setEstado(EstadoVisita.REPROGRAMADA);
        reprogramada.setObservaciones(combinarObservaciones(visita.getObservaciones(), observaciones));

        visitasRepository.update(reprogramada);
        return reprogramada;
    }

    @Transactional
    public Visita cancelVisit(String codigoVisita, String observaciones) {
        Visita visita = obtenerVisita(codigoVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            return visita;
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new IllegalArgumentException("No se puede cancelar una visita ya realizada");
        }

        Visita cancelada = copiarVisita(visita);
        cancelada.setEstado(EstadoVisita.CANCELADA);
        cancelada.setObservaciones(combinarObservaciones(visita.getObservaciones(), observaciones));

        visitasRepository.update(cancelada);
        return cancelada;
    }

    @Transactional
    public Visita confirmVisit(String codigoVisita, String observaciones) {
        Visita visita = obtenerVisita(codigoVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new IllegalArgumentException("No se puede confirmar una visita cancelada");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            return visita;
        }

        Visita confirmada = copiarVisita(visita);
        confirmada.setEstado(EstadoVisita.CONFIRMADA);
        confirmada.setObservaciones(combinarObservaciones(visita.getObservaciones(), observaciones));
        visitasRepository.update(confirmada);
        return confirmada;
    }

    @Transactional
    public Visita completeVisit(String codigoVisita, String observaciones) {
        Visita visita = obtenerVisita(codigoVisita);
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new IllegalArgumentException("No se puede completar una visita cancelada");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            return visita;
        }

        Visita realizada = copiarVisita(visita);
        realizada.setEstado(EstadoVisita.REALIZADA);
        realizada.setObservaciones(combinarObservaciones(visita.getObservaciones(), observaciones));
        visitasRepository.update(realizada);

        if (realizada.getCliente() != null && realizada.getInmueble() != null) {
            eventoHistorialService.registrarEvento(realizada.getCliente(), realizada.getInmueble(), TipoEventoHistorial.VISITA);
        }
        return realizada;
    }

    public Optional<Visita> findVisitByCode(String codigoVisita) {
        return visitasRepository.findByCodigo(codigoVisita);
    }

    public DynamicArrayList<Visita> listVisits() {
        return visitasRepository.findAll();
    }

    public DynamicArrayList<Visita> listVisitsByClient(String clienteId) {
        return visitasRepository.findByClienteId(clienteId);
    }

    public DynamicArrayList<Visita> listVisitsByProperty(String inmuebleCodigo) {
        return visitasRepository.findByInmuebleCodigo(inmuebleCodigo);
    }

    public DynamicArrayList<Visita> listVisitsByAdvisor(String asesorId) {
        return visitasRepository.findByAsesorId(asesorId);
    }

    public DynamicArrayList<Visita> listVisitsByStatus(EstadoVisita estado) {
        return visitasRepository.findByEstado(estado);
    }

    public Visita processPendingVisit() {
        return visitasRepository.procesarVisita();
    }

    private Cliente obtenerCliente(String clienteId) {
        if (estaVacio(clienteId)) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));
    }

    private Inmueble obtenerInmueble(String inmuebleCodigo) {
        if (estaVacio(inmuebleCodigo)) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        return inmuebleRepository.findById(inmuebleCodigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + inmuebleCodigo));
    }

    private Asesor obtenerAsesor(String asesorId) {
        if (estaVacio(asesorId)) {
            throw new IllegalArgumentException("El id del asesor es obligatorio");
        }
        return asesorRepository.findById(asesorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un asesor con el id: " + asesorId));
    }

    private Visita obtenerVisita(String codigoVisita) {
        if (estaVacio(codigoVisita)) {
            throw new IllegalArgumentException("El código de la visita es obligatorio");
        }
        return visitasRepository.findByCodigo(codigoVisita)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una visita con el código: " + codigoVisita));
    }

    private void validarFechaHora(LocalDate fecha, LocalTime hora) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la visita es obligatoria");
        }
        if (hora == null) {
            throw new IllegalArgumentException("La hora de la visita es obligatoria");
        }
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La visita no puede programarse en una fecha u hora pasada");
        }
    }

    private void validarVisitaModificable(Visita visita) {
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new IllegalArgumentException("No se puede reprogramar una visita cancelada");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new IllegalArgumentException("No se puede reprogramar una visita ya realizada");
        }
    }

    private void validarDisponibilidad(String codigoActual,
                                       String clienteId,
                                       String inmuebleCodigo,
                                       String asesorId,
                                       LocalDate fecha,
                                       LocalTime hora) {
        DynamicArrayList<Visita> visitas = visitasRepository.findByFecha(fecha);
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (esMismaVisita(codigoActual, visita) || !esVisitaActiva(visita) || !hora.equals(visita.getHora())) {
                continue;
            }
            if (visita.getCliente() != null && visita.getCliente().getId().equals(clienteId)) {
                throw new IllegalArgumentException("El cliente ya tiene una visita activa en esa fecha y hora");
            }
            if (visita.getInmueble() != null && visita.getInmueble().getCodigo().equals(inmuebleCodigo)) {
                throw new IllegalArgumentException("El inmueble ya tiene una visita activa en esa fecha y hora");
            }
            if (visita.getAsesotAsignado() != null && visita.getAsesotAsignado().getId().equals(asesorId)) {
                throw new IllegalArgumentException("El asesor ya tiene una visita activa en esa fecha y hora");
            }
        }
    }

    private boolean esMismaVisita(String codigoActual, Visita visita) {
        return codigoActual != null && visita != null && codigoActual.equals(visita.getCodigo());
    }

    private boolean esVisitaActiva(Visita visita) {
        return visita.getEstado() == EstadoVisita.PENDIENTE
                || visita.getEstado() == EstadoVisita.CONFIRMADA
                || visita.getEstado() == EstadoVisita.REPROGRAMADA;
    }

    private Visita copiarVisita(Visita visita) {
        return new Visita(
                visita.getCodigo(),
                visita.getCliente(),
                visita.getInmueble(),
                visita.getFecha(),
                visita.getHora(),
                visita.getEstado(),
                visita.getAsesotAsignado(),
                visita.getObservaciones()
        );
    }

    private String generarCodigo() {
        String codigo;
        do {
            codigo = "VIS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (visitasRepository.existsByCodigo(codigo));
        return codigo;
    }

    private String combinarObservaciones(String anteriores, String nuevas) {
        String nuevasNormalizadas = normalizarObservaciones(nuevas);
        if (nuevasNormalizadas == null) {
            return anteriores;
        }
        if (anteriores == null || anteriores.isBlank()) {
            return nuevasNormalizadas;
        }
        return anteriores + " | " + nuevasNormalizadas;
    }

    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        return observaciones.trim();
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
