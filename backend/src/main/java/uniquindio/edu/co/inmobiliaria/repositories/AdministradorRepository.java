package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AdministradorJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Objects;
import java.util.Optional;

@Repository
public class AdministradorRepository {

    private final AdministradorJpaRepository administradorJpaRepository;
    private final DynamicArrayList<Administrador> administradores;
    private final HashTable<String, Administrador> administradoresPorId;
    private final HashTable<String, Administrador> administradoresPorEmail;
    private final Tree<Administrador> administradoresPorNombre;

   // private final Queue<TareaAdministrativa> tareasPendientes;

    public AdministradorRepository(AdministradorJpaRepository administradorJpaRepository) {
        this.administradorJpaRepository = administradorJpaRepository;
        this.administradores = new DynamicArrayList<>();
        this.administradoresPorId = new HashTable<>();
        this.administradoresPorEmail = new HashTable<>();
        this.administradoresPorNombre = new Tree<>(this::compararPorNombre);
        cargarDesdeBaseDeDatos();
    }

    public void save(Administrador administrador) {
        Objects.requireNonNull(administrador, "El administrador no puede ser nulo");
        validarId(administrador);
        if (administradoresPorId.containsKey(administrador.getId())) {
            throw new IllegalArgumentException("Ya existe un administrador con el id: " + administrador.getId());
        }
        if (administrador.getEmail() != null && administradoresPorEmail.containsKey(administrador.getEmail())) {
            throw new IllegalArgumentException("Ya existe un administrador con el email: " + administrador.getEmail());
        }
        administradorJpaRepository.save(administrador);
        agregarAIndices(administrador);
    }

    public void update(Administrador administradorActualizado) {
        Objects.requireNonNull(administradorActualizado, "El administrador no puede ser nulo");
        validarId(administradorActualizado);
        if (!administradoresPorId.containsKey(administradorActualizado.getId())) {
            throw new IllegalArgumentException("No se encontró un administrador con el id: " + administradorActualizado.getId());
        }
        Administrador anterior = administradoresPorId.get(administradorActualizado.getId());
        if (administradorActualizado.getEmail() != null
                && administradoresPorEmail.containsKey(administradorActualizado.getEmail())
                && !Objects.equals(anterior.getId(), administradoresPorEmail.get(administradorActualizado.getEmail()).getId())) {
            throw new IllegalArgumentException("Ya existe un administrador con el email: " + administradorActualizado.getEmail());
        }
        administradorJpaRepository.save(administradorActualizado);
        eliminarDeIndices(anterior);
        agregarAIndices(administradorActualizado);
    }

    public Optional<Administrador> findById(String id) {
        if (id == null || !administradoresPorId.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(administradoresPorId.get(id));
    }

    public Optional<Administrador> findByEmail(String email) {
        if (email == null || !administradoresPorEmail.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(administradoresPorEmail.get(email));
    }

    public DynamicArrayList<Administrador> findAll() {
        return administradores;
    }

    public boolean existsById(String id) {
        return id != null && administradoresPorId.containsKey(id);
    }

    public boolean deleteById(String id) {
        Optional<Administrador> administrador = findById(id);
        if (administrador.isEmpty()) {
            return false;
        }
        administradorJpaRepository.deleteById(id);
        eliminarDeIndices(administrador.get());
        return true;
    }

    private void cargarDesdeBaseDeDatos() {
        administradorJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Administrador administrador) {
        administradores.add(administrador);
        administradoresPorId.put(administrador.getId(), administrador);
        if (administrador.getEmail() != null) {
            administradoresPorEmail.put(administrador.getEmail(), administrador);
        }
        administradoresPorNombre.insert(administrador);
    }

    private void eliminarDeIndices(Administrador administrador) {
        administradores.remove(administrador);
        administradoresPorId.remove(administrador.getId());
        if (administrador.getEmail() != null && administradoresPorEmail.containsKey(administrador.getEmail())) {
            administradoresPorEmail.remove(administrador.getEmail());
        }
        administradoresPorNombre.remove(administrador);
    }

    private void validarId(Administrador administrador) {
        if (administrador.getId() == null || administrador.getId().isBlank()) {
            throw new IllegalArgumentException("El id del administrador no puede estar vacío");
        }
    }

    private int compararPorNombre(Administrador a1, Administrador a2) {
        if (Objects.equals(a1.getNombre(), a2.getNombre())) {
            return 0;
        }
        if (a1.getNombre() == null) {
            return -1;
        }
        if (a2.getNombre() == null) {
            return 1;
        }
        return a1.getNombre().compareToIgnoreCase(a2.getNombre());
    }
}
