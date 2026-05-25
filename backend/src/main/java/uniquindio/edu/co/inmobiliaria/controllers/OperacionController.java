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
        if (operaciones == null) {
            return respuesta;
        }
        for (int i = 0; i < operaciones.size(); i++) {
            Object obj = operaciones.get(i);
            if (!(obj instanceof Venta)) {
                continue;
            }
            Venta venta = (Venta) obj;
            String codigo = venta.getInmueble() != null ? venta.getInmueble().getCodigo() : null;
            String direccion = venta.getInmueble() != null ? venta.getInmueble().getDireccion() : null;
            String ciudad = venta.getInmueble() != null && venta.getInmueble().getCiudad() != null ? venta.getInmueble().getCiudad().getNombre() : null;
            String zona = venta.getInmueble() != null && venta.getInmueble().getBarrio() != null ? venta.getInmueble().getBarrio().getZona().name() : null;
            String tipoInmueble = venta.getInmueble() != null && venta.getInmueble().getTipoInmueble() != null ? venta.getInmueble().getTipoInmueble().name() : null;
            String finalidad = venta.getInmueble() != null && venta.getInmueble().getFinalidad() != null ? venta.getInmueble().getFinalidad().name() : null;
            double precio = venta.getInmueble() != null ? venta.getInmueble().getPrecio() : 0;
            double area = venta.getInmueble() != null ? venta.getInmueble().getArea() : 0;
            int habitaciones = venta.getInmueble() != null ? venta.getInmueble().getNumeroHabitaciones() : 0;
            int banios = venta.getInmueble() != null ? venta.getInmueble().getNumeroBanios() : 0;
            String asesorNombre = venta.getAsesor() != null ? venta.getAsesor().getNombre() : null;
            double comision = venta.getComision();
            double valorAcordado = venta.getValorAcordado();
            java.time.LocalDateTime fechaCompra = venta.getFecha();

            respuesta.add(new PropiedadAdquiridaResponse(
                    codigo,
                    direccion,
                    ciudad,
                    zona,
                    tipoInmueble,
                    finalidad,
                    precio,
                    area,
                    habitaciones,
                    banios,
                    asesorNombre,
                    comision,
                    valorAcordado,
                    fechaCompra
            ));
        }
        return respuesta;
    }

}
