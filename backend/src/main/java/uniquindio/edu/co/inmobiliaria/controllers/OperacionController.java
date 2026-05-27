package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.models.dto.PropiedadAdquiridaResponse;
import uniquindio.edu.co.inmobiliaria.models.dto.VentaRequest;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.services.OperacionService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/operaciones")
@Transactional(readOnly = true) // Mantiene viva la sesión de Hibernate para toda la clase, incluyendo métodos privados al mapear asociaciones lazy
/**
 * Controlador REST para la gestión de operaciones comerciales (como ventas o alquileres).
 * Contiene endpoints para el registro de operaciones, así como para la consulta de datos analíticos.
 */
public class OperacionController {

    private final OperacionService operacionService;

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    // --- Endpoints de Registro e Historial (origin/master) ---

    /**
     * Registra una operación de venta completa (crea operación, contrato y registros relacionados).
     * Operación transaccional que persiste la información enviada en el request.
     *
     * @param request DTO con los datos de la venta (inmueble, cliente, asesor, valor y comisión)
     */
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

    /**
     * Obtiene las propiedades adquiridas por un cliente específico.
     *
     * @param clienteId id del cliente
     * @return lista de PropiedadAdquiridaResponse con las compras del cliente
     */
    @GetMapping("/cliente/{clienteId}/propiedades")
    public List<PropiedadAdquiridaResponse> obtenerPropiedadesAdquiridas(@PathVariable String clienteId) {
        return mapearPropiedades(operacionService.obtenerPropiedadesAdquiridasCliente(clienteId));
    }

    // --- Endpoints Analíticos y de Reportes (de tu commit local) ---

    /**
     * Endpoint analítico que devuelve operaciones filtradas por zona.
     *
     * @param zona zona a filtrar
     * @return lista de ReporteOperacionResponse
     */
    @GetMapping("/zona/{zona}")
    public ResponseEntity<List<ReporteOperacionResponse>> getOperacionesPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(mapearOperaciones(operacionService.consultarOperacionesPorZona(zona)));
    }

    /**
     * Devuelve operaciones cuyo valor pactado se sitúe en el rango indicado.
     *
     * @param min valor mínimo
     * @param max valor máximo
     * @return lista de ReporteOperacionResponse filtradas por precio
     */
    @GetMapping("/precio")
    public ResponseEntity<List<ReporteOperacionResponse>> getOperacionesPorPrecio(@RequestParam double min, @RequestParam double max) {
        return ResponseEntity.ok(mapearOperaciones(operacionService.consultarOperacionesPorPrecio(min, max)));
    }

    /**
     * Obtiene visitas filtradas por zona, útil para análisis de demanda.
     *
     * @param zona zona a filtrar
     * @return lista de ReporteVisitaResponse
     */
    @GetMapping("/visitas/zona/{zona}")
    public ResponseEntity<List<ReporteVisitaResponse>> getVisitasPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(mapearVisitas(operacionService.consultarVisitasPorZona(zona)));
    }

    /**
     * Devuelve las operaciones que se encuentran en estado cerrado.
     *
     * @return lista de ReporteOperacionResponse para operaciones cerradas
     */
    @GetMapping("/cerradas")
    public ResponseEntity<List<ReporteOperacionResponse>> getOperacionesCerradas() {
        return ResponseEntity.ok(mapearOperaciones(operacionService.consultarOperacionesCerradas()));
    }

    /**
     * Devuelve operaciones cerradas filtradas por zona.
     *
     * @param zona zona a filtrar
     * @return lista de ReporteOperacionResponse
     */
    @GetMapping("/cerradas/zona/{zona}")
    public ResponseEntity<List<ReporteOperacionResponse>> getOperacionesCerradasPorZona(@PathVariable Zona zona) {
        return ResponseEntity.ok(mapearOperaciones(operacionService.consultarOperacionesCerradasPorZona(zona)));
    }

    // --- Métodos Auxiliares ---

    /**
     * Mapea una lista dinámica de operaciones a DTOs PropiedadAdquiridaResponse filtrando
     * solamente las instancias de Venta.
     *
     * @param operaciones lista dinámica que puede contener diferentes tipos de operaciones
     * @return lista de PropiedadAdquiridaResponse para las ventas encontradas
     */
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
            List<String> imagenes = venta.getInmueble() != null && venta.getInmueble().getImagen() != null
                    ? venta.getInmueble().getImagen()
                    : Collections.emptyList();
            String imagenPrincipal = imagenes.isEmpty() ? null : imagenes.get(0);
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
                    imagenPrincipal,
                    imagenes,
                    comision,
                    valorAcordado,
                    fechaCompra
            ));
        }
        return respuesta;
    }

    /**
     * Convierte una DynamicArrayList genérica en una java.util.List estándar.
     *
     * @param dynamicList lista dinámica de entrada
     * @param <T> tipo de elementos
     * @return lista regular con los mismos elementos (vacía si la entrada es null)
     */
    private <T> List<T> convertirALista(DynamicArrayList<T> dynamicList) {
        List<T> lista = new ArrayList<>();
        if (dynamicList != null) {
            for (int i = 0; i < dynamicList.size(); i++) {
                lista.add(dynamicList.get(i));
            }
        }
        return lista;
    }

    /**
     * Mapea una colección dinámica de Operacion a DTOs ReporteOperacionResponse.
     *
     * @param operaciones lista dinámica de operaciones
     * @return lista de ReporteOperacionResponse (vacía si la entrada es null)
     */
    private List<ReporteOperacionResponse> mapearOperaciones(DynamicArrayList<Operacion> operaciones) {
        List<ReporteOperacionResponse> respuesta = new ArrayList<>();
        if (operaciones == null) {
            return respuesta;
        }
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            respuesta.add(mapearOperacion(operacion));
        }
        return respuesta;
    }

    /**
     * Mapea una colección dinámica de Visita a DTOs ReporteVisitaResponse.
     *
     * @param visitas lista dinámica de visitas
     * @return lista de ReporteVisitaResponse
     */
    private List<ReporteVisitaResponse> mapearVisitas(DynamicArrayList<Visita> visitas) {
        List<ReporteVisitaResponse> respuesta = new ArrayList<>();
        if (visitas == null) {
            return respuesta;
        }
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            respuesta.add(mapearVisita(visita));
        }
        return respuesta;
    }

    /**
     * Mapea una Operacion a su DTO de reporte, extrayendo cuidadosamente datos de
     * asociaciones que pueden ser nulas para evitar NPEs.
     *
     * @param operacion entidad Operacion
     * @return ReporteOperacionResponse con la información relevante de la operación
     */
    private ReporteOperacionResponse mapearOperacion(Operacion operacion) {
        var inmueble = operacion.getInmueble();
        var cliente = operacion.getCliente();
        var asesor = operacion.getAsesor();

        return new ReporteOperacionResponse(
                operacion.getCodigo(),
                operacion.getClass().getSimpleName(),
                operacion.getEstado() != null ? operacion.getEstado().name() : null,
                operacion.getFecha(),
                operacion.getValorAcordado(),
                operacion.getComision(),
                inmueble != null ? inmueble.getCodigo() : null,
                inmueble != null ? inmueble.getDireccion() : null,
                inmueble != null && inmueble.getZona() != null ? inmueble.getZona().name() : null,
                inmueble != null ? inmueble.getPrecio() : 0,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() : null,
                asesor != null ? asesor.getId() : null,
                asesor != null ? asesor.getNombre() : null
        );
    }

    /**
     * Mapea una entidad Visita a su DTO de reporte con campos seguros contra null.
     *
     * @param visita entidad Visita
     * @return ReporteVisitaResponse con la información necesaria para reportes
     */
    private ReporteVisitaResponse mapearVisita(Visita visita) {
        var inmueble = visita.getInmueble();
        var cliente = visita.getCliente();
        var asesor = visita.getAsesotAsignado();

        return new ReporteVisitaResponse(
                visita.getCodigo(),
                visita.getFecha(),
                visita.getHora(),
                visita.getEstado() != null ? visita.getEstado().name() : null,
                visita.getObservaciones(),
                inmueble != null ? inmueble.getCodigo() : null,
                inmueble != null ? inmueble.getDireccion() : null,
                inmueble != null && inmueble.getZona() != null ? inmueble.getZona().name() : null,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNombre() : null,
                asesor != null ? asesor.getId() : null,
                asesor != null ? asesor.getNombre() : null
        );
    }

    private record ReporteOperacionResponse(
            String codigo,
            String tipoOperacion,
            String estado,
            java.time.LocalDateTime fecha,
            double valorAcordado,
            double comision,
            String inmuebleCodigo,
            String inmuebleDireccion,
            String zona,
            double inmueblePrecio,
            String clienteId,
            String clienteNombre,
            String asesorId,
            String asesorNombre
    ) {
    }

    private record ReporteVisitaResponse(
            String codigo,
            java.time.LocalDate fecha,
            java.time.LocalTime hora,
            String estado,
            String observaciones,
            String inmuebleCodigo,
            String inmuebleDireccion,
            String zona,
            String clienteId,
            String clienteNombre,
            String asesorId,
            String asesorNombre
    ) {
    }
}
