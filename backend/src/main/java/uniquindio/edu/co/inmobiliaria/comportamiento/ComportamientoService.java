package uniquindio.edu.co.inmobiliaria.comportamiento;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaService;
import uniquindio.edu.co.inmobiliaria.models.entities.*;
import uniquindio.edu.co.inmobiliaria.models.enums.*;
import uniquindio.edu.co.inmobiliaria.repositories.*;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio encargado de la lógica de análisis y detección de comportamientos atípicos.
 */
@Service
public class ComportamientoService {

    private final AlertaService alertaService;
    private final InmuebleRepository inmuebleRepository;
    private final VisitasRepository visitasRepository;
    private final OperacionRepository operacionRepository;
    private final ClienteRepository clienteRepository;
    private final AsesorRepository asesorRepository;
    private final HistorialPrecioRepository precioRepository;
    private final RegistroComportamientoAtipicoRepository registroRepository;

    @Autowired
    public ComportamientoService(
            AlertaService alertaService,
            InmuebleRepository inmuebleRepository,
            VisitasRepository visitasRepository,
            OperacionRepository operacionRepository,
            ClienteRepository clienteRepository,
            AsesorRepository asesorRepository,
            HistorialPrecioRepository precioRepository,
            RegistroComportamientoAtipicoRepository registroRepository) {
        this.alertaService = alertaService;
        this.inmuebleRepository = inmuebleRepository;
        this.visitasRepository = visitasRepository;
        this.operacionRepository = operacionRepository;
        this.clienteRepository = clienteRepository;
        this.asesorRepository = asesorRepository;
        this.precioRepository = precioRepository;
        this.registroRepository = registroRepository;
    }

    /**
     * Ejecuta el análisis completo y guarda registros/alertas de todo comportamiento atípico detectado.
     */
    public void analizarComportamientoAtipico() {
        detectarExcesoVisitasSinCierre();
        detectarClientesSinContinuidad();
        detectarAsesoresSobrecarga();
        detectarPrecioCambioFrecuente();
        detectarConcentracionInteresZona();
    }

    /**
     * Regla 1: Inmuebles con un número anormalmente alto de visitas sin cierre.
     */
    private void detectarExcesoVisitasSinCierre() {
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            if (inmueble.getEstado() != Estado.DISPONIBLE) {
                continue;
            }

            DynamicArrayList<Visita> visitas = visitasRepository.findByInmuebleCodigo(inmueble.getCodigo());
            int visitasRealizadas = 0;
            for (int j = 0; j < visitas.size(); j++) {
                if (visitas.get(j).getEstado() == EstadoVisita.REALIZADA) {
                    visitasRealizadas++;
                }
            }

            if (visitasRealizadas >= 5) {
                PrioridadAlerta prioridad = visitasRealizadas >= 10 ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
                NivelAtencion nivel = visitasRealizadas >= 10 ? NivelAtencion.ALTO : NivelAtencion.MEDIO;

                String titulo = "Exceso de visitas sin cierre";
                String descripcion = "El inmueble " + inmueble.getCodigo() + " registra " + visitasRealizadas + " visitas realizadas pero sigue disponible.";

                // Generar Alerta
                alertaService.generarAlerta(
                        TipoAlerta.INMUEBLE_EXCESO_VISITAS_SIN_CIERRE,
                        prioridad,
                        titulo,
                        descripcion,
                        inmueble.getCodigo()
                );

                // Registrar Evento
                registrarEventoSiNoExiste(
                        TipoAlerta.INMUEBLE_EXCESO_VISITAS_SIN_CIERRE,
                        descripcion,
                        inmueble.getCodigo(),
                        nivel
                );
            }
        }
    }

    /**
     * Regla 2: Clientes que agendan múltiples visitas en corto tiempo sin continuidad.
     */
    private void detectarClientesSinContinuidad() {
        DynamicArrayList<Cliente> clientes = clienteRepository.findAll();
        LocalDate haceUnaSemana = LocalDate.now().minusDays(7);

        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.get(i);
            DynamicArrayList<Visita> visitas = visitasRepository.findByClienteId(cliente.getId());

            int visitasRecientes = 0;
            for (int j = 0; j < visitas.size(); j++) {
                Visita v = visitas.get(j);
                if (v.getFecha() != null && !v.getFecha().isBefore(haceUnaSemana)) {
                    visitasRecientes++;
                }
            }

            if (visitasRecientes >= 3) {
                DynamicArrayList<Operacion> operaciones = operacionRepository.findByIdCliente(cliente.getId());
                boolean tieneOperacionCompletada = false;
                for (int j = 0; j < operaciones.size(); j++) {
                    if (operaciones.get(j).getEstado() == EstadoOperacion.COMPLETADA) {
                        tieneOperacionCompletada = true;
                        break;
                    }
                }

                if (!tieneOperacionCompletada) {
                    String descripcion = "El cliente " + cliente.getNombre() + " (" + cliente.getId() + ") agendó "
                            + visitasRecientes + " visitas en los últimos 7 días sin concretar una operación comercial.";

                    alertaService.generarAlerta(
                            TipoAlerta.CLIENTE_MULTIPLES_VISITAS_SIN_CONTINUIDAD,
                            PrioridadAlerta.MEDIA,
                            "Cliente agendando visitas compulsivamente",
                            descripcion,
                            cliente.getId()
                    );

                    registrarEventoSiNoExiste(
                            TipoAlerta.CLIENTE_MULTIPLES_VISITAS_SIN_CONTINUIDAD,
                            descripcion,
                            cliente.getId(),
                            NivelAtencion.MEDIO
                    );
                }
            }
        }
    }

    /**
     * Regla 3: Asesores con sobrecarga excesiva de atención.
     */
    private void detectarAsesoresSobrecarga() {
        DynamicArrayList<Asesor> asesores = asesorRepository.asesoresPorId.values();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        DynamicArrayList<Visita> visitas = visitasRepository.findAll();

        for (int i = 0; i < asesores.size(); i++) {
            Asesor asesor = asesores.get(i);

            int propiedadesActivas = 0;
            for (int j = 0; j < inmuebles.size(); j++) {
                Inmueble inm = inmuebles.get(j);
                if (inm.getAsesor() != null && asesor.getId().equals(inm.getAsesor().getId()) && inm.getEstado() == Estado.DISPONIBLE) {
                    propiedadesActivas++;
                }
            }

            int visitasActivas = 0;
            for (int j = 0; j < visitas.size(); j++) {
                Visita vis = visitas.get(j);
                if (vis.getAsesotAsignado() != null && asesor.getId().equals(vis.getAsesotAsignado().getId())
                        && (vis.getEstado() == EstadoVisita.PENDIENTE || vis.getEstado() == EstadoVisita.CONFIRMADA)) {
                    visitasActivas++;
                }
            }

            if (propiedadesActivas >= 5 || visitasActivas >= 8) {
                boolean esCritico = propiedadesActivas >= 8 || visitasActivas >= 12;
                PrioridadAlerta prioridad = esCritico ? PrioridadAlerta.ALTA : PrioridadAlerta.MEDIA;
                NivelAtencion nivel = esCritico ? NivelAtencion.CRITICO : NivelAtencion.ALTO;

                String descripcion = "El asesor " + asesor.getNombre() + " (" + asesor.getId() + ") tiene sobrecarga comercial: "
                        + propiedadesActivas + " propiedades disponibles asignadas y " + visitasActivas + " visitas activas programadas.";

                alertaService.generarAlerta(
                        TipoAlerta.ASESOR_SOBRECARGA_ATENCION,
                        prioridad,
                        "Asesor con sobrecarga de trabajo",
                        descripcion,
                        asesor.getId()
                );

                registrarEventoSiNoExiste(
                        TipoAlerta.ASESOR_SOBRECARGA_ATENCION,
                        descripcion,
                        asesor.getId(),
                        nivel
                );
            }
        }
    }

    /**
     * Regla 4: Propiedades cuyo precio cambia con demasiada frecuencia.
     */
    private void detectarPrecioCambioFrecuente() {
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        LocalDateTime haceUnMes = LocalDateTime.now().minusDays(30);

        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            DynamicArrayList<HistorialPrecio> historial = precioRepository.findByCodigoInmueble(inmueble.getCodigo());

            int cambiosRecientes = 0;
            for (int j = 0; j < historial.size(); j++) {
                HistorialPrecio hp = historial.get(j);
                if (hp.getFechaCambio() != null && hp.getFechaCambio().isAfter(haceUnMes)) {
                    cambiosRecientes++;
                }
            }

            if (cambiosRecientes >= 3) {
                String descripcion = "La propiedad " + inmueble.getCodigo() + " cambió su precio " + cambiosRecientes
                        + " veces en los últimos 30 días.";

                alertaService.generarAlerta(
                        TipoAlerta.PROPIEDAD_PRECIO_CAMBIO_FRECUENTE,
                        PrioridadAlerta.MEDIA,
                        "Inestabilidad de precio en propiedad",
                        descripcion,
                        inmueble.getCodigo()
                );

                registrarEventoSiNoExiste(
                        TipoAlerta.PROPIEDAD_PRECIO_CAMBIO_FRECUENTE,
                        descripcion,
                        inmueble.getCodigo(),
                        NivelAtencion.MEDIO
                );
            }
        }
    }

    /**
     * Regla 5: Concentración de interés en una misma zona en un tiempo reducido.
     */
    private void detectarConcentracionInteresZona() {
        DynamicArrayList<Visita> visitas = visitasRepository.findAll();
        LocalDate haceUnaSemana = LocalDate.now().minusDays(7);

        HashTable<Zona, Integer> conteoZonas = new HashTable<>();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getFecha() == null || visita.getFecha().isBefore(haceUnaSemana)) {
                continue;
            }
            if (visita.getInmueble() == null || visita.getInmueble().getBarrio() == null) {
                continue;
            }

            Zona zona = visita.getInmueble().getBarrio().getZona();
            if (zona != null) {
                int actual = conteoZonas.containsKey(zona) ? conteoZonas.get(zona) : 0;
                conteoZonas.put(zona, actual + 1);
            }
        }

        DynamicArrayList<Zona> zonas = conteoZonas.keys();
        for (int i = 0; i < zonas.size(); i++) {
            Zona zona = zonas.get(i);
            int visitasEnZona = conteoZonas.get(zona);

            if (visitasEnZona >= 5) {
                String descripcion = "Alta concentración de interés detectada en la zona " + zona.name()
                        + " con " + visitasEnZona + " visitas agendadas en los últimos 7 días.";

                alertaService.generarAlerta(
                        TipoAlerta.ZONA_CONCENTRACION_INTERES,
                        PrioridadAlerta.BAJA,
                        "Concentración de interés en zona",
                        descripcion,
                        zona.name()
                );

                registrarEventoSiNoExiste(
                        TipoAlerta.ZONA_CONCENTRACION_INTERES,
                        descripcion,
                        zona.name(),
                        NivelAtencion.BAJO
                );
            }
        }
    }

    /**
     * Guarda un evento de comportamiento atípico en el histórico, previniendo duplicados activos.
     */
    private void registrarEventoSiNoExiste(TipoAlerta tipo, String descripcion, String referenciaId, NivelAtencion nivel) {
        DynamicArrayList<RegistroComportamientoAtipico> existentes = registroRepository.findByReferenciaId(referenciaId);
        boolean existeActivo = false;
        for (int i = 0; i < existentes.size(); i++) {
            RegistroComportamientoAtipico r = existentes.get(i);
            if (r.getTipoComportamiento() == tipo && !r.isResuelto()) {
                existeActivo = true;
                break;
            }
        }

        if (!existeActivo) {
            RegistroComportamientoAtipico nuevo = RegistroComportamientoAtipico.builder()
                    .tipoComportamiento(tipo)
                    .descripcion(descripcion)
                    .referenciaId(referenciaId)
                    .fechaDeteccion(LocalDateTime.now())
                    .nivelAtencion(nivel)
                    .resuelto(false)
                    .build();
            registroRepository.save(nuevo);
        }
    }

    /**
     * Registra el historial de cambio de precio de un inmueble.
     */
    public void registrarCambioPrecio(String codigoInmueble, double precioAnterior, double precioNuevo) {
        HistorialPrecio hp = HistorialPrecio.builder()
                .codigoInmueble(codigoInmueble)
                .precioAnterior(precioAnterior)
                .precioNuevo(precioNuevo)
                .fechaCambio(LocalDateTime.now())
                .build();
        precioRepository.save(hp);
    }

    /**
     * Consulta todos los eventos registrados.
     */
    public DynamicArrayList<RegistroComportamientoAtipico> obtenerTodosRegistros() {
        return registroRepository.findAll();
    }

    /**
     * Filtra eventos por nivel de atención.
     */
    public DynamicArrayList<RegistroComportamientoAtipico> obtenerRegistrosPorNivel(NivelAtencion nivel) {
        return registroRepository.findByNivelAtencion(nivel);
    }

    /**
     * Filtra eventos por inmueble.
     */
    public DynamicArrayList<RegistroComportamientoAtipico> obtenerRegistrosPorInmueble(String codigoInmueble) {
        return registroRepository.findByReferenciaId(codigoInmueble);
    }

    /**
     * Resuelve un evento de comportamiento atípico registrado, marcándolo y agregando notas.
     */
    public Optional<RegistroComportamientoAtipico> resolverRegistro(Long id, String observaciones) {
        return registroRepository.findById(id)
                .map(registro -> {
                    registro.setResuelto(true);
                    registro.setObservaciones(observaciones);
                    return registroRepository.save(registro);
                });
    }
}
