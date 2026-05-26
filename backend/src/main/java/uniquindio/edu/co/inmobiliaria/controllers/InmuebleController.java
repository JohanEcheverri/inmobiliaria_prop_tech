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

    /**
     * Devuelve todos los inmuebles registrados.
     *
     * @return lista de InmuebleResponse con los inmuebles existentes
     */
    @GetMapping
    public List<InmuebleResponse> listarInmuebles() {
        return inmuebleService.listarInmuebles();
    }

    /**
     * Busca inmuebles por parámetros opcionales de filtrado (zona, tipo, rango de precio,
     * número de habitaciones, finalidad y cliente para personalización).
     *
     * @param zona zona geográfica (opcional)
     * @param tipo tipo de inmueble (opcional)
     * @param minPrecio precio mínimo (opcional)
     * @param maxPrecio precio máximo (opcional)
     * @param minHabitaciones mínimo de habitaciones (opcional)
     * @param maxHabitaciones máximo de habitaciones (opcional)
     * @param finalidad finalidad (venta/arrendamiento) (opcional)
     * @param clienteId id del cliente que busca (opcional)
     * @return lista de InmuebleResponse que cumplen los filtros
     */
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

    /**
     * Obtiene un inmueble por su código único.
     *
     * @param codigo código del inmueble
     * @return InmuebleResponse con los datos del inmueble
     */
    @GetMapping("/{codigo}")
    public InmuebleResponse obtenerInmueble(@PathVariable String codigo) {
        return inmuebleService.obtenerInmueble(codigo);
    }

    /**
     * Registra un nuevo inmueble en el sistema.
     *
     * @param request DTO con los datos del inmueble
     * @return DTO con los datos guardados del inmueble
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InmuebleResponse registrarInmueble(@RequestBody InmuebleRequest request) {
        return inmuebleService.registrarInmueble(request);
    }

    /**
     * Actualiza un inmueble existente identificado por su código.
     *
     * @param codigo código del inmueble a actualizar
     * @param request DTO con los nuevos valores
     * @return InmuebleResponse con los datos actualizados
     */
    @PutMapping("/{codigo}")
    public InmuebleResponse actualizarInmueble(@PathVariable String codigo, @RequestBody InmuebleRequest request) {
        return inmuebleService.actualizarInmueble(codigo, request);
    }

    /**
     * Actualiza únicamente el estado comercial de un inmueble (p. ej. disponible, vendido).
     *
     * @param codigo código del inmueble
     * @param request DTO con el nuevo estado
     * @return InmuebleResponse con el estado actualizado
     */
    @PutMapping("/{codigo}/estado")
    public InmuebleResponse actualizarEstado(@PathVariable String codigo, @RequestBody EstadoInmuebleRequest request) {
        return inmuebleService.actualizarEstadoInmueble(codigo, request);
    }

    /**
     * Elimina un inmueble por su código. Responde con 204 No Content cuando se elimina correctamente.
     *
     * @param codigo código del inmueble a eliminar
     */
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarInmueble(@PathVariable String codigo) {
        inmuebleService.eliminarInmueble(codigo);
    }
}
