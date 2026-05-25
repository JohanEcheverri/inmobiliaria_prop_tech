package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.EventoHistorialRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.EventoHistorialResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.services.EventoHistorialService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/historial")
public class EventoHistorialController {

    private final EventoHistorialService eventoHistorialService;

    public EventoHistorialController(EventoHistorialService eventoHistorialService) {
        this.eventoHistorialService = eventoHistorialService;
    }

    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public List<EventoHistorialResponse> historialCliente(@PathVariable String clienteId) {
        return mapearLista(eventoHistorialService.consultarHistorialCliente(clienteId));
    }

    @GetMapping("/cliente/{clienteId}/favoritos")
    @Transactional(readOnly = true)
    public List<EventoHistorialResponse> favoritosCliente(@PathVariable String clienteId) {
        return mapearLista(eventoHistorialService.consultarFavoritosCliente(clienteId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public EventoHistorialResponse registrarEvento(@RequestBody EventoHistorialRequest request) {
        try {
            TipoEventoHistorial tipo = TipoEventoHistorial.valueOf(request.tipoEvento().toUpperCase());
            return mapear(eventoHistorialService.registerEvent(
                    request.clienteId(),
                    request.inmuebleCodigo(),
                    tipo
            ));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de evento no válido: " + request.tipoEvento(), e);
        }
    }

    private List<EventoHistorialResponse> mapearLista(DynamicArrayList<EventoHistorial> eventos) {
        List<EventoHistorialResponse> respuesta = new ArrayList<>();
        for (int i = 0; i < eventos.size(); i++) {
            respuesta.add(mapear(eventos.get(i)));
        }
        return respuesta;
    }

    private EventoHistorialResponse mapear(EventoHistorial evento) {
        Cliente cliente = evento.getCliente();
        Inmueble inmueble = evento.getInmueble();

        return new EventoHistorialResponse(
                evento.getId(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() : null,
                inmueble != null ? inmueble.getCodigo() : null,
                inmueble != null ? inmueble.getDireccion() : null,
                evento.getTipoEvento(),
                evento.getFechaEvento()
        );
    }
}
