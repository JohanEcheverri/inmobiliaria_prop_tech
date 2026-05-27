package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaService;
import uniquindio.edu.co.inmobiliaria.models.entities.Arriendo;
import uniquindio.edu.co.inmobiliaria.models.entities.Cancelacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Renovacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.OperacionRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ContratoRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.OperacionJpaRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Servicio para la gestión de las operaciones comerciales de la inmobiliaria.
 * Orquesta la creación y consulta de transacciones (Venta, Arriendo, Renovación, Cancelación)
 * y vincula entidades como Cliente, Inmueble y Asesor durante los cierres de negocio.
 */
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final VisitasRepository visitasRepository;
    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final AsesorRepository asesorRepository;
    private final ContratoRepository contratoRepository;
    private final AlertaService alertaService;
    private final OperacionJpaRepository operacionJpaRepository;

    public OperacionService(OperacionRepository operacionRepository,
                            VisitasRepository visitasRepository,
                            ClienteRepository clienteRepository,
                            InmuebleRepository inmuebleRepository,
                            AsesorRepository asesorRepository,
                            ContratoRepository contratoRepository,
                            AlertaService alertaService,
                            OperacionJpaRepository operacionJpaRepository) {
        this.operacionRepository = operacionRepository;
        this.visitasRepository = visitasRepository;
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
        this.contratoRepository = contratoRepository;
        this.alertaService = alertaService;
        this.operacionJpaRepository = operacionJpaRepository;
    }

    /**
     * Registra una operación de arriendo en repositorio, validando estado por defecto.
     *
     * @param arriendo entidad Arriendo a persistir
     * @return la entidad persistida
     */
    public Arriendo registerRental(Arriendo arriendo) {
        if (arriendo == null) {
            throw new IllegalArgumentException("La operación de arriendo no puede ser nula");
        }
        if (arriendo.getEstado() == null) {
            arriendo.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(arriendo);
        return arriendo;
    }

    /**
     * Registra una operación de venta; establece estado por defecto si no está presente.
     *
     * @param venta entidad Venta a persistir
     * @return la entidad persistida
     */
    public Venta registerSale(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La operación de venta no puede ser nula");
        }
        if (venta.getEstado() == null) {
            venta.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(venta);
        return venta;
    }

    /**
     * Helper usado por controladores para registrar una venta completa a partir de ids y valores.
     * Valida la existencia de inmueble y cliente, determina asesor y persiste la venta en estado COMPLETADA.
     *
     * @param inmuebleCodigo código del inmueble vendido
     * @param clienteId id del cliente comprador
     * @param asesorId id del asesor (opcional, si es null se usa el asesor del inmueble)
     * @param valorAcordado valor pactado de la venta
     * @param comision comisión asociada
     */
    public void registrarVentaCompleta(String inmuebleCodigo, String clienteId, String asesorId, double valorAcordado, double comision) {
        if (inmuebleCodigo == null || inmuebleCodigo.isBlank()) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("El id del cliente comprador es obligatorio");
        }

        Inmueble inmueble = inmuebleRepository.findByCodigo(inmuebleCodigo);
        if (inmueble == null) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + inmuebleCodigo);
        }

        Cliente comprador = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));

        Asesor asesor = null;
        if (asesorId != null && !asesorId.isBlank()) {
            asesor = asesorRepository.findById(asesorId)
                    .orElse(null);
        }
        if (asesor == null) {
            asesor = inmueble.getAsesor();
        }

        Venta venta = Venta.builder()
                .codigo("VENTA-" + inmuebleCodigo + "-" + System.currentTimeMillis())
                .inmueble(inmueble)
                .cliente(comprador)
                .asesor(asesor)
                .fecha(LocalDateTime.now())
                .valorAcordado(valorAcordado)
                .comision(comision)
                .estado(EstadoOperacion.COMPLETADA)
                .build();

        operacionRepository.save(venta);
    }

    /**
     * Registra una renovación de arriendo y persiste la entidad.
     *
     * @param renovacion entidad Renovacion
     * @return la renovación persistida
     */
    public Renovacion registerRenewal(Renovacion renovacion) {
        if (renovacion == null) {
            throw new IllegalArgumentException("La renovación no puede ser nula");
        }
        if (renovacion.getEstado() == null) {
            renovacion.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(renovacion);
        return renovacion;
    }

    /**
     * Registra una cancelación de operación y la persiste en estado CANCELADA por defecto.
     *
     * @param cancelacion entidad Cancelacion
     * @return la cancelación persistida
     */
    public Cancelacion registerTermination(Cancelacion cancelacion) {
        if (cancelacion == null) {
            throw new IllegalArgumentException("La cancelación no puede ser nula");
        }
        if (cancelacion.getEstado() == null) {
            cancelacion.setEstado(EstadoOperacion.CANCELADA);
        }
        operacionRepository.save(cancelacion);
        return cancelacion;
    }

    /**
     * Consulta una operación por su código.
     *
     * @param codigo código de la operación
     * @return Optional con la operación si existe
     */
    public Optional<Operacion> consultarOperacion(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return operacionRepository.findByCodigo(codigo);
    }


    /**
     * Lista operaciones asociadas a un cliente dado.
     *
     * @param clienteId identificador del cliente
     * @return lista dinámica de operaciones del cliente
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Operacion> listarOperacionesCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            return new DynamicArrayList<>();
        }
        return operacionRepository.findByIdCliente(clienteId);
    }

    /**
     * Obtiene ventas y arriendos vigentes del cliente (excluye cancelados).
     *
     * @param clienteId identificador del cliente
     * @return operaciones de compra o arriendo del cliente
     */
    @Transactional(readOnly = true)
    public DynamicArrayList<Operacion> obtenerPropiedadesCliente(String clienteId) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (clienteId == null || clienteId.isBlank()) {
            return resultado;
        }
        agregarPropiedadesCliente(resultado, operacionRepository.findVentas(), clienteId);
        agregarPropiedadesCliente(resultado, operacionRepository.findArriendos(), clienteId);
        return resultado;
    }

    private void agregarPropiedadesCliente(DynamicArrayList<Operacion> resultado,
                                           DynamicArrayList<? extends Operacion> operaciones,
                                           String clienteId) {
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getCliente() == null || !clienteId.equals(operacion.getCliente().getId())) {
                continue;
            }
            if (operacion.getEstado() == EstadoOperacion.CANCELADA) {
                continue;
            }
            resultado.add(operacion);
        }
    }

    /**
     * El cliente solicita cancelar un contrato de arriendo; genera alerta para el asesor.
     */
    @Transactional
    public void solicitarCancelacionArriendo(String operacionCodigo, String clienteId, String motivo) {
        if (operacionCodigo == null || operacionCodigo.isBlank()) {
            throw new IllegalArgumentException("El código de la operación es obligatorio");
        }
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
        }

        operacionRepository.refreshFromDatabase(operacionCodigo);

        Operacion operacion = operacionJpaRepository.findByIdConRelaciones(operacionCodigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la operación: " + operacionCodigo));
        if (!(operacion instanceof Arriendo arriendo)) {
            throw new IllegalArgumentException("Solo se pueden cancelar contratos de arriendo");
        }
        if (arriendo.getCliente() == null || !clienteId.equals(arriendo.getCliente().getId())) {
            throw new IllegalArgumentException("La operación no pertenece al cliente indicado");
        }
        if (arriendo.getEstado() == EstadoOperacion.CANCELADA) {
            throw new IllegalArgumentException("El contrato ya fue cancelado");
        }

        String inmuebleCodigo = arriendo.getInmueble() != null ? arriendo.getInmueble().getCodigo() : "N/A";
        String clienteNombre = arriendo.getCliente().getNombre();
        String asesorNombre = arriendo.getAsesor() != null ? arriendo.getAsesor().getNombre() : "sin asignar";
        String asesorId = arriendo.getAsesor() != null ? arriendo.getAsesor().getId() : null;

        if (arriendo.getEstado() == EstadoOperacion.EN_PROCESO) {
            if (alertaService.obtenerAlertasPorReferencia(operacionCodigo).stream()
                    .anyMatch(a -> !a.isAtendida() && a.getTipo() == TipoAlerta.SOLICITUD_CANCELACION_CONTRATO)) {
                throw new IllegalArgumentException("Ya existe una solicitud de cancelación en trámite");
            }
            // Recuperación: quedó EN_PROCESO sin alerta por un fallo previo
            crearAlertaCancelacion(operacionCodigo, clienteNombre, inmuebleCodigo, asesorNombre, asesorId, motivo);
            operacionRepository.refreshFromDatabase(operacionCodigo);
            return;
        }

        // Primero la alerta; si falla, no se cambia el estado del arriendo
        crearAlertaCancelacion(operacionCodigo, clienteNombre, inmuebleCodigo, asesorNombre, asesorId, motivo);

        arriendo.setEstado(EstadoOperacion.EN_PROCESO);
        operacionRepository.update(arriendo);
        operacionRepository.refreshFromDatabase(operacionCodigo);
    }

    private void crearAlertaCancelacion(String operacionCodigo,
                                        String clienteNombre,
                                        String inmuebleCodigo,
                                        String asesorNombre,
                                        String asesorId,
                                        String motivo) {
        String descripcion = "El cliente " + clienteNombre + " solicita cancelar el arriendo del inmueble "
                + inmuebleCodigo + ". Motivo: " + motivo.trim()
                + ". Asesor responsable: " + asesorNombre
                + (asesorId != null ? " (id: " + asesorId + ")." : ".");
        alertaService.generarAlerta(
                TipoAlerta.SOLICITUD_CANCELACION_CONTRATO,
                PrioridadAlerta.ALTA,
                "Solicitud de cancelación de arriendo",
                descripcion,
                operacionCodigo
        );
    }

    /**
     * Solicitudes de cancelación pendientes asignadas a un asesor.
     */
    @Transactional(readOnly = true)
    public List<uniquindio.edu.co.inmobiliaria.models.entities.Alerta> obtenerSolicitudesCancelacionPorAsesor(String asesorId) {
        if (asesorId == null || asesorId.isBlank()) {
            return List.of();
        }
        List<uniquindio.edu.co.inmobiliaria.models.entities.Alerta> pendientes = new ArrayList<>();
        for (uniquindio.edu.co.inmobiliaria.models.entities.Alerta alerta
                : alertaService.obtenerAlertasPorTipo(TipoAlerta.SOLICITUD_CANCELACION_CONTRATO)) {
            if (alerta.isAtendida()) {
                continue;
            }
            Optional<Operacion> operacion = operacionJpaRepository.findByIdConRelaciones(alerta.getReferenciaId());
            if (operacion.isPresent()
                    && operacion.get().getAsesor() != null
                    && asesorId.equals(operacion.get().getAsesor().getId())) {
                pendientes.add(alerta);
            }
        }
        return pendientes;
    }

    /**
     * El asesor procesa la cancelación del arriendo solicitada por el cliente.
     */
    @Transactional
    public void procesarCancelacionArriendo(String operacionCodigo, String asesorId) {
        Operacion operacion = operacionRepository.findByCodigo(operacionCodigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la operación: " + operacionCodigo));
        if (!(operacion instanceof Arriendo arriendo)) {
            throw new IllegalArgumentException("La operación no es un arriendo");
        }
        if (arriendo.getAsesor() != null && asesorId != null && !asesorId.isBlank()
                && !asesorId.equals(arriendo.getAsesor().getId())) {
            throw new IllegalArgumentException("El asesor no está autorizado para gestionar esta cancelación");
        }
        if (arriendo.getEstado() == EstadoOperacion.CANCELADA) {
            throw new IllegalArgumentException("El contrato ya fue cancelado");
        }

        arriendo.setEstado(EstadoOperacion.CANCELADA);
        operacionRepository.update(arriendo);

        if (arriendo.getContrato() != null) {
            Contrato contrato = arriendo.getContrato();
            contrato.setVigente(false);
            contratoRepository.update(contrato);
        }

        if (arriendo.getInmueble() != null) {
            Inmueble inmueble = arriendo.getInmueble();
            inmueble.setEstado(Estado.DISPONIBLE);
            inmuebleRepository.update(inmueble);
        }

        List<uniquindio.edu.co.inmobiliaria.models.entities.Alerta> alertas =
                alertaService.obtenerAlertasPorReferencia(operacionCodigo);
        for (uniquindio.edu.co.inmobiliaria.models.entities.Alerta alerta : alertas) {
            if (!alerta.isAtendida() && alerta.getTipo() == TipoAlerta.SOLICITUD_CANCELACION_CONTRATO) {
                alertaService.atenderAlerta(alerta.getCodigo());
            }
        }
    }

    /**
     * Filtra operaciones cuyo inmueble pertenezca a la zona indicada.
     *
     * @param zona zona a filtrar
     * @return lista dinámica de operaciones en la zona
     */
    public DynamicArrayList<Operacion> consultarOperacionesPorZona(Zona zona) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getInmueble() != null
                    && operacion.getInmueble().getBarrio() != null
                    && zona.equals(operacion.getInmueble().getBarrio().getZona())) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    /**
     * Devuelve operaciones cuyo precio (del inmueble o valor acordado) esté dentro del rango.
     *
     * @param min precio mínimo
     * @param max precio máximo
     * @return lista dinámica de operaciones en el rango de precio
     */
    public DynamicArrayList<Operacion> consultarOperacionesPorPrecio(double min, double max) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (min > max) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            double precio = operacion.getInmueble() != null ? operacion.getInmueble().getPrecio() : operacion.getValorAcordado();
            if (precio >= min && precio <= max) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    /**
     * Filtra visitas según la zona del inmueble asociado.
     *
     * @param zona zona a filtrar
     * @return lista dinámica de Visita en la zona
     */
    public DynamicArrayList<Visita> consultarVisitasPorZona(Zona zona) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Visita> visitas = visitasRepository.findAll();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getInmueble() != null
                    && visita.getInmueble().getBarrio() != null
                    && zona.equals(visita.getInmueble().getBarrio().getZona())) {
                resultado.add(visita);
            }
        }
        return resultado;
    }

    /**
     * Recupera operaciones cuyo estado sea COMPLETADA.
     *
     * @return lista dinámica de operaciones cerradas
     */
    public DynamicArrayList<Operacion> consultarOperacionesCerradas() {
        return operacionRepository.findByEstado(EstadoOperacion.COMPLETADA);
    }

    /**
     * Filtra operaciones cerradas dentro de una zona específica.
     *
     * @param zona zona a filtrar
     * @return lista dinámica de operaciones cerradas en la zona
     */
    public DynamicArrayList<Operacion> consultarOperacionesCerradasPorZona(Zona zona) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = consultarOperacionesPorZona(zona);
        for (int i = 0; i < operaciones.size(); i++) {
            if (operaciones.get(i).getEstado() == EstadoOperacion.COMPLETADA) {
                resultado.add(operaciones.get(i));
            }
        }
        return resultado;
    }
}
