package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.models.dto.PropiedadAdquiridaResponse;
import uniquindio.edu.co.inmobiliaria.models.dto.VentaRequest;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.services.OperacionService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/operaciones")
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    @PostMapping("/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public void registrarVenta(@RequestBody VentaRequest request) {
        operacionService.registrarVentaCompleta(
                request.inmuebleCodigo(),
                request.clienteId(),
                request.asesorId(),
                request.valorAcordado(),
                request.comision()
        );
    }

    @GetMapping("/cliente/{clienteId}/propiedades")
    @Transactional(readOnly = true)
    public List<PropiedadAdquiridaResponse> obtenerPropiedadesAdquiridas(@PathVariable String clienteId) {
        return mapearPropiedades(operacionService.obtenerPropiedadesAdquiridasCliente(clienteId));
    }

    private List<PropiedadAdquiridaResponse> mapearPropiedades(DynamicArrayList<?> operaciones) {
        List<PropiedadAdquiridaResponse> respuesta = new ArrayList<>();
        // Implementación de mapeo - será llenado según las necesidades
        return respuesta;
    }
}
