package uniquindio.edu.co.inmobiliaria.repositories.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;

import java.time.LocalDate;
import java.util.List;

public interface VisitaJpaRepository extends CrudRepository<Visita, String> {

    @Query("""
            SELECT v FROM Visita v
            LEFT JOIN FETCH v.cliente
            LEFT JOIN FETCH v.inmueble
            LEFT JOIN FETCH v.asesotAsignado
            """)
    List<Visita> findAllConRelaciones();

    List<Visita> findByCliente_Id(String clienteId);

    List<Visita> findByInmueble_Codigo(String inmuebleCodigo);

    List<Visita> findByAsesotAsignado_Id(String asesorId);

    List<Visita> findByFecha(LocalDate fecha);

    List<Visita> findByEstado(EstadoVisita estado);
}
