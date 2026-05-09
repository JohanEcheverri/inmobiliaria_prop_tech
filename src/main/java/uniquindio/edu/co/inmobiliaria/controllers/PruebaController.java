package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class PruebaController {

    @GetMapping("/prueba1")
    public String prueba1(Model model){
        model.addAttribute("mensaje", "Hola uwu mundo");
        return "prueba";
    }

}
