package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.Tree;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.structures.PriorityQueue;

import java.util.Objects;

@Repository
public class InmuebleRepository {

    //private final DynamicArrayList<Inmueble> inmuebles;

    private final Tree<Inmueble> inmueblesPorPrecio;

    private final HashTable<String, Inmueble> inmueblesPorCodigo;

    private final HashTable<Ciudad, SinglyLinkedList<Inmueble>> inmueblesPorCiudad;

    private final HashTable<TipoInmueble, SinglyLinkedList<Inmueble>> inmueblesPorTipo;

    private final HashTable<Estado, SinglyLinkedList<Inmueble>> inmueblesPorEstado;

    private final PriorityQueue<Inmueble> inmueblesMayorDemanda;

    public InmuebleRepository() {
        // this.inmuebles = new DynamicArrayList<>();
        this.inmueblesPorPrecio = new Tree<>(
            this::compararPorPrecioYCodigo);
        this.inmueblesPorCodigo = new HashTable<>();
        this.inmueblesPorCiudad = new HashTable<>();
        this.inmueblesPorTipo = new HashTable<>();
        this.inmueblesPorEstado = new HashTable<>();
        this.inmueblesMayorDemanda = new PriorityQueue<>(
                (i1, i2) -> Integer.compare(i2.getNumeroHabitaciones(), i1.getNumeroHabitaciones())); // Ej
    }

    public void save(Inmueble inmueble) {

        if (inmueble == null || inmueble.getCodigo() == null) {
            throw new IllegalArgumentException("El inmueble o su código no pueden ser nulos");
        }
        if (inmueblesPorCodigo.containsKey(inmueble.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un inmueble con el código: " + inmueble.getCodigo());
        }

        // Agregar a la estructura principal
        inmueblesPorPrecio.insert(inmueble);
        inmueblesPorCodigo.put(inmueble.getCodigo(), inmueble);

        // Agregar a índices secundarios
        inmueblesPorCiudad.computeIfAbsent(inmueble.getCiudad(), k -> new SinglyLinkedList<>()).addLast(inmueble);
        inmueblesPorTipo.computeIfAbsent(inmueble.getTipoInmueble(), k -> new SinglyLinkedList<>()).addLast(inmueble);
        inmueblesPorEstado.computeIfAbsent(inmueble.getEstado(), k -> new SinglyLinkedList<>()).addLast(inmueble);

        // Agregar a la cola de mayor demanda (ejemplo simple basado en número de
        // habitaciones)
        inmueblesMayorDemanda.enqueue(inmueble);
    }

    public void update(Inmueble inmueble) {
        if (inmueble == null || inmueble.getCodigo() == null) {
            throw new IllegalArgumentException("El inmueble o su código no pueden ser nulos");
        }
        if (!inmueblesPorCodigo.containsKey(inmueble.getCodigo())) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + inmueble.getCodigo());
        }

        deleteByCodigo(inmueble.getCodigo());
        save(inmueble);
    }

    public void deleteByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del inmueble no puede estar vacío");
        }
        if (!inmueblesPorCodigo.containsKey(codigo)) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + codigo);
        }

        Inmueble inmueble = inmueblesPorCodigo.remove(codigo);
        inmueblesPorPrecio.remove(inmueble);
        removerDeIndice(inmueblesPorCiudad, inmueble.getCiudad(), inmueble);
        removerDeIndice(inmueblesPorTipo, inmueble.getTipoInmueble(), inmueble);
        removerDeIndice(inmueblesPorEstado, inmueble.getEstado(), inmueble);
        reconstruirColaMayorDemanda();
    }


    public Inmueble findByCodigo(String codigo) {
        if (codigo == null || !inmueblesPorCodigo.containsKey(codigo)) {
            return null;
        }
        return inmueblesPorCodigo.get(codigo);
    }

    public SinglyLinkedList<Inmueble> findByCiudad(Ciudad ciudad) {
        return inmueblesPorCiudad.get(ciudad);
    }

    public SinglyLinkedList<Inmueble> findByTipo(TipoInmueble tipo) {
        return inmueblesPorTipo.get(tipo);
    }

    public SinglyLinkedList<Inmueble> findByEstado(Estado estado) {
        return inmueblesPorEstado.get(estado);
    }

    public Inmueble findInmuebleMayorDemanda() {
        return inmueblesMayorDemanda.peek();
    }

    
    public Inmueble findByPrecio(double precio){
        DynamicArrayList<Inmueble> inmueblesOrdenados = inmueblesPorPrecio.inOrder();
        for (int i = 0; i < inmueblesOrdenados.size(); i++) {
            Inmueble inmueble = inmueblesOrdenados.get(i);
            if (Double.compare(inmueble.getPrecio(), precio) == 0) {
                return inmueble;
            }
            if (inmueble.getPrecio() > precio) {
                break;
            }
        }

        return null;
        
    }

    //!Revisar de aqui pa abajho 
    //__________________________________________________________________________________________________________________________
    public DynamicArrayList<Inmueble> findInmueblesEnRangoPrecio(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor al precio máximo");
        }

        DynamicArrayList<Inmueble> resultado = new DynamicArrayList<>();
        DynamicArrayList<Inmueble> inmueblesOrdenados = inmueblesPorPrecio.inOrder();
        for (int i = 0; i < inmueblesOrdenados.size(); i++) {
            Inmueble inmueble = inmueblesOrdenados.get(i);
            if (inmueble.getPrecio() >= min && inmueble.getPrecio() <= max) {
                resultado.add(inmueble);
            }
            if (inmueble.getPrecio() > max) {
                break;
            }
        }
        return resultado;
    }

    private <K> void removerDeIndice(HashTable<K, SinglyLinkedList<Inmueble>> indice, K llave, Inmueble inmueble) {
        if (llave == null || !indice.containsKey(llave)) {
            return;
        }
        SinglyLinkedList<Inmueble> inmuebles = indice.get(llave);
        inmuebles.remove(inmueble);
        if (inmuebles.isEmpty()) {
            indice.remove(llave);
        }
    }

    private void reconstruirColaMayorDemanda() {
        inmueblesMayorDemanda.clear();
        DynamicArrayList<Inmueble> inmuebles = inmueblesPorCodigo.values();
        for (int i = 0; i < inmuebles.size(); i++) {
            inmueblesMayorDemanda.enqueue(inmuebles.get(i));
        }
    }

    private int compararPorPrecioYCodigo(Inmueble i1, Inmueble i2) {
        int comparacionPrecio = Double.compare(i1.getPrecio(), i2.getPrecio());
        if (comparacionPrecio != 0) {
            return comparacionPrecio;
        }
        if (Objects.equals(i1.getCodigo(), i2.getCodigo())) {
            return 0;
        }
        if (i1.getCodigo() == null) {
            return -1;
        }
        if (i2.getCodigo() == null) {
            return 1;
        }
        return i1.getCodigo().compareTo(i2.getCodigo());
    }
}
