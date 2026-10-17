# API REST de administración de usuarios

API REST desarrollada con **Java 21** y **Spring Boot 4.1.1** para administrar usuarios. Aplica arquitectura por capas, validaciones con Bean Validation, manejo centralizado de excepciones con `@RestControllerAdvice`, búsqueda dinámica con filtros combinables y paginación, y baja lógica.

## Tecnologías

- Java 21 y Maven
- Spring Boot (Web, Data JPA, Validation, Security)
- PostgreSQL
- Lombok
- BCrypt para el cifrado de contraseñas

## Arquitectura

```
src/main/java/com/banco/onboarding
├── config/        SecurityConfig (BCrypt y acceso a los endpoints)
├── controller/    UsuarioController
├── dto/           Request y Response DTO, ApiResponse, PaginaResponse
├── entity/        Usuario, Rol
├── exception/     Excepciones propias y GlobalExceptionHandler
├── repository/    UsuarioRepository, UsuarioSpecifications
└── service/       UsuarioService (reglas de negocio)
database/
├── 01_schema.sql              Esquema base del proyecto
└── 02_usuarios_actividad.sql  Tabla usuarios de esta actividad
```

## Cumplimiento de los requisitos técnicos

| Requisito | Cómo se cumple |
|---|---|
| Separación de responsabilidades | Controller, Service, Repository, DTO y Entity en paquetes distintos |
| Request y Response DTO | `UsuarioCrearRequest`, `UsuarioActualizarRequest`, `UsuarioIdRequest`, `UsuarioBusquedaRequest` y `UsuarioResponse` |
| Bean Validation | `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@Past`, `@Positive`, `@Min` y `@Max` en los DTO |
| `@Valid` en los controladores | Todos los métodos reciben `@Valid @RequestBody` |
| Manejo global de errores | `GlobalExceptionHandler` con `@RestControllerAdvice` y un `@ExceptionHandler` por tipo de error |
| Excepciones propias | `UsuarioNoEncontradoException`, `CorreoDuplicadoException` y `ReglaNegocioException` |
| Un solo endpoint de búsqueda | `POST /usuarios/buscar` con filtros opcionales combinables (`Specification`) |
| Paginación | `PageRequest` de Spring Data, con `pagina`, `tamanio`, `ordenarPor` y `direccion` dentro del body |
| Spring Data JPA | `UsuarioRepository` con `JpaRepository` y `JpaSpecificationExecutor` |
| Campos controlados por el servidor | `id`, `activo`, `fechaCreacion` y `fechaActualizacion` no existen en los DTO de entrada |
| Códigos de estado adecuados | 200, 201, 400, 404, 405, 409 y 422 según el caso |
| Respuestas uniformes | Toda respuesta usa el envoltorio `ApiResponse` |

### Restricciones obligatorias

- No se usa `@PathVariable` ni `@RequestParam`: toda la información viaja en `@RequestBody`.
- No hay validaciones manuales ni bloques `try-catch` en los controladores.
- Nunca se reciben ni se devuelven entidades JPA.
- Un único endpoint de búsqueda, sin endpoints por cada campo.
- Los mensajes de error son específicos y no incluyen trazas, SQL ni detalles internos (los errores inesperados se registran solo en el log del servidor).

Verificación con PowerShell (sin coincidencias):

```powershell
Get-ChildItem -Recurse src/main/java -Filter *.java | Select-String -Pattern "PathVariable|RequestParam|try \{|catch"
```

## Modelo de datos

Tabla `usuarios`:

| Columna | Tipo | Restricciones |
|---|---|---|
| id | INTEGER IDENTITY | Llave primaria |
| cliente_id | INTEGER | Opcional, llave foránea a `clientes`, única |
| fecha_nacimiento | DATE | Obligatoria, no puede ser futura |
| fecha_creacion | TIMESTAMP | Obligatoria, la asigna el servidor |
| fecha_actualizacion | TIMESTAMP | La asigna el servidor |
| activo | BOOLEAN | Obligatoria, `TRUE` por defecto |
| rol | VARCHAR(20) | `ADMIN` o `USUARIO` |
| telefono | CHAR(10) | Exactamente 10 dígitos |
| password | CHAR(60) | Hash BCrypt |
| nombre | VARCHAR(50) | Obligatorio |
| apellidos | VARCHAR(100) | Obligatorio |
| correo | VARCHAR(100) | Obligatorio, índice único sobre `LOWER(correo)` |

Índices adicionales sobre `activo` y `rol`.

## Configuración y ejecución

1. Crea la base de datos `onboarding_clientes` en PostgreSQL y ejecuta, en orden, `database/01_schema.sql` y `database/02_usuarios_actividad.sql`.
2. Copia `src/main/resources/application-local.properties.example` como `application-local.properties` y completa tu URL (puerto incluido), usuario y contraseña. Este archivo está en el `.gitignore` y no se sube al repositorio.
3. Ejecuta:

```powershell
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

Todos reciben y devuelven JSON.

| Operación | Método y ruta | Body |
|---|---|---|
| Crear usuario | `POST /usuarios` | Datos del usuario y contraseña |
| Consultar por id | `POST /usuarios/consultar` | `{"id": 1}` |
| Actualizar | `PUT /usuarios` | Id y datos a modificar |
| Baja lógica | `PATCH /usuarios/baja` | `{"id": 1}` |
| Buscar con filtros y paginación | `POST /usuarios/buscar` | Filtros opcionales |

Ejemplo de creación:

```json
{
  "nombre": "Ana",
  "apellidos": "López Pérez",
  "correo": "ana@correo.com",
  "telefono": "4421234567",
  "fechaNacimiento": "1998-05-12",
  "rol": "USUARIO",
  "password": "Segura#2026"
}
```

Ejemplo de búsqueda (todos los campos son opcionales):

```json
{
  "nombre": "ana",
  "correo": "correo.com",
  "rol": "USUARIO",
  "activo": true,
  "fechaNacimientoDesde": "1990-01-01",
  "fechaNacimientoHasta": "2005-12-31",
  "pagina": 0,
  "tamanio": 10,
  "ordenarPor": "nombre",
  "direccion": "ASC"
}
```

Si no se envía paginación, se usa la página 0, tamaño 10 y orden por `id`. Solo se puede ordenar por `id`, `nombre`, `apellidos`, `correo`, `fechaNacimiento` o `fechaCreacion`.

### Estructura de las respuestas

Éxito:

```json
{
  "exito": true,
  "estado": 201,
  "mensaje": "Usuario creado correctamente",
  "datos": { },
  "errores": [],
  "fecha": "2026-10-17T13:03:40"
}
```

Error:

```json
{
  "exito": false,
  "estado": 409,
  "mensaje": "Correo duplicado",
  "datos": null,
  "errores": ["El correo ana@correo.com ya está registrado"],
  "fecha": "2026-10-17T13:01:06"
}
```

## Reglas de negocio y validaciones

| Campo o regla | Validación |
|---|---|
| Nombre y apellidos | Obligatorios, solo letras y espacios, entre 2 y 50 caracteres (apellidos hasta 100) |
| Correo | Obligatorio, formato válido, máximo 100 caracteres, único sin distinguir mayúsculas |
| Teléfono | Exactamente 10 dígitos |
| Fecha de nacimiento | Obligatoria, fecha pasada y mayoría de edad (18 años o más) |
| Rol | `ADMIN` o `USUARIO` |
| Contraseña | De 8 a 64 caracteres, con mayúscula, minúscula, número y carácter especial; se guarda cifrada con BCrypt y nunca se devuelve |
| Baja lógica | No se elimina el registro; un usuario ya dado de baja no se puede dar de baja de nuevo |
| Búsqueda | La fecha inicial no puede ser posterior a la final; el tamaño de página va de 1 a 100 |

| Situación | Código | Mensaje |
|---|---|---|
| Datos inválidos en el body | 400 | Error de validación en los datos enviados |
| JSON mal formado, fecha o rol inválido | 400 | El cuerpo de la solicitud es inválido |
| Usuario inexistente | 404 | Usuario no encontrado |
| Método HTTP no permitido | 405 | Método HTTP no permitido |
| Correo duplicado | 409 | Correo duplicado |
| Regla de negocio incumplida | 422 | Los datos no cumplen las reglas del sistema |
| Error inesperado | 500 | Error interno del servidor |

## Pruebas realizadas

Las pruebas se hicieron con Postman contra la API local y se verificaron en pgAdmin.

### Crear usuario

**Caso 1. Creación correcta.** `POST /usuarios` con datos válidos devuelve 201, con `activo: true` y sin el campo `password`.

![Ilustración 1. Creación de usuario con respuesta 201](docs/evidencias/01-crear-usuario-201.png)

**Caso 2. Contraseña cifrada.** En la base de datos, la contraseña de cada usuario inicia con `$2a$10$`, prefijo de BCrypt.

![Ilustración 2. Contraseñas cifradas con BCrypt en la tabla usuarios](docs/evidencias/02-password-bcrypt-pgadmin.png)

**Caso 3. Correo duplicado.** Registrar un correo existente devuelve 409.

![Ilustración 3. Correo duplicado con respuesta 409](docs/evidencias/03-correo-duplicado-409.png)

**Caso 4. Validaciones de campos.** Un body con varios datos inválidos devuelve 400 con una lista de errores por campo.

![Ilustración 4. Errores de validación con respuesta 400](docs/evidencias/04-validaciones-400.png)

**Caso 5. Menor de edad.** Una fecha de nacimiento que no cumple 18 años devuelve 422.

![Ilustración 5. Regla de mayoría de edad con respuesta 422](docs/evidencias/05-menor-de-edad-422.png)

**Caso 6. Rol inválido.** Un rol fuera de `ADMIN` o `USUARIO` devuelve 400.

![Ilustración 6. Rol inválido con respuesta 400](docs/evidencias/06-rol-invalido-400.png)

### Consultar usuario

**Caso 7. Usuario existente.** `POST /usuarios/consultar` con `{"id": 1}` devuelve 200.

![Ilustración 7. Consulta de usuario existente con respuesta 200](docs/evidencias/07-consultar-200.png)

**Caso 8. Usuario inexistente.** Con `{"id": 999}` devuelve 404.

![Ilustración 8. Usuario inexistente con respuesta 404](docs/evidencias/08-consultar-404.png)

**Caso 9. Id faltante.** Con `{}` devuelve 400 indicando que el id es obligatorio.

![Ilustración 9. Consulta sin id con respuesta 400](docs/evidencias/09-consultar-sin-id-400.png)

### Actualizar usuario

**Caso 10. Actualización con campos protegidos.** Se envía `activo: false` y `fechaCreacion` en el body. La respuesta es 200, se actualizan los apellidos, `activo` sigue en `true`, `fechaCreacion` no cambia y `fechaActualizacion` se llena. Los campos controlados por el servidor se ignoran.

![Ilustración 10. Actualización que ignora los campos controlados por el servidor](docs/evidencias/10-actualizar-200.png)

**Caso 11. Correo de otro usuario.** Cambiar el correo por uno que ya usa otro usuario devuelve 409.

![Ilustración 11. Actualización con correo duplicado y respuesta 409](docs/evidencias/11-actualizar-correo-duplicado-409.png)

### Baja lógica

**Caso 12. Baja correcta.** `PATCH /usuarios/baja` devuelve 200 con `activo: false`.

![Ilustración 12. Baja lógica con respuesta 200](docs/evidencias/12-baja-200.png)

**Caso 13. Baja repetida.** Dar de baja a un usuario ya inactivo devuelve 422.

![Ilustración 13. Baja repetida con respuesta 422](docs/evidencias/13-baja-repetida-422.png)

### Búsqueda con filtros y paginación

**Caso 14. Sin filtros.** `POST /usuarios/buscar` con `{}` devuelve todos los usuarios, en la página 0.

![Ilustración 14. Búsqueda sin filtros con respuesta 200](docs/evidencias/14-buscar-sin-filtros-200.png)

**Caso 15. Filtros combinados antes de la baja.** `{"nombre": "fran", "activo": false}` no devuelve resultados, porque el usuario todavía estaba activo.

![Ilustración 15. Filtros combinados sin resultados antes de la baja](docs/evidencias/15-buscar-combinado-antes-de-baja.png)

**Caso 16. Filtros combinados después de la baja.** Con el mismo body, ahora devuelve 1 resultado: el usuario dado de baja.

![Ilustración 16. Filtros combinados con 1 resultado después de la baja](docs/evidencias/16-buscar-combinado-despues-de-baja.png)

**Caso 17. Paginación y orden.** Con `tamanio: 1`, `ordenarPor: "nombre"` y `direccion: "DESC"`, devuelve un solo resultado por página, en orden descendente.

![Ilustración 17. Búsqueda con paginación y orden descendente](docs/evidencias/17-buscar-paginacion-orden.png)

**Caso 18. Campo de orden no permitido.** `{"ordenarPor": "password"}` devuelve 400.

![Ilustración 18. Campo de orden no permitido con respuesta 400](docs/evidencias/18-buscar-orden-invalido-400.png)

**Caso 19. Rango de fechas invertido.** Una fecha inicial posterior a la final devuelve 422.

![Ilustración 19. Rango de fechas invertido con respuesta 422](docs/evidencias/19-buscar-rango-fechas-422.png)

### Otros casos

**Caso 20. Método no permitido.** `GET /usuarios` devuelve 405.

![Ilustración 20. Método HTTP no permitido con respuesta 405](docs/evidencias/20-metodo-no-permitido-405.png)

**Caso 21. Estado final en la base de datos.** Tras las pruebas, el usuario 2 queda con `activo: false`, su `fecha_creacion` original y su `fecha_actualizacion` registrada.

![Ilustración 21. Estado final de la tabla usuarios en pgAdmin](docs/evidencias/21-estado-final-pgadmin.png)

## Control de versiones

El trabajo se desarrolló en la rama `feature/usuarios-api` con commits pequeños por capa (entidad, DTO, excepciones, repositorio y servicio, controlador) y se integró a `main` mediante Pull Request.
