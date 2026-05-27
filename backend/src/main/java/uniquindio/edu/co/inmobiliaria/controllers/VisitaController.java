package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORTANTE: Asegúrate de importar esta
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import uniquindio.edu.co.inmobiliaria.models.dto.ObservacionRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.VisitaRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.VisitaResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.services.VisitaService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/visitas")
@Transactional(readOnly = true)
/**
 * Controlador REST encargado de gestionar el ciclo de vida de las visitas a inmuebles.
 * Contiene operaciones para agendar, confirmar, realizar, cancelar o reprogramar visitas,
 * además de métodos de consulta específicos para clientes y asesores.
 */
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    /**
     * Lista todas las visitas registradas en el sistema.
     *
     * @return lista de VisitaResponse
     */
    @GetMapping
    public List<VisitaResponse> listarVisitas() {
        return mapearLista(visitaService.listVisits());
    }

    /**
     * Lista las visitas de un cliente específico.
     *
     * @param clienteId id del cliente
     * @return lista de VisitaResponse pertenecientes al cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public List<VisitaResponse> listarPorCliente(@PathVariable String clienteId) {
        return mapearLista(visitaService.listVisitsByClient(clienteId));
    }

    /**
     * Lista las visitas asignadas a un asesor.
     *
     * @param asesorId id del asesor
     * @return lista de VisitaResponse asignadas al asesor
     */
    @GetMapping("/asesor/{asesorId}")
    public List<VisitaResponse> listarPorAsesor(@PathVariable String asesorId) {
        return mapearLista(visitaService.listVisitsByAdvisor(asesorId));
    }

    /**
     * Agenda una nueva visita para un cliente a un inmueble. Operación transaccional.
     *
     * @param request DTO con los datos necesarios para agendar la visita
     * @return VisitaResponse con los datos de la visita creada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public VisitaResponse agendarVisita(@RequestBody VisitaRequest request) {
        Visita visita = visitaService.scheduleVisit(
                request.clienteId(),
                request.inmuebleCodigo(),
                request.asesorId(),
                request.fecha(),
                request.hora(),
                request.observaciones()
        );
        return mapear(visita);
    }

    /**
     * Confirma una visita previamente agendada, opcionalmente guardando observaciones.
     *
     * @param codigo código de la visita a confirmar
     * @param request petición con observaciones (opcional)
     * @return VisitaResponse con el estado actualizado
     */
    @PutMapping("/{codigo}/confirmar")
    @Transactional
    public VisitaResponse confirmar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.confirmVisit(codigo, observaciones(request)));
    }

    /**
     * Marca una visita como realizada y registra observaciones si se proporcionan.
     *
     * @param codigo código de la visita
     * @param request observaciones opcionales
     * @return VisitaResponse actualizado
     */
    @PutMapping("/{codigo}/realizar")
    @Transactional
    public VisitaResponse realizar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.completeVisit(codigo, observaciones(request)));
    }

    /**
     * Cancela una visita existente y opcionalmente guarda observaciones sobre la cancelación.
     *
     * @param codigo código de la visita a cancelar
     * @param request observaciones opcionales
     * @return VisitaResponse con el estado de cancelada
     */
    @PutMapping("/{codigo}/cancelar")
    @Transactional
    public VisitaResponse cancelar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.cancelVisit(codigo, observaciones(request)));
    }

    /**
     * Reprograma una visita existente a una nueva fecha/hora y actualiza observaciones.
     *
     * @param codigo código de la visita a reprogramar
     * @param request DTO con nueva fecha, hora y observaciones
     * @return VisitaResponse con los datos reprogramados
     */
    @PutMapping("/{codigo}/reprogramar")
    @Transactional
    public VisitaResponse reprogramar(@PathVariable String codigo, @RequestBody VisitaRequest request) {
        return mapear(visitaService.rescheduleVisit(codigo, request.fecha(), request.hora(), request.observaciones()));
    }

    /**
     * Convierte una DynamicArrayList de Visita en una lista de VisitaResponse.
     * Maneja null devolviendo una lista vacía.
     *
     * @param visitas lista dinámica de visitas
     * @return lista mapeada de VisitaResponse
     */
    private List<VisitaResponse> mapearLista(DynamicArrayList<Visita> visitas) {
        List<VisitaResponse> respuesta = new ArrayList<>();
        if (visitas == null) return respuesta;

        for (int i = 0; i < visitas.size(); i++) {
            respuesta.add(mapear(visitas.get(i)));
        }
        return respuesta;
    }

    /**
     * Mapea una entidad Visita a su DTO VisitaResponse. Maneja campos nulos para
     * evitar NullPointerException al acceder a asociaciones lazy.
     *
     * @param visita entidad Visita
     * @return VisitaResponse o null si la entrada es null
     */
    private VisitaResponse mapear(Visita visita) {
        if (visita == null) return null;

        Cliente cliente = visita.getCliente();
        Inmueble inmueble = visita.getInmueble();

        Asesor asesor = visita.getAsesotAsignado();

        return new VisitaResponse(
                visita.getCodigo(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() : null,
                inmueble != null ? inmueble.getCodigo() : null,
                inmueble != null ? inmueble.getDireccion() : null,
                asesor != null ? asesor.getId() : null,
                asesor != null ? asesor.getNombre() : null,
                visita.getFecha(),
                visita.getHora(),
                visita.getEstado(),
                visita.getObservaciones()
        );
    }

    /**
     * Extrae las observaciones de una petición opcional, devolviendo null si la petición es null.
     *
     * @param request petición que contiene observaciones
     * @return texto de observaciones o null
     */
    private String observaciones(ObservacionRequest request) {
        return request != null ? request.observaciones() : null;
    }
}