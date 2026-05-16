package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;

@Repository
public class ContratoRepository {

    private final ContratoJpaRepository contratoJpaRepository;
    private final SinglyLinkedList<Contrato> contratos;


}
