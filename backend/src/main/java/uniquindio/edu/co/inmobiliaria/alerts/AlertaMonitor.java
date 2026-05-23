package uniquindio.edu.co.inmobiliaria.alerts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ClienteJpaRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ContratoJpaRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.InmuebleJpaRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.OperacionJpaRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.VisitaJpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class AlertaMonitor {

    private final AlertaService alertaService;
    private final ContratoJpaRepository contratoJpaRepository;
    private final InmuebleJpaRepository inmuebleJpaRepository;
    private final VisitaJpaRepository visitaJpaRepository;
    private final OperacionJpaRepository operacionJpaRepository;
    private final ClienteJpaRepository clienteJpaRepository;

    private static final int DIAS_CONTRATO_POR_VENCER = 30;
    private static final int DIAS_SIN_VISITAS = 30;
    private static final int VISITAS_ALTA_DEMANDA = 8;
    private static final int DIAS_RESERVADO_SIN_CIERRE = 15;
    private static final int DIAS_SIN_SEGUIMIENTO_CLIENTE = 15;

    @Autowired
    public AlertaMonitor(AlertaService alertaService,
                         ContratoJpaRepository contratoJpaRepository,
                         InmuebleJpaRepository inmuebleJpaRepository,
                         VisitaJpaRepository visitaJpaRepository,
                         OperacionJpaRepository operacionJpaRepository,
                         ClienteJpaRepository clienteJpaRepository) {
        this.alertaService = alertaService;
        this.contratoJpaRepository = contratoJpaRepository;
        this.inmuebleJpaRepository = inmuebleJpaRepository;
        this.visitaJpaRepository = visitaJpaRepository;
        this.operacionJpaRepository = operacionJpaRepository;
        this.clienteJpaRepository = clienteJpaRepository;
    }

    public void verificarTodo() {
        verificarContratosPorVencer();
        verificarInmueblesSinVisitas();
        verificarInmueblesAltaDemanda();
        verificarVisitasPendientesConfirmar();
        verificarInmueblesReservadosSinCierre();
        verificarClientesSinSeguimiento();
    }

    private void verificarContratosPorVencer() {
        List<Contrato> contratos = iterableToList(contratoJpaRepository.findAll());
        LocalDate hoy = LocalDate.now();

        contratos.stream()
                .filter(Contrato::isVigente)
                .forEach(contrato -> {
                    if (contrato.getFechaVencimiento() == null
                            || contrato.getOperacion() == null
                            || contrato.getOperacion().getInmueble() == null) {
                        return;
                    }
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, contrato.getFechaVencimiento().toLocalDate());
                    if (diasRestantes < 0 || diasRestantes > DIAS_CONTRATO_POR_VENCER) {
                        return;
                    }

                    PrioridadAlerta prioridad = diasRestantes <= 7 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
                    String titulo = "Contrato " + contrato.getCodigo() + " por vencer";
                    String descripcion = "El contrato del inmueble "
                            + contrato.getOperacion().getInmueble().getCodigo()
                            + " vence en " + diasRestantes + " dias.";

                    alertaService.generarAlerta(
                            TipoAlerta.CONTRATO_PROXIMO_A_VENCER,
                            prioridad,
                            titulo,
                            descripcion,
                            contrato.getCodigo()
                    );
                });
    }

    private void verificarInmueblesSinVisitas() {
        List<Inmueble> inmuebles = iterableToList(inmuebleJpaRepository.findAll());
        List<Visita> visitas = iterableToList(visitaJpaRepository.findAll());
        LocalDate hoy = LocalDate.now();

        inmuebles.stream()
                .filter(inmueble -> inmueble.getEstado() == Estado.DISPONIBLE)
                .forEach(inmueble -> {
                    Optional<LocalDate> ultimaVisita = visitas.stream()
                            .filter(v -> esVisitaConFechaHora(v)
                                    && v.getInmueble() != null
                                    && inmueble.getCodigo().equals(v.getInmueble().getCodigo()))
                            .map(v -> LocalDateTime.of(v.getFecha(), v.getHora()).toLocalDate())
                            .max(Comparator.naturalOrder());
                    if (ultimaVisita.isEmpty()) {
                        return;
                    }

                    long diasSinVisita = ChronoUnit.DAYS.between(ultimaVisita.get(), hoy);
                    if (diasSinVisita < DIAS_SIN_VISITAS) {
                        return;
                    }

                    PrioridadAlerta prioridad = diasSinVisita >= 60 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
                    String titulo = "Inmueble " + inmueble.getCodigo() + " sin visitas";
                    String descripcion = "El inmueble lleva " + diasSinVisita + " dias sin visitas registradas.";

                    alertaService.generarAlerta(
                            TipoAlerta.INMUEBLE_SIN_VISITAS,
                            prioridad,
                            titulo,
                            descripcion,
                            inmueble.getCodigo()
                    );
                });
    }

    private void verificarInmueblesAltaDemanda() {
        List<Inmueble> inmuebles = iterableToList(inmuebleJpaRepository.findAll());
        List<Visita> visitas = iterableToList(visitaJpaRepository.findAll());

        inmuebles.forEach(inmueble -> {
            long totalVisitas = visitas.stream()
                    .filter(v -> v.getInmueble() != null
                            && inmueble.getCodigo().equals(v.getInmueble().getCodigo())
                            && v.getEstado() != EstadoVisita.CANCELADA)
                    .count();
            if (totalVisitas < VISITAS_ALTA_DEMANDA) {
                return;
            }
            PrioridadAlerta prioridad = totalVisitas >= 15 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
            String titulo = "Alta demanda en inmueble " + inmueble.getCodigo();
            String descripcion = "El inmueble ha recibido " + totalVisitas + " visitas y puede necesitar ajuste de precio o inventario.";

            alertaService.generarAlerta(
                    TipoAlerta.PROPIEDAD_ALTA_DEMANDA,
                    prioridad,
                    titulo,
                    descripcion,
                    inmueble.getCodigo()
            );
        });
    }

    private void verificarVisitasPendientesConfirmar() {
        List<Visita> pendientes = iterableToList(visitaJpaRepository.findAll()).stream()
                .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE && esVisitaConFechaHora(v))
                .collect(Collectors.toList());

        LocalDateTime ahora = LocalDateTime.now();
        pendientes.forEach(visita -> {
            LocalDateTime fechaProgramada = LocalDateTime.of(visita.getFecha(), visita.getHora());
            long horasEspera = ChronoUnit.HOURS.between(fechaProgramada, ahora);
            if (horasEspera < 24) {
                return;
            }
            PrioridadAlerta prioridad = horasEspera >= 48 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
            String titulo = "Visita pendiente por confirmar";
            String descripcion = "La visita para el cliente "
                    + (visita.getCliente() != null ? visita.getCliente().getNombre() : "desconocido")
                    + " al inmueble " + (visita.getInmueble() != null ? visita.getInmueble().getCodigo() : "desconocido")
                    + " lleva " + horasEspera + " horas sin confirmar.";

            alertaService.generarAlerta(
                    TipoAlerta.VISITA_PENDIENTE_POR_CONFIRMAR,
                    prioridad,
                    titulo,
                    descripcion,
                    visita.getCodigo()
            );
        });
    }

    private void verificarInmueblesReservadosSinCierre() {
        List<Inmueble> reservados = iterableToList(inmuebleJpaRepository.findAll()).stream()
                .filter(inmueble -> inmueble.getEstado() == Estado.RESERVADO)
                .collect(Collectors.toList());

        List<Operacion> operaciones = iterableToList(operacionJpaRepository.findAll());
        LocalDate hoy = LocalDate.now();

        reservados.forEach(inmueble -> {
            Optional<Operacion> operacion = operaciones.stream()
                    .filter(op -> op.getInmueble() != null
                            && inmueble.getCodigo().equals(op.getInmueble().getCodigo())
                            && op.getEstado() == EstadoOperacion.EN_PROCESO)
                    .max(Comparator.comparing(Operacion::getFecha));
            if (operacion.isEmpty()) {
                return;
            }
            long diasReservado = ChronoUnit.DAYS.between(operacion.get().getFecha().toLocalDate(), hoy);
            if (diasReservado < DIAS_RESERVADO_SIN_CIERRE) {
                return;
            }
            PrioridadAlerta prioridad = diasReservado >= 30 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
            String titulo = "Inmueble reservado sin cierre";
            String descripcion = "El inmueble " + inmueble.getCodigo() + " lleva " + diasReservado + " dias reservado sin cerrar la operacion.";

            alertaService.generarAlerta(
                    TipoAlerta.INMUEBLE_RESERVADO_SIN_CIERRE,
                    prioridad,
                    titulo,
                    descripcion,
                    inmueble.getCodigo()
            );
        });
    }

    private void verificarClientesSinSeguimiento() {
        List<Cliente> clientes = iterableToList(clienteJpaRepository.findAll());
        List<Visita> visitas = iterableToList(visitaJpaRepository.findAll());
        List<Operacion> operaciones = iterableToList(operacionJpaRepository.findAll());
        LocalDate hoy = LocalDate.now();

        clientes.forEach(cliente -> {
            Optional<LocalDateTime> ultimaInteraccion = StreamSupport.stream(visitas.spliterator(), false)
                    .filter(v -> v.getCliente() != null && cliente.getId().equals(v.getCliente().getId()))
                    .filter(this::esVisitaConFechaHora)
                    .map(v -> LocalDateTime.of(v.getFecha(), v.getHora()))
                    .max(Comparator.naturalOrder());

            Optional<LocalDateTime> ultimaOperacion = operaciones.stream()
                    .filter(o -> o.getCliente() != null && cliente.getId().equals(o.getCliente().getId()))
                    .map(Operacion::getFecha)
                    .max(Comparator.naturalOrder());

            Optional<LocalDateTime> ultima = ultimaInteraccion;
            if (ultimaOperacion.isPresent() && (ultima.isEmpty() || ultimaOperacion.get().isAfter(ultima.get()))) {
                ultima = ultimaOperacion;
            }
            if (ultima.isEmpty()) {
                return;
            }
            long diasSinActividad = ChronoUnit.DAYS.between(ultima.get().toLocalDate(), hoy);
            if (diasSinActividad < DIAS_SIN_SEGUIMIENTO_CLIENTE) {
                return;
            }
            PrioridadAlerta prioridad = diasSinActividad >= 30 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
            String titulo = "Cliente sin seguimiento reciente";
            String descripcion = "El cliente " + cliente.getNombre() + " no registra interacciones desde hace "
                    + diasSinActividad + " dias.";

            alertaService.generarAlerta(
                    TipoAlerta.CLIENTE_SIN_SEGUIMIENTO_RECIENTE,
                    prioridad,
                    titulo,
                    descripcion,
                    cliente.getId()
            );
        });
    }

    private <T> List<T> iterableToList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    private boolean esVisitaConFechaHora(Visita visita) {
        return visita != null && visita.getFecha() != null && visita.getHora() != null;
    }
}
