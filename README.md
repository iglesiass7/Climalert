# Climalert

Sistema de monitoreo climático y envío automático de alertas.
Práctica de clase — Cátedra Diseño de Sistemas de Información (UTN.BA).

## Qué hace

Climalert es un servicio autónomo (sin interfaz gráfica) que:

1. **Cada 5 minutos** consulta el clima actual de una ubicación fija (Buenos Aires) usando el endpoint `/current.json` de [WeatherAPI](https://www.weatherapi.com) y lo guarda en una base de datos H2 local, como registro histórico para análisis posterior.
2. **Cada 1 minuto** analiza la última información disponible. Si la temperatura es mayor a 35 °C **y** la humedad es superior a 60 %, genera una alerta.
3. Al generarse una alerta, **envía un correo** con el detalle completo del clima a: `admin@clima.com`, `emergencias@clima.com` y `meteorologia@clima.com`.

## Tecnologías

Java 21, Spring Boot 3.5, Spring Scheduling (tareas programadas), Spring Data JPA con H2 (persistencia local en archivo), Spring Mail (SMTP) y WeatherAPI como proveedor externo.

## Flujo del sistema

```
WeatherAPI (/current.json)
        │  cada 5 min (REST GET)
        ▼
WeatherApiClient ──► WeatherFetchService ──► Base H2 (tabla weather_records)
                                                   │
                                                   │  cada 1 min (lee el último registro)
                                                   ▼
                                             AlertService
                                                   │  si temp > 35 °C y humedad > 60 %
                                                   ▼
                                             EmailService ──► SMTP ──► 3 destinatarios
```

## Estructura del código

| Clase | Responsabilidad |
|---|---|
| `ClimalertApplication` | Arranca la app y habilita las tareas programadas (`@EnableScheduling`). |
| `client/WeatherApiClient` | Llama vía REST a WeatherAPI y deserializa el JSON. |
| `dto/WeatherApiResponse` | Records (Java 21) que mapean la respuesta JSON de la API. |
| `model/WeatherRecord` | Entidad JPA: una "foto" del clima guardada en la base. |
| `repository/WeatherRecordRepository` | Acceso a datos; obtiene el registro más reciente. |
| `service/WeatherFetchService` | Tarea programada cada 5 min: consulta y guarda el clima. |
| `service/AlertService` | Tarea programada cada 1 min: analiza y dispara la alerta. |
| `service/EmailService` | Arma y envía el correo con el detalle completo del clima. |

## Configuración

Todo se configura en `src/main/resources/application.properties`. Hay que completar 3 valores:

| Propiedad | Qué poner | Dónde conseguirlo |
|---|---|---|
| `climalert.weather-api.key` | API key de WeatherAPI | Cuenta gratuita en [weatherapi.com](https://www.weatherapi.com) → la key aparece en el dashboard. |
| `spring.mail.username` | Usuario SMTP de Mailtrap | Cuenta gratuita en [mailtrap.io](https://mailtrap.io) → Email Testing → My Inbox → SMTP Settings. |
| `spring.mail.password` | Password SMTP de Mailtrap | Mismo lugar que el usuario. |

**¿Por qué Mailtrap?** Los destinatarios del enunciado (`@clima.com`) no son casillas reales. Mailtrap es un servidor SMTP de prueba que captura todos los correos salientes en un inbox web, lo que permite verificar el envío y el contenido sin mandar mails reales.

## Cómo ejecutar

Requisitos: JDK 21 (IntelliJ puede descargarlo) y conexión a internet.

**Opción A (recomendada):** abrir la carpeta del proyecto con IntelliJ IDEA, esperar a que Maven descargue las dependencias y ejecutar la clase `ClimalertApplication` (botón ▶).

**Opción B (consola, con Maven instalado):**

```bash
mvn spring-boot:run
```

La aplicación queda corriendo indefinidamente: consulta el clima al arrancar y luego cada 5 minutos, y analiza cada 1 minuto. Todo se ve en los logs de consola.

## Cómo probar la alerta

En invierno en Buenos Aires nunca se superan los 35 °C, así que para verificar el flujo completo:

1. En `application.properties` cambiar temporalmente `climalert.alert.temperature-threshold=-50` y `climalert.alert.humidity-threshold=-1`.
2. Reiniciar la aplicación. En menos de 1 minuto el log mostrará `CONDICIONES CRITICAS ... Enviando alerta...` y el correo aparecerá en el inbox de Mailtrap con el detalle completo del clima.
3. Volver los umbrales a `35` y `60`.

## Ver el registro histórico

Con la app corriendo, entrar a `http://localhost:8080/h2-console` con JDBC URL `jdbc:h2:file:./data/climalert`, usuario `sa` y password vacío. Ejecutar `SELECT * FROM WEATHER_RECORDS;` para ver todas las mediciones guardadas. (La consola H2 es solo una herramienta de desarrollo para inspeccionar la base; el sistema en sí no tiene interfaz gráfica.)

## Decisiones de diseño

- **Marca `alertSent` por registro:** el análisis corre cada 1 minuto pero los datos se renuevan cada 5; sin esta marca se enviarían ~5 correos idénticos por cada medición crítica. Con la marca, se envía exactamente una alerta por cada registro nuevo que cumpla las condiciones. Si se prefiere el comportamiento literal de "una alerta por minuto", basta con quitar la verificación `record.isAlertSent()` en `AlertService`.
- **H2 en modo archivo:** cumple el requisito de almacenamiento local sin necesidad de instalar un motor de base de datos externo; los datos persisten entre ejecuciones en `./data/climalert.mv.db`.
- **Tolerancia a fallos:** los errores de red/API o de SMTP se registran en el log y no detienen el servicio; se reintenta en el siguiente ciclo. Si el envío del correo falla, el registro no se marca como alertado, así el envío se reintenta.
- **DTOs como records e inyección por constructor:** código inmutable, simple y fácil de testear.
