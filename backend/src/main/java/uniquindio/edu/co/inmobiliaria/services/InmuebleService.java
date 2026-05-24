package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.InmuebleResponse;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.comportamiento.ComportamientoService;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;
import uniquindio.edu.co.inmobiliaria.models.entities.Barrio;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
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

    @Autowired
    public InmuebleService(InmuebleRepository inmuebleRepository, AsesorRepository asesorRepository) {
    public InmuebleService(InmuebleRepository inmuebleRepository, VisitasRepository visitasRepository, ComportamientoService comportamientoService) {
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
    }

    public List<InmuebleResponse> listarInmuebles() {
        List<InmuebleResponse> respuesta = new ArrayList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        for (int i = 0; i < inmuebles.size(); i++) {
            respuesta.add(mapear(inmuebles.get(i)));
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
        if (!inmuebleRepository.existsById(codigo)) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo);
        }

        Inmueble inmueble = construirInmueble(request, codigo);
        inmuebleRepository.update(inmueble);
        return mapear(inmueble);
        this.visitasRepository = visitasRepository;
        this.comportamientoService = comportamientoService;
    }

    public void registrarInmueble(String codigo, String direccion, Ciudad ciudad, Barrio barriro , TipoInmueble tipo, Finalidad finalidad, double precio, double area, int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor, String imagen) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del inmueble no puede estar vacío");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La dirección del inmueble no puede estar vacía");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio del inmueble debe ser mayor a cero");
        }
        if (numeroHabitaciones <= 0) {
            throw new IllegalArgumentException("El número de habitaciones debe ser mayor a cero");
        }
        if (numeroBanios <= 0) {
            throw new IllegalArgumentException("El número de baños debe ser mayor a cero");
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
        if (barriro == null) {
            throw new IllegalArgumentException("El barrio del inmueble no puede ser nulo");
        }
        if (asesor == null) {
            throw new IllegalArgumentException("El asesor del inmueble no puede ser nulo");
        }
        Inmueble inmueble = Inmueble.builder()
                .codigo(codigo)
                .direccion(direccion)
                .ciudad(ciudad)
                .barrio(barriro)
                .tipoInmueble(tipo)
                .finalidad(finalidad)
                .precio(precio)
                .area(area)
                .numeroHabitaciones(numeroHabitaciones)
                .numeroBanios(numeroBanios)
                .estado(estado)
                .asesor(asesor)
                .imagen(imagen)
                .build();
        inmuebleRepository.save(inmueble);
    }
    //! Dudoso lo del codigo
    public void actualizarInmueble(String codigo, String direccion, Ciudad ciudad, Barrio barriro , TipoInmueble tipo, Finalidad finalidad, double precio, double area, int numeroHabitaciones, int numeroBanios, Estado estado, Asesor asesor, String imagen) {
        Inmueble inmuebleExistente = inmuebleRepository.findByCodigo(codigo);
        if (inmuebleExistente == null) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo);
        }
        Inmueble inmuebleActualizado = Inmueble.builder()
                .codigo(codigo)
                .direccion(direccion)
                .ciudad(ciudad)
                .barrio(barriro)
                .tipoInmueble(tipo)
                .finalidad(finalidad)
                .precio(precio)
                .area(area)
                .numeroHabitaciones(numeroHabitaciones)
                .numeroBanios(numeroBanios)
                .estado(estado)
                .asesor(asesor)
                .imagen(imagen)
                .build();

        double precioAnterior = inmuebleExistente.getPrecio();
        inmuebleRepository.update(inmuebleActualizado);

        if (Double.compare(precioAnterior, precio) != 0) {
            comportamientoService.registrarCambioPrecio(codigo, precioAnterior, precio);
        }
    }

    public void eliminarInmueble(String codigo) {
        if (codigo == null || codigo.isBlank()) {
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
        var inmuebles = inmuebleRepository.findAll();
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
        var inmuebles = inmuebleRepository.findInmueblesEnRangoPrecio(min, max);
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    private Inmueble construirInmueble(InmuebleRequest request, String codigo) {
        Ciudad ciudad = new Ciudad(request.ciudad().trim(), "");
        Barrio barrio = new Barrio(Zona.CENTRO, request.barrio().trim(), ciudad);
        Asesor asesor = resolverAsesor(request.asesorResponsable());

        return Inmueble.builder()
                .codigo(codigo)
                .direccion(request.direccion().trim())
                .ciudad(ciudad)
                .barrio(barrio)
                .tipoInmueble(parseEnum(TipoInmueble.class, request.tipoInmueble(), "tipo de inmueble"))
                .finalidad(parseFinalidad(request.finalidad()))
                .precio(request.precio())
                .area(request.area())
                .numeroHabitaciones(request.habitaciones())
                .numeroBanios(request.banos())
                .estado(parseEstado(request.disponibilidad(), request.estadoInmueble()))
                .asesor(asesor)
                .imagen(request.imagen())
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

    private InmuebleResponse mapear(Inmueble inmueble) {
        String ciudad = inmueble.getCiudad() != null ? inmueble.getCiudad().getNombre() : "";
        String barrio = inmueble.getBarrio() != null ? inmueble.getBarrio().getNombre() : "";
        Asesor asesor = inmueble.getAsesor();
        String asesorNombre = asesor != null ? asesor.getNombre() : null;
        String asesorId = asesor != null ? asesor.getId() : null;
        String disponibilidad = inmueble.getEstado() != null ? inmueble.getEstado().name() : "";

        return new InmuebleResponse(
                inmueble.getCodigo(),
                inmueble.getDireccion(),
                ciudad,
                barrio,
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
                inmueble.getImagen(),
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
        if (estaVacio(request.direccion())) {
            throw new IllegalArgumentException("La dirección del inmueble no puede estar vacía");
        }
        if (estaVacio(request.ciudad())) {
            throw new IllegalArgumentException("La ciudad del inmueble no puede estar vacía");
        }
        if (estaVacio(request.barrio())) {
            throw new IllegalArgumentException("El barrio del inmueble no puede estar vacío");
        }
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

    public SinglyLinkedList<Inmueble> sortByPrice() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        var inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Double.compare(i1.getPrecio(), i2.getPrecio()));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    public SinglyLinkedList<Inmueble> sortByArea() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        var inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Double.compare(i1.getArea(), i2.getArea()));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }

    public SinglyLinkedList<Inmueble> sortByDemand() {
        SinglyLinkedList<Inmueble> resultado = new SinglyLinkedList<>();
        var inmuebles = inmuebleRepository.findAll();
        inmuebles.sort((i1, i2) -> Integer.compare(
                visitasRepository.findByInmuebleCodigo(i2.getCodigo()).size(),
                visitasRepository.findByInmuebleCodigo(i1.getCodigo()).size()
        ));
        for (int i = 0; i < inmuebles.size(); i++) {
            resultado.addLast(inmuebles.get(i));
        }
        return resultado;
    }
}
