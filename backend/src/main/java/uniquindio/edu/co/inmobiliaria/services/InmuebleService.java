package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.comportamiento.ComportamientoService;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.entities.Barrio;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.services.OperacionService;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;

import java.util.ArrayList;
import java.util.List;

@Service
public class InmuebleService {

    private final InmuebleRepository inmuebleRepository;
    private final AsesorRepository asesorRepository;
    private final VisitasRepository visitasRepository;
    private final ComportamientoService comportamientoService;
    private final OperacionService operacionService;
    private final ClienteRepository clienteRepository;

    public InmuebleService(InmuebleRepository inmuebleRepository,
                           AsesorRepository asesorRepository,
                           VisitasRepository visitasRepository,
                           ComportamientoService comportamientoService,
                           OperacionService operacionService,
                           ClienteRepository clienteRepository) {
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
        this.visitasRepository = visitasRepository;
        this.comportamientoService = comportamientoService;
        this.operacionService = operacionService;
        this.clienteRepository = clienteRepository;
    }


    public List<InmuebleResponse> listarInmuebles() {
        List<InmuebleResponse> respuesta = new ArrayList<>();
        List<Inmueble> inmuebles = inmuebleRepository.findAllConAsesor();
        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            // No mostrar inmuebles vendidos en el catálogo público
            if (inmueble.getEstado() == Estado.VENDIDO) {
                continue;
            }
            respuesta.add(mapear(inmueble));
        }
        return respuesta;
    }

    public InmuebleResponse obtenerInmueble(String codigo) {
        Inmueble inmueble = inmuebleRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo));
        return mapear(inmueble);
    }

    public InmuebleResponse registrarInmueble(InmuebleRequest request) {
        validarInmueble(request, true);
        if (inmuebleRepository.existsById(request.codigo())) {
            throw new IllegalArgumentException("Ya existe un inmueble con el código: " + request.codigo());
        }

        Inmueble inmueble = construirInmueble(request, request.codigo());
        inmuebleRepository.save(inmueble);
        return mapear(inmueble);
    }

    public InmuebleResponse actualizarInmueble(String codigo, InmuebleRequest request) {
        if (estaVacio(codigo)) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        validarInmueble(request, false);

        Inmueble inmuebleExistente = inmuebleRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo));

        // No permitir modificaciones si ya fue vendido
        if (inmuebleExistente.getEstado() == Estado.VENDIDO) {
            throw new IllegalArgumentException("No se puede modificar un inmueble que ya fue vendido");
        }

        double precioAnterior = inmuebleExistente.getPrecio();

        Inmueble inmuebleActualizado = construirInmueble(request, codigo);
        inmuebleRepository.update(inmuebleActualizado);
        registrarCambioPrecioSiAplica(codigo, precioAnterior, inmuebleActualizado.getPrecio());
        return mapear(inmuebleActualizado);
    }

    public void registrarInmueble(String codigo, String direccion, Ciudad ciudad, Barrio barrio,
                                  TipoInmueble tipo, Finalidad finalidad, double precio, double area,
                                  int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor,
                                  String imagen) {
        validarDatosInmueble(codigo, direccion, ciudad, barrio, tipo, finalidad, precio, area,
                numeroHabitaciones, numeroBanios, estado, asesor);
        if (inmuebleRepository.existsById(codigo)) {
            throw new IllegalArgumentException("Ya existe un inmueble con el código: " + codigo);
        }

        Inmueble inmueble = construirInmueble(codigo, direccion, ciudad, barrio.getZona(), tipo, finalidad, precio, area,
                numeroHabitaciones, numeroBanios, estado, asesor, normalizarImagenes(List.of(imagen)));
        inmuebleRepository.save(inmueble);
    }

    public void actualizarInmueble(String codigo, String direccion, Ciudad ciudad, Barrio barrio,
                                   TipoInmueble tipo, Finalidad finalidad, double precio, double area,
                                   int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor,
                                   String imagen) {
        validarDatosInmueble(codigo, direccion, ciudad, barrio, tipo, finalidad, precio, area,
                numeroHabitaciones, numeroBanios, estado, asesor);

        Inmueble inmuebleExistente = inmuebleRepository.findByCodigo(codigo);
        if (inmuebleExistente == null) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo);
        }

        double precioAnterior = inmuebleExistente.getPrecio();
        Inmueble inmuebleActualizado = construirInmueble(codigo, direccion, ciudad, barrio.getZona(), tipo, finalidad, precio,
                area, numeroHabitaciones, numeroBanios, estado, asesor, normalizarImagenes(List.of(imagen)));
        inmuebleRepository.update(inmuebleActualizado);
        registrarCambioPrecioSiAplica(codigo, precioAnterior, precio);
    }

    public InmuebleResponse actualizarEstadoInmueble(String codigo, uniquindio.edu.co.inmobiliaria.models.dto.EstadoInmuebleRequest request) {
        if (request == null || request.estado() == null) {
            throw new IllegalArgumentException("El estado del inmueble es obligatorio");
        }
        Estado estado = request.estado();
        Inmueble inmueble = inmuebleRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo));

        // Si ya está vendido, no permitir cambios
        if (inmueble.getEstado() == Estado.VENDIDO) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un inmueble ya vendido");
        }

        Estado estadoAnterior = inmueble.getEstado();
        inmueble.setEstado(estado);
        inmuebleRepository.update(inmueble);

        // Registrar cierre en asesor
        if (esCierre(estadoAnterior, estado) && inmueble.getAsesor() != null) {
            Asesor asesor = inmueble.getAsesor();
            asesor.setNumeroDeCierres((asesor.getNumeroDeCierres() != null ? asesor.getNumeroDeCierres() : 0) + 1);
            asesorRepository.update(asesor);
        }

        // Si el nuevo estado es VENDIDO y se enviaron detalles de la venta, crear la operación de venta
        if (estado == Estado.VENDIDO && request.venta() != null) {
            uniquindio.edu.co.inmobiliaria.models.dto.VentaRequest ventaReq = request.venta();
            // Buscar comprador
            var compradorOpt = clienteRepository.findById(ventaReq.clienteId());
            if (compradorOpt.isEmpty()) {
                throw new IllegalArgumentException("No se encontró el cliente comprador con id: " + ventaReq.clienteId());
            }
            var comprador = compradorOpt.get();

            // Construir objeto Venta (subclase de Operacion)
            Venta venta = Venta.builder()
                    .codigo("VENTA-" + inmueble.getCodigo() + "-" + System.currentTimeMillis())
                    .inmueble(inmueble)
                    .cliente(comprador)
                    .asesor(inmueble.getAsesor())
                    .fecha(java.time.LocalDateTime.now())
                    .valorAcordado(ventaReq.valorAcordado())
                    .comision(ventaReq.comision())
                    .build();

            operacionService.registerSale(venta);
        }

        return mapear(inmueble);
    }

    public void eliminarInmueble(String codigo) {
        if (estaVacio(codigo)) {
            throw new IllegalArgumentException("El código del inmueble no puede estar vacío");
        }
        if (inmuebleRepository.findByCodigo(codigo) == null) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo);
        }
        inmuebleRepository.deleteByCodigo(codigo);
    }

    public Inmueble consultarInmueblePorCodigo(String codigo) {
        return inmuebleRepository.findByCodigo(codigo);
    }

    public SinglyLinkedList<Inmueble> consultarInmueblesPorCiudad(Ciudad ciudad) {
        return inmuebleRepository.findByCiudad(ciudad);
    }

    public SinglyLinkedList<Inmueble> consultarInmueblesPorTipo(TipoInmueble tipo) {
        return inmuebleRepository.findByTipo(tipo);
    }

    public SinglyLinkedList<Inmueble> consultarInmueblesPorEstado(Estado estado) {
        return inmuebleRepository.findByEstado(estado);
    }

    public Inmueble consultarInmuebleMayorDemanda() {
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        Inmueble mayorDemanda = null;
        int maxVisitas = -1;
        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            int visitas = visitasRepository.findByInmuebleCodigo(inmueble.getCodigo()).size();
            if (visitas > maxVisitas) {
                maxVisitas = visitas;
                mayorDemanda = inmueble;
            }
        }
        return mayorDemanda;
    }

    public Inmueble consultarInmueblePorPrecio(double precio) {
        return inmuebleRepository.findByPrecio(precio);
    }

    public SinglyLinkedList<Inmueble> consultarInmueblesEnRangoPrecio(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findInmueblesEnRangoPrecio(min, max);
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    public SinglyLinkedList<Inmueble> sortByPrice() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Double.compare(i1.getPrecio(), i2.getPrecio()));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    public SinglyLinkedList<Inmueble> sortByArea() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Double.compare(i1.getArea(), i2.getArea()));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    public SinglyLinkedList<Inmueble> sortByDemand() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Integer.compare(
                visitasRepository.findByInmuebleCodigo(i2.getCodigo()).size(),
                visitasRepository.findByInmuebleCodigo(i1.getCodigo()).size()
        ));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    private Inmueble construirInmueble(InmuebleRequest request, String codigo) {
        Ciudad ciudad = new Ciudad(request.ciudad().trim(), !estaVacio(request.departamento()) ? request.departamento().trim() : "");
        String direccionBarrio = obtenerDireccionBarrio(request);
        Zona zona = parseEnum(Zona.class, request.zona(), "zona");
        Asesor asesor = resolverAsesor(request.asesorResponsable());

        return construirInmueble(
                codigo,
                direccionBarrio,
                ciudad,
                zona,
                parseEnum(TipoInmueble.class, request.tipoInmueble(), "tipo de inmueble"),
                parseFinalidad(request.finalidad()),
                request.precio(),
                request.area(),
                request.habitaciones(),
                request.banos(),
                parseEstado(request.disponibilidad(), request.estadoInmueble()),
                asesor,
                normalizarImagenes(request.imagenes() != null ? request.imagenes() : List.of(request.imagen()))
        );
    }

    private Inmueble construirInmueble(String codigo, String direccionBarrio, Ciudad ciudad, Zona zona,
                                       TipoInmueble tipo, Finalidad finalidad, double precio, double area,
                                       int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor,
                                       List<String> imagenes) {
        return Inmueble.builder()
                .codigo(codigo)
                .direccionBarrio(direccionBarrio)
                .ciudad(ciudad)
                .zona(zona)
                .tipoInmueble(tipo)
                .finalidad(finalidad)
                .precio(precio)
                .area(area)
                .numeroHabitaciones(numeroHabitaciones)
                .numeroBanios(numeroBanios)
                .estado(estado)
                .asesor(asesor)
                .imagen(imagenes)
                .build();
    }

    private Asesor resolverAsesor(String asesorResponsable) {
        if (estaVacio(asesorResponsable)) {
            return null;
        }
        return asesorRepository.findById(asesorResponsable)
                .or(() -> asesorRepository.findByEmail(asesorResponsable))
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un asesor con el id o email: " + asesorResponsable));
    }

    public InmuebleResponse mapear(Inmueble inmueble) {
        String ciudad = inmueble.getCiudad() != null ? inmueble.getCiudad().getNombre() : "";
        String departamento = inmueble.getCiudad() != null ? inmueble.getCiudad().getDepartamento() : "";
        String direccionBarrio = inmueble.getDireccionBarrio();
        List<String> imagenes = inmueble.getImagen() != null ? inmueble.getImagen() : new ArrayList<>();
        String imagenPrincipal = imagenes.isEmpty() ? null : imagenes.get(0);
        Asesor asesor = inmueble.getAsesor();
        String asesorNombre = asesor != null ? asesor.getNombre() : null;
        String asesorId = asesor != null ? asesor.getId() : null;
        String disponibilidad = inmueble.getEstado() != null ? inmueble.getEstado().name() : "";

        return new InmuebleResponse(
                inmueble.getCodigo(),
                direccionBarrio,
                direccionBarrio,
                ciudad,
                departamento,
                direccionBarrio,
                inmueble.getZona(),
                inmueble.getTipoInmueble(),
                inmueble.getFinalidad(),
                inmueble.getPrecio(),
                inmueble.getArea(),
                inmueble.getNumeroHabitaciones(),
                inmueble.getNumeroBanios(),
                disponibilidad,
                disponibilidad,
                asesorNombre,
                asesorId,
                imagenPrincipal,
                imagenes,
                inmueble.getEstado()
        );
    }

    private void validarInmueble(InmuebleRequest request, boolean validarCodigo) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del inmueble son obligatorios");
        }
        if (validarCodigo && estaVacio(request.codigo())) {
            throw new IllegalArgumentException("El código del inmueble no puede estar vacío");
        }
        if (estaVacio(obtenerDireccionBarrio(request))) {
            throw new IllegalArgumentException("La dirección o barrio del inmueble no puede estar vacío");
        }
        if (estaVacio(request.ciudad())) {
            throw new IllegalArgumentException("La ciudad del inmueble no puede estar vacía");
        }
        parseEnum(Zona.class, request.zona(), "zona");
        if (request.precio() == null || request.precio() <= 0) {
            throw new IllegalArgumentException("El precio del inmueble debe ser mayor a cero");
        }
        if (request.area() == null || request.area() <= 0) {
            throw new IllegalArgumentException("El área del inmueble debe ser mayor a cero");
        }
        if (request.habitaciones() == null || request.habitaciones() < 0) {
            throw new IllegalArgumentException("El número de habitaciones no puede ser negativo");
        }
        if (request.banos() == null || request.banos() < 0) {
            throw new IllegalArgumentException("El número de baños no puede ser negativo");
        }
        parseEnum(TipoInmueble.class, request.tipoInmueble(), "tipo de inmueble");
        parseFinalidad(request.finalidad());
        parseEstado(request.disponibilidad(), request.estadoInmueble());
        validarImagenes(request.imagenes() != null ? request.imagenes() : List.of(request.imagen()));
    }

    private void validarDatosInmueble(String codigo, String direccion, Ciudad ciudad, Barrio barrio,
                                      TipoInmueble tipo, Finalidad finalidad, double precio, double area,
                                      int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor) {
        if (estaVacio(codigo)) {
            throw new IllegalArgumentException("El código del inmueble no puede estar vacío");
        }
        if (estaVacio(direccion)) {
            throw new IllegalArgumentException("La dirección del inmueble no puede estar vacía");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio del inmueble debe ser mayor a cero");
        }
        if (area <= 0) {
            throw new IllegalArgumentException("El área del inmueble debe ser mayor a cero");
        }
        if (numeroHabitaciones < 0) {
            throw new IllegalArgumentException("El número de habitaciones no puede ser negativo");
        }
        if (numeroBanios < 0) {
            throw new IllegalArgumentException("El número de baños no puede ser negativo");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del inmueble no puede ser nulo");
        }
        if (finalidad == null) {
            throw new IllegalArgumentException("La finalidad del inmueble no puede ser nula");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de inmueble no puede ser nulo");
        }
        if (ciudad == null) {
            throw new IllegalArgumentException("La ciudad del inmueble no puede ser nula");
        }
        if (barrio == null) {
            throw new IllegalArgumentException("El barrio del inmueble no puede ser nulo");
        }
        if (asesor == null) {
            throw new IllegalArgumentException("El asesor del inmueble no puede ser nulo");
        }
    }

    private void registrarCambioPrecioSiAplica(String codigo, double precioAnterior, double precioNuevo) {
        if (Double.compare(precioAnterior, precioNuevo) != 0) {
            comportamientoService.registrarCambioPrecio(codigo, precioAnterior, precioNuevo);
        }
    }

    private boolean esCierre(Estado anterior, Estado nuevo) {
        boolean antesCerrado = anterior == Estado.VENDIDO || anterior == Estado.ARRENDADO;
        boolean ahoraCerrado = nuevo == Estado.VENDIDO || nuevo == Estado.ARRENDADO;
        return !antesCerrado && ahoraCerrado;
    }

    private String obtenerDireccionBarrio(InmuebleRequest request) {
        if (request == null) {
            return null;
        }
        if (!estaVacio(request.direccionBarrio())) {
            return request.direccionBarrio().trim();
        }
        if (!estaVacio(request.direccion())) {
            return request.direccion().trim();
        }
        if (!estaVacio(request.barrio())) {
            return request.barrio().trim();
        }
        return null;
    }

    private List<String> normalizarImagenes(List<String> imagenes) {
        List<String> resultado = new ArrayList<>();
        if (imagenes == null) {
            return resultado;
        }
        for (String imagen : imagenes) {
            if (!estaVacio(imagen)) {
                resultado.add(imagen.trim());
            }
        }
        return resultado;
    }

    private void validarImagenes(List<String> imagenes) {
        List<String> normalizadas = normalizarImagenes(imagenes);
        if (normalizadas.isEmpty()) {
            return;
        }
        if (normalizadas.size() < 3 || normalizadas.size() > 6) {
            throw new IllegalArgumentException("El inmueble debe tener entre 3 y 6 imágenes cuando se adjuntan imágenes");
        }
    }

    private Finalidad parseFinalidad(String valor) {
        if ("ARRIENDO".equalsIgnoreCase(valor)) {
            return Finalidad.ARRENDAMIENTO;
        }
        return parseEnum(Finalidad.class, valor, "finalidad");
    }

    private Estado parseEstado(String disponibilidad, String estadoInmueble) {
        String valor = !estaVacio(disponibilidad) ? disponibilidad : estadoInmueble;
        if ("Disponible".equalsIgnoreCase(valor)) {
            return Estado.DISPONIBLE;
        }
        if ("Vendido".equalsIgnoreCase(valor)) {
            return Estado.VENDIDO;
        }
        if ("Arrendado".equalsIgnoreCase(valor)) {
            return Estado.ARRENDADO;
        }
        return parseEnum(Estado.class, valor, "estado del inmueble");
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String valor, String campo) {
        if (estaVacio(valor)) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return Enum.valueOf(enumType, valor.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ": " + valor);
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
