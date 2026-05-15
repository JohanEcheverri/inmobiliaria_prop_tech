# Documentación del proyecto Inmobiliaria

Aplicación Spring Boot para gestión inmobiliaria. El núcleo del dominio vive en `uniquindio.edu.co.inmobiliaria.models`, separado en **entities** (clases de datos del negocio) y **enums** (valores fijos tipados).

---

## Visión general del proyecto

| Aspecto | Detalle |
|--------|---------|
| **Empaquetado base** | `uniquindio.edu.co.inmobiliaria` |
| **Java** | 21 |
| **Spring Boot** | 4.0.5 |
| **Dependencias principales** | Web MVC, Thymeleaf, Lombok |
| **Persistencia** | Las entidades son POJOs; aún no hay JPA/Hibernate configurado en el código revisado |
| **Punto de entrada** | `InmobiliariaApplication` |

El archivo `application.properties` solo define el nombre de la aplicación (`spring.application.name=inmobiliaria`). No hay controladores ni vistas Thymeleaf en el repositorio en el estado actual; el foco está en el **modelo de dominio**.

---

## Paquetes relevantes

```
src/main/java/uniquindio/edu/co/inmobiliaria/
├── InmobiliariaApplication.java
└── models/
    ├── entities/     # Entidades del dominio
    └── enums/        # Enumeraciones usadas por las entidades
```

---

## Entidades (`models.entities`)

Todas usan **Lombok** (`@Getter`, `@Setter`, `@ToString`, constructores) salvo donde se indica. Varias jerarquías usan `@SuperBuilder` para construcción encadenada en subclases.

### Usuarios y sesión

#### `Usuario`

Clase base de identidad en el sistema. No está marcada como abstracta.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `nombre` | `String` | Nombre del usuario |
| `email` | `String` | Correo (típico identificador de login) |
| `telefono` | `String` | Teléfono de contacto |
| `password` | `String` | Contraseña (en producción debería almacenarse de forma segura, no en texto plano) |
| `fotoPerfil` | `String` | Referencia a imagen de perfil (URL o ruta, según implementación futura) |

#### `Cliente` extends `Usuario`

Representa un cliente de la inmobiliaria.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `tipoCliente` | `TipoCliente` | Comprador o arrendatario |
| `zonaInteres` | `Zona` | Zona geográfica de interés |
| `presupuesto` | `Double` | Presupuesto aproximado |
| `tipoInmuebleDeseado` | `TipoInmueble` | Tipo de inmueble buscado |
| `numeroHabitacionesDeseadas` | `int` | Habitaciones deseadas |
| `estadoBusqueda` | `EstadoBusquedaCliente` | Etapa de la búsqueda del cliente |

#### `Asesor` extends `Usuario`

Representa un asesor inmobiliario.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `zonaAsignada` | `Zona` | Zona de trabajo asignada |
| `especialidad` | `TipoInmueble` | Tipo de inmueble de especialidad |

*Nota:* En esta clase los campos `zonaAsignada` y `especialidad` están declarados como `public` (inconsistente con el resto de entidades que usan `private` + Lombok).

#### `Sesion`

Representa una **sesión de acceso** a la aplicación para un cliente o un asesor (por ejemplo, tras el login). No sustituye la sesión HTTP de Spring por sí sola; es el modelo de dominio que puedes persistir o asociar a un identificador de sesión.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `idSesion` | `String` | Identificador único (se genera con UUID en los factories) |
| `tipoSesion` | `TipoSesion` | `CLIENTE` o `ASESOR` |
| `usuario` | `Usuario` | Instancia concreta (`Cliente` o `Asesor`) |
| `fechaInicio` | `LocalDateTime` | Inicio de la sesión |
| `fechaUltimaActividad` | `LocalDateTime` | Última actividad registrada |
| `fechaExpiracion` | `LocalDateTime` | Caducidad; puede ser `null` (entonces `estaExpirada()` devuelve `false`) |
| `direccionIp` | `String` | IP del cliente (auditoría / seguridad) |
| `userAgent` | `String` | Navegador o cliente HTTP |

**Métodos estáticos**

- `crearParaCliente(Cliente, LocalDateTime fechaExpiracion, String ip, String userAgent)`
- `crearParaAsesor(Asesor, LocalDateTime fechaExpiracion, String ip, String userAgent)`

**Métodos de instancia**

- `registrarActividad()` — actualiza `fechaUltimaActividad` a “ahora”.
- `estaExpirada()` — `true` si hay `fechaExpiracion` y ya pasó.
- `esCliente()` / `esAsesor()` — según `tipoSesion`.
- `getCliente()` / `getAsesor()` — devuelven el `usuario` convertido o `null` si el tipo no coincide.

---

### Ubicación geográfica

#### `Ciudad`

| Campo | Tipo |
|-------|------|
| `nombre` | `String` |
| `departamento` | `String` |

#### `Barrio`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `zona` | `Zona` | Zona cardinal / macro |
| `nombre` | `String` | Nombre del barrio |
| `ciudad` | `Ciudad` | Ciudad a la que pertenece |

---

### Catálogo y operaciones sobre inmuebles

#### `Inmueble`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `codigo` | `String` | Código interno del inmueble |
| `direccion` | `String` | Dirección |
| `ciudad` | `Ciudad` | Ciudad |
| `barrio` | `Barrio` | Barrio |
| `tipoInmueble` | `TipoInmueble` | Apartamento, casa, etc. |
| `finalidad` | `Finalidad` | Venta o arrendamiento |
| `precio` | `double` | Precio |
| `area` | `double` | Área |
| `numeroHabitaciones` | `int` | Habitaciones |
| `numeroBanios` | `int` | Baños |
| `estado` | `Estado` | Disponibilidad comercial del inmueble |
| `asesor` | `Asesor` | Asesor asociado |
| `imagen` | `String` | Comentario en código: convendría una lista propia para varias imágenes |

#### `Visita`

Cita para ver un inmueble.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `codigo` | `String` | Identificador de la visita |
| `cliente` | `Cliente` | Cliente que visita |
| `inmueble` | `Inmueble` | Inmueble a visitar |
| `fecha` | `LocalDate` | Día |
| `hora` | `LocalTime` | Hora |
| `estado` | `EstadoVisita` | Estado del flujo de la visita |
| `asesotAsignado` | `Asesor` | Asesor asignado *(nombre del campo con posible typo: “asesot”)* |
| `observaciones` | `String` | Notas |

#### `Operacion`

Operación comercial genérica (venta, arriendo, renovación o cancelación según subclase).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `codigo` | `String` | Código de la operación |
| `inmueble` | `Inmueble` | Inmueble involucrado |
| `cliente` | `Cliente` | Cliente |
| `asesor` | `Asesor` | Asesor |
| `fecha` | `LocalDateTime` | Fecha/hora de registro o del hito principal |
| `estado` | `EstadoOperacion` | Estado del trámite |
| `comision` | `double` | Comisión |
| `valorAcordado` | `double` | Valor acordado |

Subclases:

| Clase | Campos adicionales |
|-------|-------------------|
| `Venta` | Ninguno (especialización vacía por ahora) |
| `Arriendo` | `duracionMeses` (`int`), `fechaVencimiento` (`LocalDate`) |
| `Renovacion` | `codigoOperacionAnterior` (`String`), `mesesAdicionales` (`int`), `fechaNuevaVencimiento` (`LocalDate`) |
| `Cancelacion` | `motivo` (`String`) |

*Nota de diseño:* `Renovacion` y `Cancelacion` extienden `Operacion` igual que `Venta` y `Arriendo`. Si en el negocio una cancelación no siempre comparte todos los atributos de una operación “completa”, convendrá revisar la jerarquía más adelante.

---

### Historial y alertas

#### `EventoHistorial`

Registro de un hecho sobre un inmueble (consulta, visita, etc.).

| Campo | Tipo |
|-------|------|
| `inmueble` | `Inmueble` |
| `tipoEvento` | `TipoEventoHistorial` |
| `fechaEvento` | `LocalDateTime` |

Constructor conveniente: `EventoHistorial(Inmueble, TipoEventoHistorial)` asigna `fechaEvento` a `LocalDateTime.now()`.

#### `Alerta`

Notificacion interna generada por reglas de seguimiento comercial.

Casos cubiertos:

- contratos proximos a vencer
- inmuebles sin visitas recientes
- propiedades con alta demanda
- visitas pendientes por confirmar
- inmuebles reservados por mucho tiempo sin cierre
- clientes sin seguimiento reciente

| Campo | Tipo |
|-------|------|
| `codigo` | `String` |
| `tipo` | `TipoAlerta` |
| `prioridad` | `PrioridadAlerta` |
| `titulo` | `String` |
| `descripcion` | `String` |
| `fechaGeneracion` | `LocalDateTime` |
| `fechaLimiteAtencion` | `LocalDateTime` |
| `atendida` | `boolean` |
| `fechaAtencion` | `LocalDateTime` |
| `inmueble` | `Inmueble` |
| `cliente` | `Cliente` |
| `contrato` | `Contrato` |
| `visita` | `Visita` |
| `operacion` | `Operacion` |
| `diasReferencia` | `int` |
| `cantidadReferencia` | `int` |

Incluye metodos convenientes para construir alertas de cada caso y `marcarAtendida()` para cerrar la alerta.

---

## Enumeraciones (`models.enums`)

| Enum | Valores | Uso principal |
|------|---------|----------------|
| `TipoSesion` | `CLIENTE`, `ASESOR` | Tipo de actor en `Sesion` |
| `TipoCliente` | `COMPRADOR`, `ARRENDATARIO` | Rol del cliente |
| `EstadoBusquedaCliente` | `BUSCANDO`, `NEGOCIANDO`, `CERRADO` | Fase de búsqueda del cliente |
| `Zona` | `NORTE`, `SUR`, `ESTE`, `OESTE`, `CENTRO` | Zonas geográficas |
| `TipoInmueble` | `APARTAMENTO`, `CASA`, `LOCAL_COMERCIAL`, `OFICINA`, `LOTE`, `BODEGA` | Tipología del inmueble |
| `Estado` | `DISPONIBLE`, `VENDIDO`, `ARRENDADO`, `RESERVADO` | Estado del inmueble en catálogo |
| `Finalidad` | `VENTA`, `ARRENDAMIENTO` | Objetivo de publicación del inmueble |
| `EstadoVisita` | `PENDIENTE`, `CONFIRMADA`, `REALIZADA`, `CANCELADA`, `REPROGRAMADA` | Flujo de visitas |
| `EstadoOperacion` | `COMPLETADA`, `EN_PROCESO`, `CANCELADA` | Estado de operaciones comerciales |
| `TipoEventoHistorial` | `CONSULTA`, `VISITA`, `DESCARTADO`, `GUARDADO`, `NEGOCIANDO`, `CERRADO` | Tipos de evento en historial |
| `TipoAlerta` | `CONTRATO_PROXIMO_A_VENCER`, `INMUEBLE_SIN_VISITAS`, `PROPIEDAD_ALTA_DEMANDA`, `VISITA_PENDIENTE_POR_CONFIRMAR`, `INMUEBLE_RESERVADO_SIN_CIERRE`, `CLIENTE_SIN_SEGUIMIENTO_RECIENTE` | Reglas que generan alertas |
| `PrioridadAlerta` | `BAJA`, `MEDIA`, `ALTA` | Urgencia de atencion de una alerta |

---

## Jerarquías de clases (resumen)

```
Usuario
├── Cliente
└── Asesor

Operacion
├── Venta
├── Arriendo
├── Renovacion
└── Cancelacion
```

---

## Pruebas

`src/test/java/.../InmobiliariaApplicationTests.java` — prueba de contexto Spring Boot estándar.

---

## Posibles mejoras (fuera del alcance de esta documentación)

- Unificar visibilidad de campos en `Asesor` (`private` + Lombok).
- Corregir el nombre `asesotAsignado` → `asesorAsignado` en `Visita` si se desea consistencia ortográfica.
- Sustituir `Alerta` vacía por atributos y reglas de negocio cuando se definan.
- Añadir anotaciones JPA (`@Entity`, `@Id`, relaciones) cuando se elija persistencia.
- Documentar contraseñas: hashing (BCrypt, etc.) y nunca persistir en claro.

---

*Documento generado a partir del código fuente del proyecto en su estado actual.*
