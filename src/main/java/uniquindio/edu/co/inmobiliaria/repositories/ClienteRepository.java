package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio en memoria para la entidad {@link Cliente}.
 * <p>
 * Utiliza {@link DynamicArrayList} como estructura de almacenamiento.
 * Anotado con {@code @Repository} para que Spring lo gestione como bean
 * e inyectarlo donde se necesite.
 * </p>
 */
@Repository
public class ClienteRepository {

    private final DynamicArrayList<Cliente> clientes;

    private final Tree<Cliente> clientesPorPresupuesto;
    // ─────────────────────────────── CRUD ───────────────────────────────

    public ClienteRepository() {
        this.clientes = new DynamicArrayList<>();
        this.clientesPorPresupuesto = new Tree<>(
                (c1, c2) -> Double.compare(c1.getPresupuesto(), c2.getPresupuesto()));
    }

    /**
     * Guarda un nuevo cliente en el repositorio.
     *
     * @param cliente entidad a guardar
     * @throws IllegalArgumentException si el cliente es nulo o ya existe uno con el mismo email
     */
    public void save(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser nulo");
        if (cliente.getEmail() != null && findByEmail(cliente.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el email: " + cliente.getEmail());
        }
        clientes.add(cliente);
    }

    /**
     * Actualiza los datos de un cliente existente identificado por su email.
     *
     * @param clienteActualizado datos nuevos del cliente
     * @throws IllegalArgumentException si no se encuentra un cliente con ese email
     */
    public void update(Cliente clienteActualizado) {
        Objects.requireNonNull(clienteActualizado, "El cliente no puede ser nulo");
        for (int i = 0; i < clientes.size(); i++) {
            if (Objects.equals(clientes.get(i).getEmail(), clienteActualizado.getEmail())) {
                clientes.set(i, clienteActualizado);
                return;
            }
        }
        throw new IllegalArgumentException(
                "No se encontró un cliente con el email: " + clienteActualizado.getEmail());
    }

    /**
     * Elimina un cliente por su email.
     *
     * @param email correo del cliente a eliminar
     * @return {@code true} si se eliminó, {@code false} si no existía
     */
    public boolean deleteByEmail(String email) {
        for (int i = 0; i < clientes.size(); i++) {
            if (Objects.equals(clientes.get(i).getEmail(), email)) {
                clientes.removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Retorna todos los clientes almacenados.
     *
     * @return lista dinámica con todos los clientes
     */
    public DynamicArrayList<Cliente> findAll() {
        return clientes;
    }

    /**
     * Indica la cantidad de clientes registrados.
     *
     * @return número de clientes
     */
    public int count() {
        return clientes.size();
    }

    /**
     * Verifica si el repositorio está vacío.
     *
     * @return {@code true} si no hay clientes
     */
    public boolean isEmpty() {
        return clientes.isEmpty();
    }

    // ──────────────────────── BÚSQUEDAS PUNTUALES ────────────────────────

    /**
     * Busca un cliente por su correo electrónico (identificador único).
     *
     * @param email correo a buscar
     * @return {@link Optional} con el cliente encontrado, o vacío
     */
    public Optional<Cliente> findByEmail(String email) {
        for (int i = 0; i < clientes.size(); i++) {
            if (Objects.equals(clientes.get(i).getEmail(), email)) {
                return Optional.of(clientes.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Busca un cliente por su nombre (coincidencia exacta, case-insensitive).
     *
     * @param nombre nombre a buscar
     * @return {@link Optional} con el primer cliente que coincida
     */
    public Optional<Cliente> findByNombre(String nombre) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getNombre() != null
                    && clientes.get(i).getNombre().equalsIgnoreCase(nombre)) {
                return Optional.of(clientes.get(i));
            }
        }
        return Optional.empty();
    }

    // ──────────────────────── BÚSQUEDAS POR FILTRO ────────────────────────

    /**
     * Filtra clientes por tipo (COMPRADOR / ARRENDATARIO).
     *
     * @param tipo tipo de cliente
     * @return lista con los clientes que coinciden
     */
    public DynamicArrayList<Cliente> findByTipoCliente(TipoCliente tipo) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getTipoCliente() == tipo) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    /**
     * Filtra clientes por zona de interés.
     *
     * @param zona zona geográfica
     * @return lista con los clientes de esa zona
     */
    public DynamicArrayList<Cliente> findByZonaInteres(Zona zona) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getZonaInteres() == zona) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    /**
     * Filtra clientes por tipo de inmueble deseado.
     *
     * @param tipo tipo de inmueble
     * @return lista con los clientes que buscan ese tipo
     */
    public DynamicArrayList<Cliente> findByTipoInmuebleDeseado(TipoInmueble tipo) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getTipoInmuebleDeseado() == tipo) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }

    /**
     * Filtra clientes por estado de búsqueda.
     *
     * @param estado estado actual de la búsqueda
     * @return lista con los clientes en ese estado
     */
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
     * Filtra clientes cuyo presupuesto se encuentra dentro de un rango.
     *
     * @param min presupuesto mínimo (inclusive)
     * @param max presupuesto máximo (inclusive)
     * @return lista con los clientes dentro del rango
     */
    public DynamicArrayList<Cliente> findByPresupuestoBetween(double min, double max) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            if (c.getPresupuesto() != null
                    && c.getPresupuesto() >= min
                    && c.getPresupuesto() <= max) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /**
     * Filtra clientes que buscan al menos cierta cantidad de habitaciones.
     *
     * @param minHabitaciones número mínimo de habitaciones deseadas
     * @return lista con los clientes que cumplen el criterio
     */
    public DynamicArrayList<Cliente> findByMinHabitacionesDeseadas(int minHabitaciones) {
        DynamicArrayList<Cliente> resultado = new DynamicArrayList<>();
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getNumeroHabitacionesDeseadas() >= minHabitaciones) {
                resultado.add(clientes.get(i));
            }
        }
        return resultado;
    }
}
