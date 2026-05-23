package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.comportamiento.ComportamientoService;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;
import uniquindio.edu.co.inmobiliaria.models.entities.Barrio;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;

@Service
public class InmuebleService {

    private final InmuebleRepository inmuebleRepository;
    private final VisitasRepository visitasRepository;
    private final ComportamientoService comportamientoService;

    @Autowired
    public InmuebleService(InmuebleRepository inmuebleRepository, VisitasRepository visitasRepository, ComportamientoService comportamientoService) {
        this.inmuebleRepository = inmuebleRepository;
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
