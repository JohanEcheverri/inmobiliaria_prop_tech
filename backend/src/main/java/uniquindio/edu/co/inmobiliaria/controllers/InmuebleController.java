package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.EstadoInmuebleRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleResponse;
import uniquindio.edu.co.inmobiliaria.services.InmuebleService;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/inmuebles")
public class InmuebleController {

    private final InmuebleService inmuebleService;

    public InmuebleController(InmuebleService inmuebleService) {
        this.inmuebleService = inmuebleService;
    }

    @GetMapping
    public List<InmuebleResponse> listarInmuebles() {
        return inmuebleService.listarInmuebles();
    }

    @GetMapping("/search")
    public List<InmuebleResponse> buscarInmuebles(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String zona,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String tipo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Double minPrecio,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Double maxPrecio,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer minHabitaciones,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer maxHabitaciones,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String finalidad,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String clienteId
    ) {
        return inmuebleService.buscarPorPreferencias(zona, tipo, minPrecio, maxPrecio, minHabitaciones, maxHabitaciones, finalidad, null, clienteId);
    }

    @GetMapping("/{codigo}")
    public InmuebleResponse obtenerInmueble(@PathVariable String codigo) {
        return inmuebleService.obtenerInmueble(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InmuebleResponse registrarInmueble(@RequestBody InmuebleRequest request) {
        return inmuebleService.registrarInmueble(request);
    }

    @PutMapping("/{codigo}")
    public InmuebleResponse actualizarInmueble(@PathVariable String codigo, @RequestBody InmuebleRequest request) {
        return inmuebleService.actualizarInmueble(codigo, request);
    }

    @PutMapping("/{codigo}/estado")
    public InmuebleResponse actualizarEstado(@PathVariable String codigo, @RequestBody EstadoInmuebleRequest request) {
        return inmuebleService.actualizarEstadoInmueble(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarInmueble(@PathVariable String codigo) {
        inmuebleService.eliminarInmueble(codigo);
    }
}
