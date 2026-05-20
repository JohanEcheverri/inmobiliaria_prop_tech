package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaMonitor;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaService;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class AlertaController {

    private final AlertaService alertaService;
    private final AlertaMonitor alertaMonitor;

    public AlertaController(AlertaService alertaService, AlertaMonitor alertaMonitor) {
        this.alertaService = alertaService;
        this.alertaMonitor = alertaMonitor;
    }

    /**
     * Obtiene todas las alertas pendientes.
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Alerta>> obtenerAlertasPendientes() {
        List<Alerta> alertas = alertaService.obtenerAlertasPendientes();
        return ResponseEntity.ok(alertas);
    }

    /**
     * Obtiene el historial completo de alertas.
     */
    @GetMapping("/historial")
    public ResponseEntity<List<Alerta>> obtenerHistorial() {
        List<Alerta> alertas = alertaService.obtenerHistorial();
        return ResponseEntity.ok(alertas);
    }

    /**
     * Obtiene alertas por tipo.
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Alerta>> obtenerAlertasPorTipo(@PathVariable TipoAlerta tipo) {
        List<Alerta> alertas = alertaService.obtenerAlertasPorTipo(tipo);
        return ResponseEntity.ok(alertas);
    }

    /**
     * Obtiene alertas por prioridad.
     */
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<Alerta>> obtenerAlertasPorPrioridad(@PathVariable PrioridadAlerta prioridad) {
        List<Alerta> alertas = alertaService.obtenerAlertasPorPrioridad(prioridad);
        return ResponseEntity.ok(alertas);
    }

    /**
     * Obtiene alertas relacionadas con una entidad específica.
     */
    @GetMapping("/referencia/{referenciaId}")
    public ResponseEntity<List<Alerta>> obtenerAlertasPorReferencia(@PathVariable String referenciaId) {
        List<Alerta> alertas = alertaService.obtenerAlertasPorReferencia(referenciaId);
        return ResponseEntity.ok(alertas);
    }

    /**
     * Marca una alerta como atendida.
     */
    @PutMapping("/{codigo}/atender")
    public ResponseEntity<Alerta> atenderAlerta(@PathVariable String codigo) {
        Optional<Alerta> alerta = alertaService.atenderAlerta(codigo);
        return alerta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Obtiene una alerta específica.
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Alerta> obtenerAlerta(@PathVariable String codigo) {
        Optional<Alerta> alerta = alertaService.obtenerAlerta(codigo);
        return alerta.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Elimina una alerta.
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminarAlerta(@PathVariable String codigo) {
        alertaService.eliminarAlerta(codigo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene resumen de alertas pendientes.
     */
    @GetMapping("/resumen")
    public ResponseEntity<AlertaService.AlertaResumen> obtenerResumen() {
        AlertaService.AlertaResumen resumen = alertaService.obtenerResumen();
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene conteo de alertas pendientes.
     */
    @GetMapping("/contar/pendientes")
    public ResponseEntity<Long> contarAlertasPendientes() {
        long count = alertaService.contarAlertasPendientes();
        return ResponseEntity.ok(count);
    }

    /**
     * Ejecuta la verificación de todas las reglas de alertas.
     * Esta operación puede ser costosa en términos de rendimiento.
     */
    @PostMapping("/verificar")
    public ResponseEntity<String> verificarTodo() {
        try {
            alertaMonitor.verificarTodo();
            return ResponseEntity.ok("Verificación de alertas completada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante la verificación de alertas: " + e.getMessage());
        }
    }
}
