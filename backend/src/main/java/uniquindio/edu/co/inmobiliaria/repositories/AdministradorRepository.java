package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AdministradorJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

@Repository
public class AdministradorRepository {

    private final AdministradorJpaRepository administradorJpaRepository;
    private final HashTable<String, Administrador> administradoresPorId;

    public AdministradorRepository(AdministradorJpaRepository administradorJpaRepository) {
        this.administradorJpaRepository = administradorJpaRepository;
        this.administradoresPorId = new HashTable<>();
        cargarDesdeBaseDeDatos();
    }

    public void save(Administrador administrador) {
        if (administrador == null || administrador.getId() == null || administrador.getId().isBlank()) {
            throw new IllegalArgumentException("El administrador o su id no pueden ser nulos");
        }
        if (administradoresPorId.containsKey(administrador.getId())) {
            throw new IllegalArgumentException("Ya existe un administrador con el id: " + administrador.getId());
        }
        administradorJpaRepository.save(administrador);
        agregarAIndices(administrador);
    }

    public Administrador findById(String id) {
        if (id == null || !administradoresPorId.containsKey(id)) {
            return null;
        }
        return administradoresPorId.get(id);
    }

    private void cargarDesdeBaseDeDatos() {
        administradorJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Administrador administrador) {
        administradoresPorId.put(administrador.getId(), administrador);
    }
}
