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
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @GetMapping
    public List<VisitaResponse> listarVisitas() {
        return mapearLista(visitaService.listVisits());
    }

    @GetMapping("/cliente/{clienteId}")
    public List<VisitaResponse> listarPorCliente(@PathVariable String clienteId) {
        return mapearLista(visitaService.listVisitsByClient(clienteId));
    }

    @GetMapping("/asesor/{asesorId}")
    public List<VisitaResponse> listarPorAsesor(@PathVariable String asesorId) {
        return mapearLista(visitaService.listVisitsByAdvisor(asesorId));
    }

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

    @PutMapping("/{codigo}/confirmar")
    @Transactional
    public VisitaResponse confirmar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.confirmVisit(codigo, observaciones(request)));
    }

    @PutMapping("/{codigo}/realizar")
    @Transactional
    public VisitaResponse realizar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.completeVisit(codigo, observaciones(request)));
    }

    @PutMapping("/{codigo}/cancelar")
    @Transactional
    public VisitaResponse cancelar(@PathVariable String codigo, @RequestBody(required = false) ObservacionRequest request) {
        return mapear(visitaService.cancelVisit(codigo, observaciones(request)));
    }

    @PutMapping("/{codigo}/reprogramar")
    @Transactional
    public VisitaResponse reprogramar(@PathVariable String codigo, @RequestBody VisitaRequest request) {
        return mapear(visitaService.rescheduleVisit(codigo, request.fecha(), request.hora(), request.observaciones()));
    }

    private List<VisitaResponse> mapearLista(DynamicArrayList<Visita> visitas) {
        List<VisitaResponse> respuesta = new ArrayList<>();
        if (visitas == null) return respuesta;

        for (int i = 0; i < visitas.size(); i++) {
            respuesta.add(mapear(visitas.get(i)));
        }
        return respuesta;
    }

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

    private String observaciones(ObservacionRequest request) {
        return request != null ? request.observaciones() : null;
    }
}