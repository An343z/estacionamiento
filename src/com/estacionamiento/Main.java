package com.estacionamiento;

import com.estacionamiento.dao.ConexionDB;

/**
 * Clase Principal - Punto de entrada de la aplicación
 * 
 * NOTA: Esta clase solo verificará la conexión a la base de datos.
 * Para ejecutar pruebas completas del sistema, ejecutar MainTest.java
 */
public class Main {
    
    public static void main(String[] args) {
        // Intentar conectar a la base de datos
        ConexionDB conexion = ConexionDB.getInstancia();
        
        System.out.println("=".repeat(70));
        System.out.println("SISTEMA DE GESTIÓN DE ESTACIONAMIENTOS");
        System.out.println("=".repeat(70));
        System.out.println();
        
        if (conexion.conectar()) {
            System.out.println("✓ Aplicación lista para iniciar");
            System.out.println();
            System.out.println("NOTA: Para ejecutar pruebas de módulos, ejecute MainTest");
            System.out.println();
            System.out.println("Módulos disponibles:");
            System.out.println("  - Modelos (Entidades): 16 clases completas");
            System.out.println("  - DAO (Acceso a datos): 10 DAOs con operaciones CRUD");
            System.out.println("  - Controllers (Lógica): 8 controladores funcionales");
            System.out.println();
            System.out.println("=".repeat(70));
        } else {
            System.err.println("✗ No se pudo conectar a la base de datos");
            System.err.println("Por favor, verifica:");
            System.err.println("1. Que MySQL esté en ejecución");
            System.err.println("2. Que la base de datos 'estacionamiento' exista");
            System.err.println("3. Los parámetros de conexión en ConexionDB.java");
            System.exit(1);
        }
    }
}
