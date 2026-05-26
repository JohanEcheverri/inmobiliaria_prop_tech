package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
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
