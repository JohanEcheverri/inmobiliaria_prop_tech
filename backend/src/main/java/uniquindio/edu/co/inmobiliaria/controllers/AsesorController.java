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
public class AsesorController {

    private final AsesorService asesorService;

    public AsesorController(AsesorService asesorService) {
        this.asesorService = asesorService;
    }

    @GetMapping
    public List<AsesorResponse> listarAsesores() {
        return asesorService.listarAsesores();
    }

    @GetMapping("/{id}")
    public AsesorResponse obtenerAsesor(@PathVariable String id) {
        return asesorService.obtenerAsesor(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsesorResponse registrarAsesor(@RequestBody AsesorRequest request) {
        return asesorService.registrarAsesor(request);
    }

    @PutMapping("/{id}")
    public AsesorResponse actualizarAsesor(@PathVariable String id, @RequestBody AsesorRequest request) {
        return asesorService.actualizarAsesor(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAsesor(@PathVariable String id) {
        asesorService.eliminarAsesor(id);
    }
}