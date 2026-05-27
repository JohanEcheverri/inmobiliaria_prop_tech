package uniquindio.edu.co.inmobiliaria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaMonitor;
import uniquindio.edu.co.inmobiliaria.comportamiento.ComportamientoService;
import uniquindio.edu.co.inmobiliaria.comportamiento.HistorialPrecio;
import uniquindio.edu.co.inmobiliaria.comportamiento.HistorialPrecioRepository;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteRequest;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.entities.Ciudad;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.Estado;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import uniquindio.edu.co.inmobiliaria.models.enums.Finalidad;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoContrato;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.AdministradorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.OperacionRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ContratoJpaRepository;
import uniquindio.edu.co.inmobiliaria.services.AsesorService;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
/**
 * Clase encargada de poblar la base de datos con datos de prueba o iniciales (semilla).
 * Se ejecuta al arrancar la aplicación e inyecta clientes, asesores, administradores,
 * inmuebles, operaciones y visitas para permitir la validación inmediata del sistema.
 */
public class DataSeeder {

    @Bean
    CommandLineRunner seedLoginUsers(ClienteRepository clienteRepository,
                                     ClienteService clienteService,
                                     AsesorRepository asesorRepository,
                                     AsesorService asesorService,
                                     AdministradorRepository administradorRepository,
                                     PasswordEncoder passwordEncoder,
                                     InmuebleRepository inmuebleRepository,
                                     VisitasRepository visitasRepository,
                                     OperacionRepository operacionRepository,
                                     ContratoJpaRepository contratoJpaRepository,
                                     HistorialPrecioRepository historialPrecioRepository,
                                     ComportamientoService comportamientoService,
                                     AlertaMonitor alertaMonitor) {
        return args -> {
            if (clienteRepository.findById("3030789").isEmpty()) {
                clienteService.registrarCliente(new ClienteRequest(
                        "3030789",
                        "Juan Cliente",
                        "juan.cliente@inmobiliaria.local",
                        "3001234567",
                        "cliente123",
                        null,
                        TipoCliente.COMPRADOR,
                        Zona.CENTRO,
                        250000000.0,
                        TipoInmueble.APARTAMENTO,
                        3,
                        EstadoBusquedaCliente.BUSCANDO
                ));
            }

            if (clienteRepository.findById("3030790").isEmpty()) {
                clienteService.registrarCliente(new ClienteRequest(
                        "3030790",
                        "Laura Cliente Demo",
                        "laura.demo@inmobiliaria.local",
                        "3012223344",
                        "cliente123",
                        null,
                        TipoCliente.COMPRADOR,
                        Zona.NORTE,
                        430000000.0,
                        TipoInmueble.CASA,
                        3,
                        EstadoBusquedaCliente.BUSCANDO
                ));
            }

            if (asesorRepository.findById("10949001").isEmpty()) {
                asesorService.registrarAsesor(new Asesor(
                        "Ana Asesora",
                        "10949001",
                        "ana.asesora@inmobiliaria.local",
                        "3009876543",
                        "asesor123",
                        null,
                        Zona.NORTE,
                        TipoInmueble.CASA
                ));
            }

            if (asesorRepository.findById("10949002").isEmpty()) {
                asesorService.registrarAsesor(new Asesor(
                        "Carlos Reportes",
                        "10949002",
                        "carlos.reportes@inmobiliaria.local",
                        "3015556677",
                        "asesor123",
                        null,
                        Zona.SUR,
                        TipoInmueble.APARTAMENTO
                ));
            }

            if (administradorRepository.findById("10000001").isEmpty()) {
                administradorRepository.save(new Administrador(
                        "Admin DomusTech",
                        "10000001",
                        "admin@inmobiliaria.local",
                        "3000000000",
                        passwordEncoder.encode("admin123"),
                        null
                ));
            }

            seedAdminDemoData(
                    clienteRepository,
                    asesorRepository,
                    inmuebleRepository,
                    visitasRepository,
                    operacionRepository,
                    contratoJpaRepository,
                    historialPrecioRepository
            );

            comportamientoService.analizarComportamientoAtipico();
            alertaMonitor.verificarTodo();
        };
    }

    private void seedAdminDemoData(ClienteRepository clienteRepository,
                                   AsesorRepository asesorRepository,
                                   InmuebleRepository inmuebleRepository,
                                   VisitasRepository visitasRepository,
                                   OperacionRepository operacionRepository,
                                   ContratoJpaRepository contratoJpaRepository,
                                   HistorialPrecioRepository historialPrecioRepository) {
        if (inmuebleRepository.existsById("DT-ALR-001")) {
            return;
        }

        Cliente clienteBase = clienteRepository.findById("3030789")
                .orElseThrow(() -> new IllegalStateException("No existe el cliente base para datos demo"));
        Cliente clienteDemo = clienteRepository.findById("3030790")
                .orElseThrow(() -> new IllegalStateException("No existe el cliente demo para datos demo"));
        Asesor ana = asesorRepository.findById("10949001")
                .orElseThrow(() -> new IllegalStateException("No existe la asesora base para datos demo"));
        Asesor carlos = asesorRepository.findById("10949002")
                .orElseThrow(() -> new IllegalStateException("No existe el asesor demo para datos demo"));

        Ciudad armenia = new Ciudad("Armenia", "Quindio");
        Inmueble alerta = inmueble("DT-ALR-001", "La Castellana", armenia, Zona.NORTE, TipoInmueble.CASA,
                Finalidad.VENTA, 420000000, 138, 4, 3, Estado.DISPONIBLE, ana);
        Inmueble cierre = inmueble("DT-REP-001", "Providencia", armenia, Zona.NORTE, TipoInmueble.APARTAMENTO,
                Finalidad.VENTA, 260000000, 82, 3, 2, Estado.VENDIDO, ana);
        Inmueble reservado = inmueble("DT-REP-002", "El Prado", armenia, Zona.SUR, TipoInmueble.CASA,
                Finalidad.VENTA, 310000000, 110, 3, 2, Estado.RESERVADO, carlos);
        Inmueble proceso = inmueble("DT-REP-003", "Granada", armenia, Zona.CENTRO, TipoInmueble.LOCAL_COMERCIAL,
                Finalidad.ARRENDAMIENTO, 4800000, 64, 0, 1, Estado.DISPONIBLE, carlos);

        List.of(alerta, cierre, reservado, proceso).forEach(inmuebleRepository::save);

        for (int i = 1; i <= 5; i++) {
            inmuebleRepository.save(inmueble(
                    "DT-SOB-00" + i,
                    "Norte Demo " + i,
                    armenia,
                    Zona.NORTE,
                    i % 2 == 0 ? TipoInmueble.APARTAMENTO : TipoInmueble.CASA,
                    Finalidad.VENTA,
                    210000000 + (i * 15000000),
                    70 + (i * 8),
                    2 + (i % 3),
                    2,
                    Estado.DISPONIBLE,
                    ana
            ));
        }

        for (int i = 1; i <= 10; i++) {
            visitasRepository.save(visita(
                    "VIS-DEMO-ALR-" + i,
                    i <= 3 ? clienteDemo : clienteBase,
                    alerta,
                    LocalDate.now().minusDays(i % 6),
                    LocalTime.of(9 + (i % 8), 0),
                    EstadoVisita.REALIZADA,
                    ana,
                    "Visita realizada de prueba para alertas y comportamiento"
            ));
        }

        visitasRepository.save(visita("VIS-DEMO-OLD-1", clienteBase, proceso, LocalDate.now().minusDays(45),
                LocalTime.of(10, 30), EstadoVisita.REALIZADA, carlos, "Ultima visita antigua para alerta de seguimiento"));
        visitasRepository.save(visita("VIS-DEMO-PEND-1", clienteDemo, reservado, LocalDate.now().minusDays(3),
                LocalTime.of(14, 0), EstadoVisita.PENDIENTE, carlos, "Visita pendiente por confirmar"));

        for (int i = 1; i <= 8; i++) {
            visitasRepository.save(visita(
                    "VIS-DEMO-SOB-" + i,
                    clienteBase,
                    inmuebleRepository.findByCodigo("DT-SOB-00" + ((i % 5) + 1)),
                    LocalDate.now().plusDays(i),
                    LocalTime.of(8 + (i % 9), 30),
                    i % 2 == 0 ? EstadoVisita.CONFIRMADA : EstadoVisita.PENDIENTE,
                    ana,
                    "Agenda activa para validar sobrecarga del asesor"
            ));
        }

        Venta ventaCerrada = venta("OP-DEMO-001", cierre, clienteBase, ana, LocalDateTime.now().minusDays(12),
                EstadoOperacion.COMPLETADA, 255000000, 7650000);
        Venta ventaReservada = venta("OP-DEMO-002", reservado, clienteDemo, carlos, LocalDateTime.now().minusDays(25),
                EstadoOperacion.EN_PROCESO, 305000000, 9150000);
        Venta operacionProceso = venta("OP-DEMO-003", proceso, clienteDemo, carlos, LocalDateTime.now().minusDays(5),
                EstadoOperacion.EN_PROCESO, 4800000, 480000);

        List.of(ventaCerrada, ventaReservada, operacionProceso).forEach(operacionRepository::save);

        contratoJpaRepository.save(new Contrato(
                "CTR-DEMO-001",
                ventaCerrada,
                "Contrato demo proximo a vencer para validar alertas administrativas.",
                LocalDateTime.now().minusMonths(11),
                LocalDateTime.now().plusDays(10),
                true,
                TipoContrato.VENTA
        ));

        historialPrecioRepository.save(HistorialPrecio.builder()
                .codigoInmueble(alerta.getCodigo())
                .precioAnterior(450000000)
                .precioNuevo(440000000)
                .fechaCambio(LocalDateTime.now().minusDays(20))
                .build());
        historialPrecioRepository.save(HistorialPrecio.builder()
                .codigoInmueble(alerta.getCodigo())
                .precioAnterior(440000000)
                .precioNuevo(430000000)
                .fechaCambio(LocalDateTime.now().minusDays(12))
                .build());
        historialPrecioRepository.save(HistorialPrecio.builder()
                .codigoInmueble(alerta.getCodigo())
                .precioAnterior(430000000)
                .precioNuevo(420000000)
                .fechaCambio(LocalDateTime.now().minusDays(4))
                .build());
    }

    private Inmueble inmueble(String codigo,
                              String barrio,
                              Ciudad ciudad,
                              Zona zona,
                              TipoInmueble tipo,
                              Finalidad finalidad,
                              double precio,
                              double area,
                              int habitaciones,
                              int banios,
                              Estado estado,
                              Asesor asesor) {
        return Inmueble.builder()
                .codigo(codigo)
                .direccionBarrio(barrio)
                .ciudad(ciudad)
                .zona(zona)
                .tipoInmueble(tipo)
                .finalidad(finalidad)
                .precio(precio)
                .area(area)
                .numeroHabitaciones(habitaciones)
                .numeroBanios(banios)
                .estado(estado)
                .asesor(asesor)
                .imagen(List.of())
                .build();
    }

    private Visita visita(String codigo,
                          Cliente cliente,
                          Inmueble inmueble,
                          LocalDate fecha,
                          LocalTime hora,
                          EstadoVisita estado,
                          Asesor asesor,
                          String observaciones) {
        return new Visita(codigo, cliente, inmueble, fecha, hora, estado, asesor, observaciones);
    }

    private Venta venta(String codigo,
                        Inmueble inmueble,
                        Cliente cliente,
                        Asesor asesor,
                        LocalDateTime fecha,
                        EstadoOperacion estado,
                        double valorAcordado,
                        double comision) {
        return Venta.builder()
                .codigo(codigo)
                .inmueble(inmueble)
                .cliente(cliente)
                .asesor(asesor)
                .fecha(fecha)
                .estado(estado)
                .valorAcordado(valorAcordado)
                .comision(comision)
                .build();
    }
}
