package uniquindio.edu.co.inmobiliaria.repositories;

import lombok.Getter;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AsesorJpaRepository;
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


}