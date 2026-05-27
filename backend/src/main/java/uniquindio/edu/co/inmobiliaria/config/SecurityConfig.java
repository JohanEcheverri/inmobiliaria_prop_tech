package uniquindio.edu.co.inmobiliaria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
/**
 * Configuración auxiliar de seguridad encargada de exponer beans específicos,
 * como el codificador de contraseñas (BCryptPasswordEncoder) utilizado para
 * proteger las credenciales de los usuarios en la base de datos.
 */
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
