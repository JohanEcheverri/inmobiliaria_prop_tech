package uniquindio.edu.co.inmobiliaria.repositories;

import lombok.Getter;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AsesorJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Optional;

@Repository
public class AsesorRepository {

    @Getter
    public final Tree<Asesor> numeroDeCierres = new Tree<>(
            (a1, a2) -> Integer.compare(a2.getNumeroDeCierres(), a1.getNumeroDeCierres())
    );
    @Getter
    public final HashTable<String, Asesor> asesoresPorId = new HashTable<>();
    @Getter
    public final HashTable<String, Asesor> asesoresPorEmail = new HashTable<>();

    private final AsesorJpaRepository asesorJpaRepository;

    public AsesorRepository(AsesorJpaRepository asesorJpaRepository) {
        this.asesorJpaRepository = asesorJpaRepository;
        this.numeroDeCierres = new Tree<>(
                this::compararPorCierresEId);
        this.asesoresPorId = new HashTable<>();
        this.asesoresPorEmail = new HashTable<>();
    }

    @jakarta.annotation.PostConstruct
    public void inicializarDatos() {
        cargarDesdeBaseDeDatos();
    }
    private void cargarDesdeBaseDeDatos() {
        // Tu bucle de carga normal...
        for (Asesor asesor : asesorJpaRepository.findAll()) {
            if (asesor != null && asesor.getId() != null) {
                asesoresPorId.put(asesor.getId(), asesor);
                if (asesor.getEmail() != null) {
                    asesoresPorEmail.put(asesor.getEmail(), asesor);
                }
                numeroDeCierres.insert(asesor);
            }
        }
    }

    public void save(Asesor asesor) {
        if (asesor == null || asesor.getId() == null) {
            throw new IllegalArgumentException("El asesor o su id no pueden ser nulos");
        }
        asesorJpaRepository.save(asesor);
        agregarAIndices(asesor);
    }

    public Optional<Asesor> findById(String id) {
        if (id == null || !asesoresPorId.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(asesoresPorId.get(id));
    }

    public Optional<Asesor> findByEmail(String email) {
        if (email == null || !asesoresPorEmail.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(asesoresPorEmail.get(email));
    }

    public DynamicArrayList<Asesor> findAll() {
        return asesoresPorId.values();
    }

    public void update(Asesor asesorActualizado) {
        if (asesorActualizado == null || asesorActualizado.getId() == null) {
            throw new IllegalArgumentException("El asesor o su id no pueden ser nulos");
        }
        if (!asesoresPorId.containsKey(asesorActualizado.getId())) {
            throw new IllegalArgumentException("No se encontró un asesor con el id: " + asesorActualizado.getId());
        }
        Asesor anterior = asesoresPorId.get(asesorActualizado.getId());
        asesorJpaRepository.save(asesorActualizado);
        eliminarDeIndices(anterior);
        agregarAIndices(asesorActualizado);
    }

    public boolean deleteById(String id) {
        if (id == null || !asesoresPorId.containsKey(id)) {
            return false;
        }
        Asesor asesor = asesoresPorId.get(id);
        asesorJpaRepository.deleteById(id);
        eliminarDeIndices(asesor);
        return true;
    }

    private void cargarDesdeBaseDeDatos() {
        asesorJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Asesor asesor) {
    public void agregarAIndices(Asesor asesor) {
        asesoresPorId.put(asesor.getId(), asesor);
        if (asesor.getEmail() != null) {
            asesoresPorEmail.put(asesor.getEmail(), asesor);
        }
        numeroDeCierres.insert(asesor);
    }

    public void deleteById(String id) {
        if (id != null && asesoresPorId.containsKey(id)) {
            Asesor asesor = asesoresPorId.get(id);
            asesoresPorId.remove(id);
            if (asesor.getEmail() != null) {
                asesoresPorEmail.remove(asesor.getEmail());
            }
            asesorJpaRepository.deleteById(id);
        }
    }

    private void eliminarDeIndices(Asesor asesor) {
        asesoresPorId.remove(asesor.getId());
        if (asesor.getEmail() != null && asesoresPorEmail.containsKey(asesor.getEmail())) {
            asesoresPorEmail.remove(asesor.getEmail());
        }
        numeroDeCierres.remove(asesor);
    }

    private int compararPorCierresEId(Asesor a1, Asesor a2) {
        int comparacionCierres = Integer.compare(a2.getNumeroDeCierres(), a1.getNumeroDeCierres());
        if (comparacionCierres != 0) {
            return comparacionCierres;
        }
        if (a1.getId() == null && a2.getId() == null) {
            return 0;
        }
        if (a1.getId() == null) {
            return -1;
        }
        if (a2.getId() == null) {
            return 1;
        }
        return a1.getId().compareTo(a2.getId());
    }
}


}