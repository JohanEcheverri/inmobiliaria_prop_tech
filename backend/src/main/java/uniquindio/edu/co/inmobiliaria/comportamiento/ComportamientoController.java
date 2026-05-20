package uniquindio.edu.co.inmobiliaria.comportamiento;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import java.util.Optional;

/**
 * Controlador REST para exponer la funcionalidad del módulo de seguimiento y análisis de comportamiento atípico.
 */
@RestController
@RequestMapping("/api/comportamiento")
@CrossOrigin(origins = "*")
public class ComportamientoController {

    private final ComportamientoService comportamientoService;

    public ComportamientoController(ComportamientoService comportamientoService) {
        this.comportamientoService = comportamientoService;
    }

    /**
     * Ejecuta manualmente el análisis y detección de comportamientos atípicos en la inmobiliaria.
     */
    @PostMapping("/analizar")
    public ResponseEntity<String> analizar() {
        try {
            comportamientoService.analizarComportamientoAtipico();
            return ResponseEntity.ok("Análisis de comportamientos atípicos completado y alertas generadas.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al ejecutar el análisis: " + e.getMessage());
        }
    }

    /**
     * Consulta el historial completo de comportamientos atípicos detectados.
     */
    @GetMapping("/registro")
    public ResponseEntity<DynamicArrayList<RegistroComportamientoAtipico>> obtenerTodos() {
        return ResponseEntity.ok(comportamientoService.obtenerTodosRegistros());
    }

    /**
     * Filtra los comportamientos atípicos por su nivel de atención.
     */
    @GetMapping("/registro/nivel/{nivel}")
    public ResponseEntity<DynamicArrayList<RegistroComportamientoAtipico>> obtenerPorNivel(@PathVariable NivelAtencion nivel) {
        return ResponseEntity.ok(comportamientoService.obtenerRegistrosPorNivel(nivel));
    }

    /**
     * Consulta los eventos de comportamiento atípico asociados a un inmueble en específico.
     */
    @GetMapping("/registro/inmueble/{codigo}")
    public ResponseEntity<DynamicArrayList<RegistroComportamientoAtipico>> obtenerPorInmueble(@PathVariable String codigo) {
        return ResponseEntity.ok(comportamientoService.obtenerRegistrosPorInmueble(codigo));
    }

    /**
     * Resuelve un comportamiento atípico registrado, marcándolo como atendido y añadiendo observaciones.
     */
    @PutMapping("/registro/{id}/resolver")
    public ResponseEntity<RegistroComportamientoAtipico> resolver(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String observaciones) {
        Optional<RegistroComportamientoAtipico> registro = comportamientoService.resolverRegistro(id, observaciones);
        return registro.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
