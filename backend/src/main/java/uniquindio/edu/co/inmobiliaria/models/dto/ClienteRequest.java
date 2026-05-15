package uniquindio.edu.co.inmobiliaria.models.dto;

import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;

public record ClienteRequest(
        String id,
        String nombre,
        String email,
        String telefono,
        String password,
        String fotoPerfil,
        TipoCliente tipoCliente,
        Zona zonaInteres,
        Double presupuesto,
        TipoInmueble tipoInmuebleDeseado,
        int numeroHabitacionesDeseadas,
        EstadoBusquedaCliente estadoBusqueda
) {
}
