package uniquindio.edu.co.inmobiliaria.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Converter
/**
 * Conversor JPA para transformar listas de cadenas (String) en una única cadena Base64
 * separada por saltos de línea para su persistencia en base de datos relacional.
 * Útil para campos multivaluados como fotos o amenidades de un inmueble.
 */
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        List<String> encoded = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                encoded.add(Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)));
            }
        }
        return String.join("\n", encoded);
    }

    @Override
    public List<String> convertToEntityAttribute(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String item : value.split("\\R")) {
            if (item == null || item.isBlank()) {
                continue;
            }
            try {
                result.add(new String(Base64.getDecoder().decode(item), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException exception) {
                result.add(item);
            }
        }
        return result;
    }
}
