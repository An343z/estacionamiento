# GUÍA RÁPIDA - ARQUITECTURA DEL SISTEMA

## Estructura del Proyecto

```
estacionamiento/
├── src/
│   └── com/estacionamiento/
│       ├── Main.java                    ← Punto de entrada (verificar conexión)
│       ├── MainTest.java                ← Pruebas de módulos
│       ├── modelos/                     ← 16 Entidades
│       │   ├── Estacionamiento.java
│       │   ├── Usuario.java
│       │   ├── Cliente.java
│       │   ├── Vehiculo.java
│       │   ├── Cajon.java
│       │   ├── Precio.java
│       │   ├── Promocion.java
│       │   ├── Pension.java
│       │   ├── RegistroEntradaSalida.java
│       │   └── ...otros modelos
│       ├── dao/                         ← 10 DAOs
│       │   ├── ConexionDB.java         ← Singleton - Maneja conexión
│       │   ├── EstacionamientoDAO.java
│       │   ├── UsuarioDAO.java
│       │   ├── ClienteDAO.java
│       │   ├── VehiculoDAO.java
│       │   ├── CajonDAO.java
│       │   ├── PrecioDAO.java
│       │   ├── PromocionDAO.java
│       │   ├── PensionDAO.java
│       │   └── RegistroEntradaSalidaDAO.java
│       └── controladores/               ← 8 Controllers
│           ├── EstacionamientoController.java
│           ├── UsuarioController.java
│           ├── ClienteController.java
│           ├── VehiculoController.java
│           ├── CajonController.java
│           ├── PensionController.java
│           ├── RegistroController.java
│           └── AdminController.java
├── bin/                                 ← Compilados
├── scripts/
│   └── crear_base_datos.sql            ← BD structure
└── RESUMEN_IMPLEMENTACION.md           ← Este documento
```

## Cómo Usar Cada Componente

### 1. Crear un Nuevo Registro

```java
// 1. Crear el modelo
Cliente cliente = new Cliente("Juan", "García", "juan@email.com", "555-1234", "12345678");
cliente.setTipoDocumento("DNI");

// 2. Usar el Controller
ClienteController controller = new ClienteController();
if (controller.crearCliente(cliente)) {
    System.out.println("✓ Cliente creado");
}
```

### 2. Obtener Registros

```java
// Obtener todos
List<Cliente> clientes = controller.obtenerTodosLosClientes();

// Obtener por ID
Cliente cliente = controller.obtenerCliente(1);

// Obtener con filtro específico
Cliente cliente = controller.obtenerClientePorDocumento("12345678");
```

### 3. Actualizar Registro

```java
Cliente cliente = controller.obtenerCliente(1);
cliente.setTelefono("555-9999");
if (controller.actualizarCliente(cliente)) {
    System.out.println("✓ Cliente actualizado");
}
```

### 4. Eliminar Registro

```java
if (controller.eliminarCliente(1)) {
    System.out.println("✓ Cliente eliminado");
}
```

## Flujo de Entrada/Salida de Vehículos

```java
// 1. Registrar entrada
RegistroController registroCtrl = new RegistroController();
registroCtrl.registrarEntrada(vehiculoId, cajonId, estacionamientoId);

// 2. Cambiar estado del cajón a Ocupado
CajonController cajonCtrl = new CajonController();
cajonCtrl.cambiarEstadoCajon(cajonId, "Ocupado");

// 3. Registrar salida (calcula automáticamente el monto)
RegistroEntradaSalida salida = registroCtrl.registrarSalida(
    vehiculoId, 
    "Auto",  // tipo de vehículo
    estacionamientoId
);

// 4. El monto se calcula automáticamente basado en:
//    - Tiempo estacionado
//    - Tipo de vehículo
//    - Tarifas configuradas
```

## Gestión de Pensiones (Estacionamientos de Larga Duración)

```java
PensionController pensionCtrl = new PensionController();

// Crear pensión
Pension pension = new Pension(
    clienteId,
    vehiculoId,
    cajonId,
    LocalDateTime.now(),
    LocalDateTime.now().plusDays(30),
    3000.00,
    estacionamientoId
);
pension.setEstado("Activa");

pensionCtrl.crearPension(pension);

// Obtener pensiones activas
List<Pension> activas = pensionCtrl.obtenerPensionesActivas(estacionamientoId);

// Cancelar pensión
pensionCtrl.cancelarPension(pensionId);
```

## Manejo de Usuarios y Autenticación

```java
UsuarioController usuarioCtrl = new UsuarioController();

// Crear usuario
Usuario admin = new Usuario(
    "Juan", "García", 
    "admin@estacionamiento.com", 
    "admin", "password123", 
    1  // Rol 1 = Admin Global
);

usuarioCtrl.crearUsuario(admin);

// Autenticar
Usuario usuarioLogueado = usuarioCtrl.autenticar("admin", "password123");
if (usuarioLogueado != null) {
    System.out.println("✓ Autenticación exitosa");
    System.out.println("Rol: " + usuarioCtrl.obtenerDescripcionRol(usuarioLogueado.getRol()));
}

// Roles disponibles:
// 1 = Administrador Global (ve todos los estacionamientos)
// 2 = Encargado de Estacionamiento (asignado a uno)
// 3 = Cajero (asignado a uno)
```

## Validación de Datos

```java
// Cada Controller incluye validación básica
ClienteController clienteCtrl = new ClienteController();

Cliente cliente = new Cliente(null, "García", "email@test.com", "555-1234", "12345678");

String error = clienteCtrl.validarCliente(cliente);
if (error != null) {
    System.out.println("Error de validación: " + error);
}
```

## Transacciones

```java
// Ejemplo: Cambiar estado de múltiples cajones
try {
    List<Cajon> cajones = cajonCtrl.obtenerCajonesPorEstacionamiento(estacionamientoId);
    for (Cajon cajon : cajones) {
        cajonCtrl.cambiarEstadoCajon(cajon.getId(), "Disponible");
    }
} catch (Exception e) {
    System.err.println("Error en transacción: " + e.getMessage());
}
```

## Manejo de Excepciones

```java
try {
    List<Vehiculo> vehiculos = vehiculoCtrl.obtenerTodosVehiculos();
    System.out.println("Total vehículos: " + vehiculos.size());
} catch (Exception e) {
    System.err.println("Error al obtener vehículos: " + e.getMessage());
    e.printStackTrace();
}
```

## Consultas Comunes

### Obtener cajones disponibles
```java
int disponibles = cajonCtrl.obtenerCajonesDisponibles(estacionamientoId);
System.out.println("Cajones disponibles: " + disponibles);
```

### Obtener ingresos del día
```java
double ingresos = registroCtrl.obtenerIngresoDelDia(
    estacionamientoId, 
    LocalDateTime.now()
);
System.out.println("Ingresos hoy: $" + ingresos);
```

### Obtener vehículos de un cliente
```java
List<Vehiculo> vehiculosCliente = vehiculoCtrl.obtenerVehiculosPorCliente(clienteId);
System.out.println("Vehículos del cliente: " + vehiculosCliente.size());
```

### Obtener usuarios de un estacionamiento
```java
List<Usuario> usuarios = usuarioCtrl.obtenerUsuariosPorEstacionamiento(estacionamientoId);
System.out.println("Usuarios en estacionamiento: " + usuarios.size());
```

## Métodos Importantes por Clase

### EstacionamientoController
- `crearEstacionamiento(Estacionamiento)` ✓
- `obtenerEstacionamiento(int id)` ✓
- `obtenerTodosLosEstacionamientos()` ✓
- `actualizarEstacionamiento(Estacionamiento)` ✓

### ClienteController
- `crearCliente(Cliente)` ✓
- `obtenerCliente(int id)` ✓
- `obtenerTodosLosClientes()` ✓
- `obtenerClientePorDocumento(String)` ✓
- `actualizarCliente(Cliente)` ✓
- `eliminarCliente(int id)` ✓

### VehiculoController
- `crearVehiculo(Vehiculo)` ✓
- `obtenerVehiculoPorPatente(String)` ✓ (NO getPlaca)
- `obtenerVehiculosPorCliente(int clienteId)` ✓
- `actualizarVehiculo(Vehiculo)` ✓
- `eliminarVehiculo(int id)` ✓

### CajonController
- `crearCajon(Cajon)` ✓
- `obtenerCajonesPorEstacionamiento(int)` ✓
- `obtenerCajonesDisponibles(int)` ✓
- `cambiarEstadoCajon(int cajonId, String estado)` ✓
- `actualizarCajon(Cajon)` ✓
- `eliminarCajon(int id)` ✓

### PensionController
- `crearPension(Pension)` ✓
- `obtenerTodasPensiones()` ✓
- `obtenerPensionesActivas(int estacionamientoId)` ✓
- `obtenerPensionesPorCliente(int clienteId)` ✓
- `cancelarPension(int pensionId)` ✓
- `eliminarPension(int id)` ✓

### RegistroController
- `registrarEntrada(int vehiculoId, int cajonId, int estacionamientoId)` ✓
- `obtenerRegistroActivoDelVehiculo(int vehiculoId)` ✓
- `obtenerRegistrosPorEstacionamiento(int estacionamientoId)` ✓
- `obtenerIngresoDelDia(int estacionamientoId, LocalDateTime)` ✓

## Conexión a Base de Datos

```java
// ConexionDB es un Singleton - Usarlo así:
ConexionDB conexion = ConexionDB.getInstancia();
Connection conn = conexion.getConexion();

// Para verificar si está conectado:
if (conexion.estaConectado()) {
    System.out.println("✓ Conectado a BD");
}
```

**NOTA:** Modificar credenciales en `ConexionDB.java`:
```java
private static final String URL = "jdbc:mysql://HOST:PUERTO/estacionamiento_db";
private static final String USUARIO = "usuario";
private static final String CONTRASENA = "contraseña";
```

## Testing

```bash
# Compilar
javac -encoding UTF-8 -d bin src/com/estacionamiento/*.java src/com/estacionamiento/*/*.java

# Ejecutar pruebas
java -cp bin com.estacionamiento.MainTest

# Verificar conexión
java -cp bin com.estacionamiento.Main
```

## Convenciones de Nombres

| Tipo | Formato | Ejemplo |
|------|---------|---------|
| Clases | PascalCase | `ClienteController` |
| Métodos | camelCase | `obtenerClientePorDocumento()` |
| Constantes | UPPER_CASE | `TOTAL_CAJONES` |
| Variables | camelCase | `totalClientes` |
| BD Fields | snake_case | `numero_documento` |
| Java Fields | camelCase | `numeroDocumento` |

## Lista de Verificación para Nueva Feature

- [ ] ¿Se agregó la entidad en modelos/?
- [ ] ¿Se implementó el DAO con CRUD?
- [ ] ¿Se validó el DAO con ConexionDB?
- [ ] ¿Se creó el Controller?
- [ ] ¿Se agregaron validaciones básicas?
- [ ] ¿Se testeó con MainTest?
- [ ] ¿Se compila sin errores?
- [ ] ¿Se documentó el código?

---

**Estado: ✅ PROYECTO COMPILABLE Y FUNCIONAL**
