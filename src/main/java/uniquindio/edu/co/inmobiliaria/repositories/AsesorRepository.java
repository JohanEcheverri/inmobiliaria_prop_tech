package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AsesorJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Optional;

@Repository
public class AsesorRepository {

    public final Tree<Asesor> numeroDeCierres;
    public final HashTable<String, Asesor> asesoresPorId;
    public final HashTable<String, Asesor> asesoresPorEmail;
    private final AsesorJpaRepository asesorJpaRepository;

    public AsesorRepository(AsesorJpaRepository asesorJpaRepository) {
        this.asesorJpaRepository = asesorJpaRepository;
        this.numeroDeCierres = new Tree<>(
                (a1, a2) -> Integer.compare(a2.getNumeroDeCierres(), a1.getNumeroDeCierres()));
        this.asesoresPorId = new HashTable<>();
        this.asesoresPorEmail = new HashTable<>();
        cargarDesdeBaseDeDatos();
    }

    public void save(Asesor asesor) {
        if (asesor == null || asesor.getId() == null) {
            throw new IllegalArgumentException("El asesor o su id no pueden ser nulos");
        }
        if (asesoresPorId.containsKey(asesor.getId())) {
            throw new IllegalArgumentException("Ya existe un asesor con el id: " + asesor.getId());
        }
        if (asesor.getEmail() != null && asesoresPorEmail.containsKey(asesor.getEmail())) {
            throw new IllegalArgumentException("Ya existe un asesor con el email: " + asesor.getEmail());
        }
        asesorJpaRepository.save(asesor);
        agregarAIndices(asesor);
    }

    public Asesor findById(String id) {
        if (id == null || !asesoresPorId.containsKey(id)) {
            return null;
        }
        return asesoresPorId.get(id);
    }

    public Optional<Asesor> findByEmail(String email) {
        if (email == null || !asesoresPorEmail.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(asesoresPorEmail.get(email));
    }

    private void cargarDesdeBaseDeDatos() {
        asesorJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Asesor asesor) {
        asesoresPorId.put(asesor.getId(), asesor);
        if (asesor.getEmail() != null) {
            asesoresPorEmail.put(asesor.getEmail(), asesor);
        }
        numeroDeCierres.insert(asesor);
    }
}
