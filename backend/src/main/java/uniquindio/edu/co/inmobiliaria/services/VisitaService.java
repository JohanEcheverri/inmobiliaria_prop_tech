package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;

@Service
public class VisitaService {

    private final VisitasRepository visitasRepository;

    public VisitaService(VisitasRepository visitasRepository) {
        this.visitasRepository = visitasRepository;
    }
}

