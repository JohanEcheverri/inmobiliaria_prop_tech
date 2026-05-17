package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Alerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.util.List;
import java.util.Optional;

public interface AlertaJpaRepository extends CrudRepository<Alerta, String> {

    List<Alerta> findAllByOrderByFechaGeneracionDesc();

    List<Alerta> findByAtendidaFalseOrderByFechaGeneracionDesc();

    boolean existsByTipoAndReferenciaIdAndAtendidaFalse(TipoAlerta tipo, String referenciaId);

    Optional<Alerta> findFirstByTipoAndReferenciaIdAndAtendidaFalseOrderByFechaGeneracionDesc(
            TipoAlerta tipo,
            String referenciaId);
}
