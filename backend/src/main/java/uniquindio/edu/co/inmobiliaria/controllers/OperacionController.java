package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.services.OperacionService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/operaciones")
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    @GetMapping("/zona/{zona}")
    public ResponseEntity<List<Operacion>> getOperacionesPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(convertirALista(operacionService.consultarOperacionesPorZona(zona)));
    }

    @GetMapping("/precio")
    public ResponseEntity<List<Operacion>> getOperacionesPorPrecio(@RequestParam double min, @RequestParam double max) {
        return ResponseEntity.ok(convertirALista(operacionService.consultarOperacionesPorPrecio(min, max)));
    }

    @GetMapping("/visitas/zona/{zona}")
    public ResponseEntity<List<Visita>> getVisitasPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(convertirALista(operacionService.consultarVisitasPorZona(zona)));
    }

    @GetMapping("/cerradas")
    public ResponseEntity<List<Operacion>> getOperacionesCerradas() {
        return ResponseEntity.ok(convertirALista(operacionService.consultarOperacionesCerradas()));
    }

    @GetMapping("/cerradas/zona/{zona}")
    public ResponseEntity<List<Operacion>> getOperacionesCerradasPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(convertirALista(operacionService.consultarOperacionesCerradasPorZona(zona)));
    }

    private <T> List<T> convertirALista(DynamicArrayList<T> dynamicList) {
        List<T> lista = new java.util.ArrayList<>();
        if (dynamicList != null) {
            for (int i = 0; i < dynamicList.size(); i++) {
                lista.add(dynamicList.get(i));
            }
        }
        return lista;
    }
}
