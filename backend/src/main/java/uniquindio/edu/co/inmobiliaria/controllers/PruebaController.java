package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
/**
 * Controlador de prueba para verificar la configuración básica de Spring MVC y Thymeleaf.
 * Exclusivamente para desarrollo y validación de plantillas iniciales.
 */
public class PruebaController {

    /**
     * Controlador de ejemplo que carga una vista 'prueba' con un mensaje simple.
     * Usado para verificar integración MVC/Thymeleaf en la aplicación.
     *
     * @param model modelo de la vista al que se añade el atributo 'mensaje'
     * @return nombre de la plantilla a renderizar
     */
    @GetMapping("/prueba1")
    public String prueba1(Model model){
        model.addAttribute("mensaje", "Hola uwu mundo");
        return "prueba";
    }

}
