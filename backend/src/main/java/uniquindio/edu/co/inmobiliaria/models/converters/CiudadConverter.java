package uniquindio.edu.co.inmobiliaria.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;

@Converter
/**
 * Conversor JPA para persistir el objeto de valor {@link Ciudad} como una cadena
 * simple separada por un pipe '|' (ej. "Armenia|Quindío") en la base de datos,
 * y viceversa.
 */
public class CiudadConverter implements AttributeConverter<Ciudad, String> {

    private static final String SEPARATOR = "|";

    @Override
    public String convertToDatabaseColumn(Ciudad ciudad) {
        if (ciudad == null) {
            return null;
        }
        String nombre = ciudad.getNombre() != null ? ciudad.getNombre() : "";
        String departamento = ciudad.getDepartamento() != null ? ciudad.getDepartamento() : "";
        return nombre + SEPARATOR + departamento;
    }

    @Override
    public Ciudad convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.split("\\|", 2);
        String nombre = parts.length > 0 ? parts[0] : "";
        String departamento = parts.length > 1 ? parts[1] : "";
        return new Ciudad(nombre, departamento);
    }
}
