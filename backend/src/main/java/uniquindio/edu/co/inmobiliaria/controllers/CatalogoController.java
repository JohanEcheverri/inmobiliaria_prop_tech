package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @GetMapping("/clientes")
    public Map<String, Object> catalogosCliente() {
        return Map.of(
                "tiposCliente", TipoCliente.values(),
                "zonas", Zona.values(),
                "tiposInmueble", TipoInmueble.values(),
                "estadosBusqueda", EstadoBusquedaCliente.values()
        );
    }
}
