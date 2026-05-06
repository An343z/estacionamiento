# Instalación y Puesta en Marcha

## Requisitos del sistema

-   Java JDK 8 o superior
-   Maven (opcional si se usa JAR)
-   Base de datos (MySQL u otra compatible con JDBC)

## Configuración de la base de datos

Ejecutar: scripts/crear_base_datos.sql

Editar config.properties:
db.url=jdbc:mysql://localhost:3306/estacionamiento db.user=root
db.password=1234

## Ejecución

java -jar target/ppark-1.0.0.jar
