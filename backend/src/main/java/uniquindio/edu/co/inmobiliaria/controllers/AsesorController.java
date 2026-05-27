package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.AsesorResponse;
import uniquindio.edu.co.inmobiliaria.services.AsesorService;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/asesores")
/**
 * Controlador REST que expone los servicios relacionados con los asesores de la inmobiliaria.
 * Permite listar, registrar, consultar, actualizar y eliminar asesores.
 */
public class AsesorController {

    private final AsesorService asesorService;

    public AsesorController(AsesorService asesorService) {
        this.asesorService = asesorService;
    }

    /**
     * Lista todos los asesores registrados en el sistema.
     *
     * @return lista de respuestas DTO con la información de cada asesor
     */
    @GetMapping
    public List<AsesorResponse> listarAsesores() {
        return asesorService.listarAsesores();
    }

    /**
     * Obtiene un asesor por su identificador.
     *
     * @param id identificador del asesor
     * @return DTO con los datos del asesor
     */
    @GetMapping("/{id}")
    public AsesorResponse obtenerAsesor(@PathVariable String id) {
        return asesorService.obtenerAsesor(id);
    }

    /**
     * Registra un nuevo asesor en el sistema.
     *
     * @param request DTO con los datos necesarios para crear el asesor
     * @return DTO con los datos del asesor recién creado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsesorResponse registrarAsesor(@RequestBody AsesorRequest request) {
        return asesorService.registrarAsesor(request);
    }

    /**
     * Actualiza los datos de un asesor existente.
     *
     * @param id identificador del asesor a actualizar
     * @param request DTO con los nuevos valores
     * @return DTO con los datos actualizados
     */
    @PutMapping("/{id}")
    public AsesorResponse actualizarAsesor(@PathVariable String id, @RequestBody AsesorRequest request) {
        return asesorService.actualizarAsesor(id, request);
    }

    /**
     * Elimina un asesor del sistema. No devuelve contenido en la respuesta.
     *
     * @param id identificador del asesor a eliminar
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAsesor(@PathVariable String id) {
        asesorService.eliminarAsesor(id);
    }
}