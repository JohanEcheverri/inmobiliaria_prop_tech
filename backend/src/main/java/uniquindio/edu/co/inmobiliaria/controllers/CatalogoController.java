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
/**
 * Controlador REST que proporciona información estática de catálogos y enumeraciones
 * necesarios para alimentar las interfaces de usuario.
 */
public class CatalogoController {

    /**
     * Devuelve catálogos estáticos usados por el frontend para formularios y filtros.
     * Agrupa enums como tipos de cliente, zonas, tipos de inmueble y estados de búsqueda.
     *
     * @return mapa con arrays de valores enumerados
     */
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
