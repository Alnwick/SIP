# SIP (Sistema Institucional de Practicas)

SIP es una aplicación desarrollada con **Spring Boot 3** diseñada para la gestión de procesos administrativos, servicios y seguimiento de documentos. El sistema integra autenticación segura, generación de documentos PDF y un sistema de notificaciones por correo electrónico.

## Stack Tecnológico

### Backend
* **Lenguaje:** Java 17.
* **Framework:** Spring Boot 3.5.7.
* **Gestión de Dependencias:** Maven.
* **Persistencia:** Spring Data JPA con Hibernate y MySQL Connector.
* **Seguridad:** Spring Security con JSON Web Tokens (JWT) usando `java-jwt`.
* **Documentación:** Swagger UI mediante `springdoc-openapi`.
* **Librerías Adicionales:** * **Lombok:** Para agilizar el desarrollo de modelos.
    * **iTextPDF:** Para la creación y manipulación de archivos PDF.
    * **Spring Mail:** Para la integración con servidores de correo.

### Frontend
* **Tecnologías:** HTML5, CSS3, JavaScript para componentes dinámicos.
* **Estructura:** Organizado por módulos de usuario (Administrador, Operativo y Estudiante) dentro de `src/main/resources/static`.

---

## Preparación del Entorno

### Requisitos Previos
1.  **Java Development Kit (JDK) 17** o superior.
2.  **MySQL Server** (instancia activa).
3.  **Maven** (o uso del wrapper `./mvnw` incluido).

### Configuración del Sistema de Archivos
La aplicación requiere que existan las siguientes rutas en el entorno local (Windows) para el manejo de archivos y logs:
* `C:/ApiSIP/Student_Doc/uploads` (Cargas de alumnos).
* `C:/ApiSIP/Base_Doc/Cedula_registro.pdf` (Plantilla base necesaria para PDFs).
* `C:/ApiSIP/Student_Doc/generates/` (Destino de documentos generados).
* `C:/ApiSIP/Logs/` (Carpeta para el archivo `pruebaLogs.log`).

---

## Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone <url-del-repositorio>
    cd ApiSIP
    ```

2.  **Variables de Entorno:**
    Es necesario configurar las siguientes variables en el sistema o en el archivo `application.properties` para la conexión y seguridad:
    * `DB_URL`: URL de conexión a MySQL.
    * `DB_USER` y `DB_PASSWORD`: Credenciales de la base de datos.
    * `PRIVATE_KEY`: Llave privada para la firma de tokens JWT.
    * `GMAIL`, `SPRING_EMAIL_USERNAME`, `SPRING_EMAIL_PASSWORD`: Configuración del servidor SMTP.
    * `SPRING_EMAIL_PORT`: Puerto del servidor de correo.

---

## Ejecución

Para ejecutar la aplicación en el entorno de desarrollo:

```bash
./mvnw spring-boot:run
