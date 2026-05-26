package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaMonitor;
import uniquindio.edu.co.inmobiliaria.comportamiento.ComportamientoService;
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
    private final ComportamientoService comportamientoService;
    private final AlertaMonitor alertaMonitor;

    public VisitaService(VisitasRepository visitasRepository,
                         ClienteRepository clienteRepository,
                         InmuebleRepository inmuebleRepository,
                         AsesorRepository asesorRepository,
                         EventoHistorialService eventoHistorialService,
                         ComportamientoService comportamientoService,
                         AlertaMonitor alertaMonitor) {
        this.visitasRepository = visitasRepository;
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
        this.eventoHistorialService = eventoHistorialService;
        this.comportamientoService = comportamientoService;
        this.alertaMonitor = alertaMonitor;
    }

    /**
     * Agenda una nueva visita validando cliente, inmueble, asesor, fecha/hora y disponibilidad.
     * Persiste la visita en estado PENDIENTE, registra un evento de historial y ejecuta monitores.
     *
     * @param clienteId id del cliente solicitante
     * @param inmuebleCodigo código del inmueble a visitar
     * @param asesorId id del asesor responsable (opcional)
     * @param fecha fecha de la visita
     * @param hora hora de la visita
     * @param observaciones texto opcional de observaciones
     * @return entidad Visita creada
     */
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
        ejecutarMonitoresComerciales();
        return visita;
    }

    /**
     * Reprograma una visita existente validando que sea modificable y que no exista
     * conflicto de disponibilidad. Actualiza estado a REPROGRAMADA y guarda observaciones.
     *
     * @param codigoVisita código de la visita a reprogramar
     * @param nuevaFecha nueva fecha solicitada
     * @param nuevaHora nueva hora solicitada
     * @param observaciones observaciones adicionales (opcional)
     * @return entidad Visita reprogramada
     */
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
        ejecutarMonitoresComerciales();
        return reprogramada;
    }

    /**
     * Cancela una visita válida (no realizada). Mantiene el registro anterior, cambia
     * estado a CANCELADA y concatena observaciones.
     *
     * @param codigoVisita código de la visita a cancelar
     * @param observaciones motivo u observaciones de la cancelación (opcional)
     * @return la entidad Visita actualizada en estado CANCELADA
     */
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
        ejecutarMonitoresComerciales();
        return cancelada;
    }

    /**
     * Confirma una visita válida, cambiando su estado a CONFIRMADA y concatenando observaciones.
     * No permite confirmar visitas canceladas; si ya fue realizada, retorna la misma.
     *
     * @param codigoVisita código de la visita a confirmar
     * @param observaciones observaciones adicionales (opcional)
     * @return la visita en estado CONFIRMADA
     */
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
        ejecutarMonitoresComerciales();
        return confirmada;
    }

    /**
     * Marca una visita como realizada (REALIZADA). Valida que no esté cancelada y
     * registra un evento de historial si aplica. Retorna la visita actualizada.
     *
     * @param codigoVisita código de la visita a completar
     * @param observaciones notas o evidencia relacionadas con la visita (opcional)
     * @return la visita en estado REALIZADA
     */
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
        ejecutarMonitoresComerciales();
        return realizada;
    }

    /**
     * Busca una visita por su código y devuelve un Optional.
     *
     * @param codigoVisita código a buscar
     * @return Optional<Visita> que contiene la visita si existe
     */
    public Optional<Visita> findVisitByCode(String codigoVisita) {
        return visitasRepository.findByCodigo(codigoVisita);
    }

    /**
     * Retorna todas las visitas persistidas.
     *
     * @return DynamicArrayList de Visita
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Visita> listVisits() {
        return visitasRepository.findAll();
    }

    /**
     * Lista las visitas asociadas a un cliente específico.
     *
     * @param clienteId id del cliente
     * @return DynamicArrayList de Visita
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Visita> listVisitsByClient(String clienteId) {
        return visitasRepository.findByClienteId(clienteId);
    }

    /**
     * Lista las visitas asociadas a un inmueble específico.
     *
     * @param inmuebleCodigo código del inmueble
     * @return DynamicArrayList de Visita
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Visita> listVisitsByProperty(String inmuebleCodigo) {
        return visitasRepository.findByInmuebleCodigo(inmuebleCodigo);
    }

    /**
     * Lista las visitas asignadas a un asesor.
     *
     * @param asesorId id del asesor
     * @return DynamicArrayList de Visita
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Visita> listVisitsByAdvisor(String asesorId) {
        return visitasRepository.findByAsesorId(asesorId);
    }

    /**
     * Lista las visitas filtradas por estado.
     *
     * @param estado estado de visita (PENDIENTE, CONFIRMADA, REALIZADA, etc.)
     * @return DynamicArrayList de Visita
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Visita> listVisitsByStatus(EstadoVisita estado) {
        return visitasRepository.findByEstado(estado);
    }

    /**
     * Procesa la siguiente visita pendiente según la lógica del repositorio. Después
     * de procesar ejecuta monitores comerciales para detectar comportamientos y alertas.
     *
     * @return Visita procesada o null si no hay visitas pendientes
     */
    public Visita processPendingVisit() {
        Visita visita = visitasRepository.procesarVisita();
        ejecutarMonitoresComerciales();
        return visita;
    }

    /**
     * Ejecuta componentes que analizan comportamiento atípico y verifican alertas.
     * Se invoca tras operaciones que mutan el estado de visitas para mantener
     * monitoreo en tiempo cercano al cambio.
     */
    private void ejecutarMonitoresComerciales() {
        comportamientoService.analizarComportamientoAtipico();
        alertaMonitor.verificarTodo();
    }

    /**
     * Recupera un cliente por id o lanza IllegalArgumentException si no existe.
     *
     * @param clienteId id del cliente
     * @return Cliente
     */
    private Cliente obtenerCliente(String clienteId) {
        if (estaVacio(clienteId)) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));
    }

    /**
     * Recupera un inmueble por su código o lanza IllegalArgumentException si no existe.
     *
     * @param inmuebleCodigo código del inmueble
     * @return Inmueble
     */
    private Inmueble obtenerInmueble(String inmuebleCodigo) {
        if (estaVacio(inmuebleCodigo)) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        return inmuebleRepository.findById(inmuebleCodigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + inmuebleCodigo));
    }

    /**
     * Recupera un asesor por id o lanza IllegalArgumentException si no existe.
     *
     * @param asesorId id del asesor
     * @return Asesor
     */
    private Asesor obtenerAsesor(String asesorId) {
        if (estaVacio(asesorId)) {
            throw new IllegalArgumentException("El id del asesor es obligatorio");
        }
        return asesorRepository.findById(asesorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un asesor con el id: " + asesorId));
    }

    /**
     * Recupera una visita por código o lanza IllegalArgumentException si no existe.
     *
     * @param codigoVisita código de la visita
     * @return Visita
     */
    private Visita obtenerVisita(String codigoVisita) {
        if (estaVacio(codigoVisita)) {
            throw new IllegalArgumentException("El código de la visita es obligatorio");
        }
        return visitasRepository.findByCodigo(codigoVisita)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una visita con el código: " + codigoVisita));
    }

    /**
     * Valida que la fecha y hora de la visita sean no nulas y no estén en el pasado.
     *
     * @param fecha fecha de la visita
     * @param hora hora de la visita
     */
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

    /**
     * Valida que una visita pueda ser modificada (no cancelada ni ya realizada).
     *
     * @param visita visita a validar
     */
    private void validarVisitaModificable(Visita visita) {
        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new IllegalArgumentException("No se puede reprogramar una visita cancelada");
        }
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new IllegalArgumentException("No se puede reprogramar una visita ya realizada");
        }
    }

    /**
     * Valida que no existan conflictos de disponibilidad para la combinación
     * cliente-inmueble-asesor en la fecha y hora proporcionadas.
     *
     * @param codigoActual código de la visita actual (para excluirla en reprogramaciones)
     * @param clienteId id del cliente
     * @param inmuebleCodigo código del inmueble
     * @param asesorId id del asesor
     * @param fecha fecha de la visita
     * @param hora hora de la visita
     */
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

    /**
     * Indica si la visita pasada corresponde a la visita actual (por código).
     *
     * @param codigoActual código de la visita actual
     * @param visita visita a comparar
     * @return true si coinciden
     */
    private boolean esMismaVisita(String codigoActual, Visita visita) {
        return codigoActual != null && visita != null && codigoActual.equals(visita.getCodigo());
    }

    /**
     * Determina si una visita se considera activa para propósitos de conflicto
     * (pendiente, confirmada o reprogramada).
     *
     * @param visita visita a evaluar
     * @return true si la visita está activa
     */
    private boolean esVisitaActiva(Visita visita) {
        return visita.getEstado() == EstadoVisita.PENDIENTE
                || visita.getEstado() == EstadoVisita.CONFIRMADA
                || visita.getEstado() == EstadoVisita.REPROGRAMADA;
    }

    /**
     * Crea una copia superficial de la visita para modificar y persistir sin alterar
     * la instancia original en memoria.
     *
     * @param visita visita original
     * @return nueva instancia Visita con los mismos valores
     */
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

    /**
     * Genera un código único para la visita con prefijo VIS- y asegura su
     * unicidad consultando el repositorio.
     *
     * @return código único
     */
    private String generarCodigo() {
        String codigo;
        do {
            codigo = "VIS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (visitasRepository.existsByCodigo(codigo));
        return codigo;
    }

    /**
     * Combina observaciones anteriores con nuevas, normalizando y separando con '|'.
     * Si las nuevas observaciones estan vacías retorna las anteriores.
     *
     * @param anteriores observaciones anteriores
     * @param nuevas nuevas observaciones
     * @return cadena combinada o la existente si no hay nuevas
     */
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

    /**
     * Normaliza observaciones: si están vacías retorna null, si no retorna el trim.
     *
     * @param observaciones texto a normalizar
     * @return texto normalizado o null
     */
    private String normalizarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }
        return observaciones.trim();
    }

    /**
     * Utilidad para verificar si un string está vacío o es null.
     *
     * @param valor cadena a evaluar
     * @return true si es null o está en blanco
     */
    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
