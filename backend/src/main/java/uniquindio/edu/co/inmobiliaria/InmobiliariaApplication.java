package uniquindio.edu.co.inmobiliaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Clase principal de arranque para la aplicación Spring Boot de la Inmobiliaria.
 * Inicializa el contexto de la aplicación, los repositorios de datos y los controladores REST.
 */
public class InmobiliariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(InmobiliariaApplication.class, args);
	}

}
