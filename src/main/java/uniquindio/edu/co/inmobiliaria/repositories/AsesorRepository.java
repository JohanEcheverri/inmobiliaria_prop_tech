package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.structures.Tree;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

@Repository
public class AsesorRepository {

    public final Tree<Asesor> numeroDeCierres;

    public final HashTable<String, Asesor> asesoresPorId;

    public AsesorRepository() {
        this.numeroDeCierres = new Tree<>(
                (a1, a2) -> Integer.compare(a2.getNumeroDeCierres(), a1.getNumeroDeCierres()));
        this.asesoresPorId = new HashTable<>();
    }



}
