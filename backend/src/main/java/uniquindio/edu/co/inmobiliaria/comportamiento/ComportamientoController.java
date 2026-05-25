package uniquindio.edu.co.inmobiliaria.comportamiento;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;
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
    public ResponseEntity<List<RegistroComportamientoAtipico>> obtenerTodos() {
        DynamicArrayList<RegistroComportamientoAtipico> registros = comportamientoService.obtenerTodosRegistros();
        return ResponseEntity.ok(convertirALista(registros));
    }

    /**
     * Filtra los comportamientos atípicos por su nivel de atención.
     */
    @GetMapping("/registro/nivel/{nivel}")
    public ResponseEntity<List<RegistroComportamientoAtipico>> obtenerPorNivel(@PathVariable NivelAtencion nivel) {
        DynamicArrayList<RegistroComportamientoAtipico> registros = comportamientoService.obtenerRegistrosPorNivel(nivel);
        return ResponseEntity.ok(convertirALista(registros));
    }

    /**
     * Consulta los eventos de comportamiento atípico asociados a un inmueble en específico.
     */
    @GetMapping("/registro/inmueble/{codigo}")
    public ResponseEntity<List<RegistroComportamientoAtipico>> obtenerPorInmueble(@PathVariable String codigo) {
        DynamicArrayList<RegistroComportamientoAtipico> registros = comportamientoService.obtenerRegistrosPorInmueble(codigo);
        return ResponseEntity.ok(convertirALista(registros));
    }

    private List<RegistroComportamientoAtipico> convertirALista(DynamicArrayList<RegistroComportamientoAtipico> registros) {
        List<RegistroComportamientoAtipico> lista = new ArrayList<>();
        for (int i = 0; i < registros.size(); i++) {
            lista.add(registros.get(i));
        }
        return lista;
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
