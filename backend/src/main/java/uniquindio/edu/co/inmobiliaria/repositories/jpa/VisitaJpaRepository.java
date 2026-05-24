package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;

import java.time.LocalDate;
import java.util.List;

public interface VisitaJpaRepository extends CrudRepository<Visita, String> {

    List<Visita> findByCliente_Id(String clienteId);

    List<Visita> findByInmueble_Codigo(String inmuebleCodigo);

    List<Visita> findByAsesotAsignado_Id(String asesorId);

    List<Visita> findByFecha(LocalDate fecha);

    List<Visita> findByEstado(EstadoVisita estado);
}
