# FALTANTES PARA PRODUCCIÓN

Este documento lista lo que aún falta en el proyecto para llevarlo a un estado de producción completo y profesional. Está dividido en 4 partes con foco en los elementos pendientes y las tareas clave.

---

## 1. INTERFAZ DE USUARIO (UI)

### Objetivo
Diseñar e implementar la capa de presentación que permita a los usuarios operar el sistema de estacionamientos de forma amigable, accesible y segura.

### Pendientes
- Crear la aplicación de interfaz gráfica completa.
  - Panel de inicio de sesión con roles: Administrador, Encargado, Cajero.
  - Menú principal con acceso a módulos: Estacionamientos, Cajones, Clientes, Vehículos, Pensiones, Precios, Promociones, Notificaciones, Configuración y Reportes.
- Implementar formularios de CRUD para todas las entidades principales.
  - Crear/editar/consultar/eliminar Estacionamientos, Cajones, Clientes, Vehículos, Pensiones, Precios, Promociones, Notificaciones, Configuraciones.
- Integrar validaciones de UI antes de enviar datos a los Controllers.
  - Validar campos obligatorios, formatos de fechas y valores numéricos.
  - Mostrar mensajes de error y confirmaciones claros.
- Soporte de navegación responsiva y estados de carga.
  - Estados de espera mientras se ejecutan operaciones.
  - Interfaz clara para datos sin resultados.
- Implementar vistas de reportes/estadísticas.
  - Estado de ocupación de cajones.
  - Ingresos diarios/semanales.
  - Pensiones activas y promociones vigentes.
- Asegurar una experiencia consistente y profesional.
  - Íconos y etiquetas comprensibles.
  - Accesibilidad básica (tabuladores, contraste y mensajes legibles).

### Recomendaciones de UI
- Usar Java Swing como base, o considerar JavaFX para mayor modernidad.
- Mantener la lógica de UI separada de la lógica de negocio.
- No mezclar llamadas directas a la base de datos desde la UI.

---

## 2. CONEXIONES Y DATA LAYER PARA PRODUCCIÓN

### Objetivo
Completar y endurecer las conexiones de datos para un entorno de producción, incluyendo base de datos en la nube, manejo de configuración segura y resiliencia.

### Pendientes
- Configurar la conexión a la base de datos en la nube.
  - Parámetros de conexión leídos desde `config.properties` o variables de entorno.
  - Soporte para entorno local, test y producción.
- Validar y asegurar las credenciales.
  - No dejar contraseñas embebidas en el código.
  - Usar archivos de configuración seguros o variables de entorno.
- Implementar manejo de errores y reintentos.
  - Conexión a BD con reconexión automática en caso de falla temporal.
  - Tiempo de espera configurables (`timeout`) para consultas.
- Añadir logs en el acceso a datos.
  - Registrar operaciones críticas con nivel INFO/ERROR.
  - Evitar exponer datos sensibles en logs.
- Revisar transacciones y consistencia de datos.
  - Usar transacciones en operaciones compuestas si se agrega la capa de servicio.
  - Garantizar consistencia al crear/actualizar datos relacionados.
- Ajustar `ConexionDB` para producción.
  - Validar múltiples conexiones concurrentes si se requiere.
  - Verificar cierre de `Connection`, `Statement` y `ResultSet` en todos los DAO.
- Preparar scripts de base de datos para despliegue.
  - Script SQL actualizado con índices, constraints y datos iniciales.
  - Instrucciones claras de despliegue en la nube.

### Recomendaciones de conexión
- Usar un pool de conexiones como HikariCP si el proyecto escala.
- Mantener la capa DAO lo más limpia posible.
- Separar propiedades de ambiente en archivos distintos: `config-dev.properties`, `config-prod.properties`.

---

## 3. PREPARACIÓN DE PRODUCCIÓN

### Objetivo
Implementar los elementos necesarios para ejecutar el proyecto en producción con calidad, estabilidad y soporte.

### Pendientes
- Crear un proceso de empaquetado y despliegue.
  - Construir un `jar` ejecutable con dependencias.
  - Documentar comandos de compilación y ejecución.
- Añadir logging y monitoreo.
  - Integrar `java.util.logging`, `Log4j2` o similar.
  - Registrar eventos de negocio importantes y errores críticos.
- Definir configuración de entorno.
  - Variables de entorno para `DB_URL`, `DB_USER`, `DB_PASSWORD` y `MODELO_PRODUCCION`.
  - Parámetros de logging y nivel de detalles.
- Asegurar la aplicación.
  - Validar autenticación y roles antes de acciones sensibles.
  - Evitar ejecución de SQL inyectable en DAO.
- Establecer mecanismos de rollback/recuperación mínima.
  - Operaciones que fallan deben dejar la base de datos en estado consistente.
- Preparar despliegue en servidor/PC.
  - Instrucciones del entorno Java requerido y versiones.
  - Requisitos de hardware mínimos.
- Incluir pruebas de aceptación/
  - Pruebas funcionales manuales para los flujos críticos.
  - Checklist de verificación antes de pasar a producción.

### Recomendaciones de producción
- Mantener el proyecto en un control de versiones limpio.
- Revisar las dependencias externas antes de empaquetar.
- Integrar un plan de respaldo de base de datos.

---

## 4. DOCUMENTACIÓN Y SOPORTE OPERATIVO

### Objetivo
Completar la documentación y los elementos de soporte para facilitar la puesta en marcha, mantenimiento y transferencia del proyecto.

### Pendientes
- Documentar la instalación y puesta en marcha.
  - Pasos para compilar y ejecutar en Windows.
  - Configuración de base de datos local y en la nube.
- Crear guías de usuario.
  - Manual básico para administradores, encargados y cajeros.
  - Uso de cada módulo y reporte.
- Documentar la arquitectura y el flujo.
  - Diagrama de capas: UI → Controllers → DAOs → DB.
  - Descripción de cada entidad y sus relaciones.
- Preparar un plan de mantenimiento.
  - Cómo actualizar credenciales y parámetros.
  - Cómo aplicar cambios en los scripts de BD.
- Crear una lista de verificación previa a producción.
  - Revisar conexión con DB en la nube.
  - Verificar que la UI realiza llamadas solo a Controllers.
  - Confirmar que no hay referencias a datos de prueba en producción.

### Recomendaciones de documentación
- Mantener los documentos actualizados en el repositorio.
- Incluir un `README.md` claro con resumen de estado y pasos de despliegue.
- Documentar cualquier cambio de esquema de base de datos con un script migración.

---

## PRIORIDAD RECOMENDADA

1. UI funcional y separada de la lógica de negocio.
2. Conexiones seguras y configurables para producción.
3. Empaquetado y despliegue con logging/monitorización.
4. Documentación de instalación, uso y mantenimiento.

---

## NOTA FINAL

El proyecto actual presenta la lógica de negocio y la capa de datos listas, pero aún falta completar la capa de presentación y la integración de producción. Con estas cuatro áreas resueltas, el sistema quedará listo para ser desplegado y usado en un entorno real.
