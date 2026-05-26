package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ClienteJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Objects;
import java.util.Optional;

@Repository
/**
 * Repositorio en memoria para clientes con sincronización a la base de datos
 * a través de ClienteJpaRepository. Mantiene índices auxiliares (hash tables,
 * árbol y lista dinámica) para consultas rápidas por id, email y rango de
 * presupuesto.
 */
public class ClienteRepository {

    private final ClienteJpaRepository clienteJpaRepository;
    private final DynamicArrayList<Cliente> clientes;
    private final HashTable<String, Cliente> clientesPorId;
    private final HashTable<String, Cliente> clientesPorEmail;
    private final Tree<Cliente> clientesPorPresupuesto;

    public ClienteRepository(ClienteJpaRepository clienteJpaRepository) {
        this.clienteJpaRepository = clienteJpaRepository;
        this.clientes = new DynamicArrayList<>();
        this.clientesPorId = new HashTable<>();
        this.clientesPorEmail = new HashTable<>();
        this.clientesPorPresupuesto = new Tree<>(this::compararPorPresupuestoEId);
        cargarDesdeBaseDeDatos();
    }

    /**
     * Guarda un cliente en la base de datos y lo añade a los índices en memoria.
     * Valida unicidad por id y email.
     *
     * @param cliente entidad Cliente a persistir
     */
    public void save(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo");
        validarId(cliente);
        if (clientesPorId.containsKey(cliente.getId())) {
            throw new IllegalArgumentException("Ya existe un cliente con el id: " + cliente.getId());
        }
        if (cliente.getEmail() != null && clientesPorEmail.containsKey(cliente.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + cliente.getEmail());
        }
        clienteJpaRepository.save(cliente);
        agregarAIndices(cliente);
    }

    /**
     * Actualiza un cliente existente. Sincroniza cambios con la base y actualiza
     * los índices en memoria (elimina la versión anterior e indexa la nueva).
     *
     * @param clienteActualizado entidad Cliente con id existente
     */
    public void update(Cliente clienteActualizado) {
        Objects.requireNonNull(clienteActualizado, "El cliente no puede ser nulo");
        validarId(clienteActualizado);
        if (!clientesPorId.containsKey(clienteActualizado.getId())) {
            throw new IllegalArgumentException("No se encontró un cliente con el id: " + clienteActualizado.getId());
        }
        Cliente anterior = clientesPorId.get(clienteActualizado.getId());
        if (clienteActualizado.getEmail() != null
                && clientesPorEmail.containsKey(clienteActualizado.getEmail())
                && !Objects.equals(anterior.getId(), clientesPorEmail.get(clienteActualizado.getEmail()).getId())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + clienteActualizado.getEmail());
        }
        clienteJpaRepository.save(clienteActualizado);
        eliminarDeIndices(anterior);
        agregarAIndices(clienteActualizado);
    }

    public boolean deleteByEmail(String email) {
        Optional<Cliente> cliente = findByEmail(email);
        if (cliente.isEmpty()) {
            return false;
        }
        return deleteById(cliente.get().getId());
    }

    /**
     * Elimina un cliente por id tanto en la base de datos como de los índices en memoria.
     * Devuelve true si la eliminación se realizó.
     *
     * @param id identificador del cliente
     * @return true si se eliminó
     */
    public boolean deleteById(String id) {
        Optional<Cliente> cliente = findById(id);
        if (cliente.isEmpty()) {
            return false;
        }
        clienteJpaRepository.deleteById(id);
        eliminarDeIndices(cliente.get());
        return true;
    }

    /**
     * Retorna la lista en memoria de todos los clientes indexados.
     *
     * @return DynamicArrayList de clientes
     */
    public DynamicArrayList<Cliente> findAll() {
        return clientes;
    }

    public int count() {
        return clientes.size();
    }

    public boolean isEmpty() {
        return clientes.isEmpty();
    }

    public boolean existsById(String id) {
        return id != null && clientesPorId.containsKey(id);
    }

    /**
     * Busca un cliente por id usando el índice en memoria (HashTable).
     *
     * @param id identificador del cliente
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> findById(String id) {
        if (id == null || !clientesPorId.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(clientesPorId.get(id));
    }

    /**
     * Busca un cliente por email usando el índice de email en memoria.
     *
     * @param email correo electrónico
     * @return Optional con el cliente si existe
     */
    public Optional<Cliente> findByEmail(String email) {
        if (email == null || !clientesPorEmail.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(clientesPorEmail.get(email));
    }

    public Optional<Cliente> findByNombre(String nombre) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getNombre() != null
                    && clientes.get(i).getNombre().equalsIgnoreCase(nombre)) {
                return Optional.of(clientes.get(i));
            }
        }
        return Optional.empty();
    }

    public DynamicArrayList<Cliente> findByTipoCliente(TipoCliente tipo) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getTipoCliente() == tipo) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Cliente> findByZonaInteres(Zona zona) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getZonaInteres() == zona) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Cliente> findByTipoInmuebleDeseado(TipoInmueble tipo) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getTipoInmuebleDeseado() == tipo) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Cliente> findByEstadoBusqueda(EstadoBusquedaCliente estado) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getEstadoBusqueda() == estado) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    /**
     * Consulta clientes cuyo presupuesto está entre min y max. Utiliza el árbol
     * ordenado por presupuesto para eficiencia en búsquedas por rango.
     *
     * @param min mínimo presupuesto
     * @param max máximo presupuesto
     * @return DynamicArrayList de clientes que cumplen el rango
     */
    public DynamicArrayList<Cliente> findByPresupuestoBetween(double min, double max) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        DynamicArrayList<Cliente> ordenados = clientesPorPresupuesto.inOrder();
        for (int i = 0; i < ordenados.size(); i++) {
            Cliente cliente = ordenados.get(i);
            if (cliente.getPresupuesto() != null
                    && cliente.getPresupuesto() >= min
                    && cliente.getPresupuesto() <= max) {
                resultado.add(cliente);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Cliente> findByMinHabitacionesDeseadas(int minHabitaciones) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getNumeroHabitacionesDeseadas() >= minHabitaciones) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    private void cargarDesdeBaseDeDatos() {
        clienteJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Cliente cliente) {
        clientes.add(cliente);
        clientesPorId.put(cliente.getId(), cliente);
        if (cliente.getEmail() != null) {
            clientesPorEmail.put(cliente.getEmail(), cliente);
        }
        clientesPorPresupuesto.insert(cliente);
    }

    private void eliminarDeIndices(Cliente cliente) {
        clientes.remove(cliente);
        clientesPorId.remove(cliente.getId());
        if (cliente.getEmail() != null && clientesPorEmail.containsKey(cliente.getEmail())) {
            clientesPorEmail.remove(cliente.getEmail());
        }
        clientesPorPresupuesto.remove(cliente);
    }

    private void validarId(Cliente cliente) {
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            throw new IllegalArgumentException("El id del cliente no puede estar vacío");
        }
    }

    private int compararPorPresupuestoEId(Cliente c1, Cliente c2) {
        int comparacionPresupuesto = Double.compare(
                c1.getPresupuesto() != null ? c1.getPresupuesto() : 0,
                c2.getPresupuesto() != null ? c2.getPresupuesto() : 0);
        if (comparacionPresupuesto != 0) {
            return comparacionPresupuesto;
        }
        return compararTexto(c1.getId(), c2.getId());
    }

    private int compararTexto(String valor1, String valor2) {
        if (Objects.equals(valor1, valor2)) {
            return 0;
        }
        if (valor1 == null) {
            return -1;
        }
        if (valor2 == null) {
            return 1;
        }
        return valor1.compareTo(valor2);
    }
}
