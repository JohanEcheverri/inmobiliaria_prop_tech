package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ContratoJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;

@Repository
public class ContratoRepository {

    private final ContratoJpaRepository contratoJpaRepository;
    private final SinglyLinkedList<Contrato> contratos;

    public ContratoRepository(ContratoJpaRepository contratoJpaRepository) {
        this.contratoJpaRepository = contratoJpaRepository;
        this.contratos = new SinglyLinkedList<>();
    }
}
