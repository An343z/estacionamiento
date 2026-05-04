package com.estacionamiento;

import com.estacionamiento.controladores.*;
import com.estacionamiento.modelos.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase MainTest para pruebas independientes del sistema de gestión de estacionamientos
 * Este programa verifica la funcionalidad de cada módulo sin interfaz gráfica
 * 
 * Arquitectura verificada:
 * - Modelos (Entidades) con atributos completos
 * - DAO con operaciones CRUD básicas
 * - Controllers como intermediarios
 * - Sin dependencias de UI ni integraciones externas
 */
public class MainTest {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("PRUEBAS DEL SISTEMA DE GESTIÓN DE ESTACIONAMIENTOS");
        System.out.println("Arquitectura por capas: Modelos -> DAO -> Controllers");
        System.out.println("=".repeat(70));
        System.out.println();

        try {
            // Pruebas de Estacionamiento
            pruebasEstacionamiento();
            System.out.println();

            // Pruebas de Usuario
            pruebasUsuario();
            System.out.println();

            // Pruebas de Cliente
            pruebasCliente();
            System.out.println();

            // Pruebas de Vehículo
            pruebasVehiculo();
            System.out.println();

            // Pruebas de Cajón
            pruebaCajon();
            System.out.println();

            // Pruebas de Pensión
            pruebaPension();
            System.out.println();

            // Pruebas de Registro de Entrada/Salida
            pruebaRegistroEntradaSalida();
            System.out.println();

            System.out.println("=".repeat(70));
            System.out.println("✓ TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE");
            System.out.println("=".repeat(70));

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pruebas del controlador de Estacionamientos
     */
    private static void pruebasEstacionamiento() throws Exception {
        System.out.println(">>> PRUEBAS DE ESTACIONAMIENTO");
        EstacionamientoController controller = new EstacionamientoController();

        // Crear estacionamiento
        Estacionamiento est = new Estacionamiento("Estacionamiento Centro", "Calle Principal 123", "555-1234", "info@estacionamiento.com", 100);
        est.setCiudad("Buenos Aires");
        est.setProvincia("Buenos Aires");
        est.setCodigoPostal("1000");

        if (controller.crearEstacionamiento(est)) {
            System.out.println("  ✓ Estacionamiento creado");
        }

        // Obtener todos
        List<Estacionamiento> estacionamientos = controller.obtenerTodosLosEstacionamientos();
        System.out.println("  ✓ Total estacionamientos en BD: " + estacionamientos.size());

        // Obtener por ID (si existen)
        if (!estacionamientos.isEmpty()) {
            Estacionamiento obtenido = controller.obtenerEstacionamiento(estacionamientos.get(0).getId());
            System.out.println("  ✓ Estacionamiento obtenido: " + obtenido.getNombre());
        }
    }

    /**
     * Pruebas del controlador de Usuarios
     */
    private static void pruebasUsuario() throws Exception {
        System.out.println(">>> PRUEBAS DE USUARIO");
        UsuarioController controller = new UsuarioController();

        // Crear usuario
        Usuario usr = new Usuario("Juan", "García", "admin@estacionamiento.com", "admin", "admin123", 1);

        if (controller.crearUsuario(usr)) {
            System.out.println("  ✓ Usuario creado");
        }

        // Obtener todos
        java.util.List<Usuario> usuarios = controller.obtenerTodos();
        System.out.println("  ✓ Total usuarios en BD: " + usuarios.size());

        // Autenticar
        if (controller.autenticar("admin", "admin123") != null) {
            System.out.println("  ✓ Autenticación exitosa");
        }
    }

    /**
     * Pruebas del controlador de Clientes
     */
    private static void pruebasCliente() throws Exception {
        System.out.println(">>> PRUEBAS DE CLIENTE");
        ClienteController controller = new ClienteController();

        // Crear cliente
        Cliente cli = new Cliente("Juan", "García", "juan@email.com", "555-5678", "12345678");
        cli.setTipoDocumento("DNI");
        cli.setCiudad("Buenos Aires");

        if (controller.crearCliente(cli)) {
            System.out.println("  ✓ Cliente creado");
        }

        // Obtener todos
        List<Cliente> clientes = controller.obtenerTodosLosClientes();
        System.out.println("  ✓ Total clientes en BD: " + clientes.size());

        // Obtener por documento
        if (!clientes.isEmpty()) {
            Cliente obtenido = controller.obtenerClientePorDocumento(clientes.get(0).getNumeroDocumento());
            if (obtenido != null) {
                System.out.println("  ✓ Cliente obtenido: " + obtenido.getNombre());
            }
        }
    }

    /**
     * Pruebas del controlador de Vehículos
     */
    private static void pruebasVehiculo() throws Exception {
        System.out.println(">>> PRUEBAS DE VEHÍCULO");
        VehiculoController controller = new VehiculoController();

        // Crear vehículo (se necesita cliente existente)
        ClienteController clienteCtrl = new ClienteController();
        List<Cliente> clientes = clienteCtrl.obtenerTodosLosClientes();

        if (!clientes.isEmpty()) {
            Vehiculo veh = new Vehiculo("ABC123", "Toyota", "Corolla", "Negro", clientes.get(0).getId(), "Auto");

            if (controller.crearVehiculo(veh)) {
                System.out.println("  ✓ Vehículo creado");
            }

            // Obtener todos
            List<Vehiculo> vehiculos = controller.obtenerTodosVehiculos();
            System.out.println("  ✓ Total vehículos en BD: " + vehiculos.size());

            // Obtener por patente
            if (!vehiculos.isEmpty()) {
                Vehiculo obtenido = controller.obtenerVehiculoPorPatente(vehiculos.get(0).getPatente());
                if (obtenido != null) {
                    System.out.println("  ✓ Vehículo obtenido: " + obtenido.getPatente());
                }
            }
        } else {
            System.out.println("  ⚠ No hay clientes para asociar vehículos");
        }
    }

    /**
     * Pruebas del controlador de Cajones
     */
    private static void pruebaCajon() throws Exception {
        System.out.println(">>> PRUEBAS DE CAJÓN");
        CajonController controller = new CajonController();
        EstacionamientoController estCtrl = new EstacionamientoController();

        List<Estacionamiento> estacionamientos = estCtrl.obtenerTodosLosEstacionamientos();
        if (!estacionamientos.isEmpty()) {
            Cajon caj = new Cajon(1, "Normal", "Disponible", estacionamientos.get(0).getId());

            if (controller.crearCajon(caj)) {
                System.out.println("  ✓ Cajón creado");
            }

            // Obtener cajones por estacionamiento
            List<Cajon> cajones = controller.obtenerCajonesPorEstacionamiento(estacionamientos.get(0).getId());
            System.out.println("  ✓ Total cajones: " + cajones.size());

            // Obtener disponibles
            int disponibles = controller.obtenerCajonesDisponibles(estacionamientos.get(0).getId());
            System.out.println("  ✓ Cajones disponibles: " + disponibles);

            // Cambiar estado
            if (!cajones.isEmpty()) {
                if (controller.cambiarEstadoCajon(cajones.get(0).getId(), "Ocupado")) {
                    System.out.println("  ✓ Estado de cajón actualizado");
                }
            }
        } else {
            System.out.println("  ⚠ No hay estacionamientos disponibles");
        }
    }

    /**
     * Pruebas del controlador de Pensiones
     */
    private static void pruebaPension() throws Exception {
        System.out.println(">>> PRUEBAS DE PENSIÓN");
        PensionController controller = new PensionController();
        
        ClienteController clienteCtrl = new ClienteController();
        VehiculoController vehiculoCtrl = new VehiculoController();
        CajonController cajonCtrl = new CajonController();
        EstacionamientoController estCtrl = new EstacionamientoController();

        List<Estacionamiento> estacionamientos = estCtrl.obtenerTodosLosEstacionamientos();
        List<Cliente> clientes = clienteCtrl.obtenerTodosLosClientes();
        List<Vehiculo> vehiculos = vehiculoCtrl.obtenerTodosVehiculos();

        if (!estacionamientos.isEmpty() && !clientes.isEmpty() && !vehiculos.isEmpty()) {
            List<Cajon> cajones = cajonCtrl.obtenerCajonesPorEstacionamiento(estacionamientos.get(0).getId());
            
            if (!cajones.isEmpty()) {
                Pension pension = new Pension(
                    clientes.get(0).getId(),
                    vehiculos.get(0).getId(),
                    cajones.get(0).getId(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(30),
                    3000.00,
                    estacionamientos.get(0).getId()
                );
                pension.setEstado("Activa");

                if (controller.crearPension(pension)) {
                    System.out.println("  ✓ Pensión creada");
                }

                // Obtener todas
                List<Pension> pensiones = controller.obtenerTodasPensiones();
                System.out.println("  ✓ Total pensiones: " + pensiones.size());

                // Obtener activas
                List<Pension> activas = controller.obtenerPensionesActivas(estacionamientos.get(0).getId());
                System.out.println("  ✓ Pensiones activas: " + activas.size());
            } else {
                System.out.println("  ⚠ No hay cajones disponibles");
            }
        } else {
            System.out.println("  ⚠ Falta información para crear pensión");
        }
    }

    /**
     * Pruebas del controlador de Registro de Entrada/Salida
     */
    private static void pruebaRegistroEntradaSalida() throws Exception {
        System.out.println(">>> PRUEBAS DE REGISTRO DE ENTRADA/SALIDA");
        RegistroController controller = new RegistroController();
        
        VehiculoController vehiculoCtrl = new VehiculoController();
        CajonController cajonCtrl = new CajonController();
        EstacionamientoController estCtrl = new EstacionamientoController();

        List<Estacionamiento> estacionamientos = estCtrl.obtenerTodosLosEstacionamientos();
        List<Vehiculo> vehiculos = vehiculoCtrl.obtenerTodosVehiculos();

        if (!estacionamientos.isEmpty() && !vehiculos.isEmpty()) {
            List<Cajon> cajones = cajonCtrl.obtenerCajonesPorEstacionamiento(estacionamientos.get(0).getId());
            
            if (!cajones.isEmpty()) {
                if (controller.registrarEntrada(vehiculos.get(0).getId(), cajones.get(0).getId(), estacionamientos.get(0).getId())) {
                    System.out.println("  ✓ Entrada registrada");
                }

                // Obtener registro activo
                RegistroEntradaSalida registroActivo = controller.obtenerRegistroActivoDelVehiculo(vehiculos.get(0).getId());
                if (registroActivo != null) {
                    System.out.println("  ✓ Registro activo encontrado");
                }

                // Obtener por estacionamiento
                List<RegistroEntradaSalida> registros = controller.obtenerRegistrosPorEstacionamiento(estacionamientos.get(0).getId());
                System.out.println("  ✓ Total registros en estacionamiento: " + registros.size());

            } else {
                System.out.println("  ⚠ No hay cajones disponibles");
            }
        } else {
            System.out.println("  ⚠ Falta información para registrar entrada/salida");
        }
    }
}
