package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.services.AdministradorService;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/administradores")
/**
 * Controlador REST dedicado a la gestión de perfiles de administradores.
 * Proporciona endpoints para obtener y actualizar la información de cuenta del administrador.
 */
public class AdminController {

    private final AdministradorService administradorService;

    public AdminController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    /**
     * Obtiene el perfil de un administrador por su identificador.
     *
     * @param id identificador del administrador
     * @return mapa con los datos del perfil
     */
    @GetMapping("/{id}")
    public Map<String, Object> obtenerAdmin(@PathVariable String id) {
        return administradorService.obtenerAdmin(id);
    }

    /**
     * Actualiza los datos de perfil de un administrador existente.
     *
     * @param id identificador del administrador
     * @param datos mapa con los campos a actualizar
     * @return mapa con los datos actualizados
     */
    @PutMapping("/{id}")
    public Map<String, Object> actualizarAdmin(@PathVariable String id, @RequestBody Map<String, String> datos) {
        return administradorService.actualizarAdmin(id, datos);
    }
}
