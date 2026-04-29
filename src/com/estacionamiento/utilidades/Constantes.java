package com.estacionamiento.utilidades;

/**
 * Clase con constantes de la aplicación
 */
public class Constantes {
    
    // Roles de usuario
    public static final int ROL_ADMIN = 1;
    public static final int ROL_ENCARGADO = 2;
    public static final int ROL_CAJERO = 3;
    
    public static final String[] ROLES = {"Admin", "Encargado", "Cajero"};
    
    // Estados de cajones
    public static final String ESTADO_DISPONIBLE = "Disponible";
    public static final String ESTADO_OCUPADO = "Ocupado";
    public static final String ESTADO_MANTENIMIENTO = "Mantenimiento";
    
    public static final String[] ESTADOS_CAJON = {
        ESTADO_DISPONIBLE,
        ESTADO_OCUPADO,
        ESTADO_MANTENIMIENTO
    };
    
    // Tipos de cajones
    public static final String TIPO_NORMAL = "Normal";
    public static final String TIPO_MINUSVALIDO = "Minusválido";
    public static final String TIPO_PREFERENTE = "Preferente";
    
    public static final String[] TIPOS_CAJON = {
        TIPO_NORMAL,
        TIPO_MINUSVALIDO,
        TIPO_PREFERENTE
    };
    
    // Tipos de vehículos
    public static final String TIPO_AUTO = "Auto";
    public static final String TIPO_MOTO = "Moto";
    public static final String TIPO_CAMIONETA = "Camioneta";
    
    public static final String[] TIPOS_VEHICULO = {
        TIPO_AUTO,
        TIPO_MOTO,
        TIPO_CAMIONETA
    };
    
    // Estados de registros
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_FINALIZADO = "Finalizado";
    
    // Estados de pensiones
    public static final String PENSION_ACTIVA = "Activa";
    public static final String PENSION_FINALIZADA = "Finalizada";
    public static final String PENSION_CANCELADA = "Cancelada";
    
    public static final String[] ESTADOS_PENSION = {
        PENSION_ACTIVA,
        PENSION_FINALIZADA,
        PENSION_CANCELADA
    };
    
    // Tipos de notificaciones
    public static final String NOTIF_INFO = "Info";
    public static final String NOTIF_ADVERTENCIA = "Advertencia";
    public static final String NOTIF_ERROR = "Error";
    
    public static final String[] TIPOS_NOTIFICACION = {
        NOTIF_INFO,
        NOTIF_ADVERTENCIA,
        NOTIF_ERROR
    };
    
    // Tipos de documentos
    public static final String[] TIPOS_DOCUMENTO = {
        "DNI",
        "Pasaporte",
        "Cédula",
        "Licencia"
    };
    
    // Dimensiones de ventanas (por defecto)
    public static final int VENTANA_ANCHO = 1024;
    public static final int VENTANA_ALTO = 768;
    
    // Mensajes
    public static final String MSG_EXITO = "Operación realizada exitosamente";
    public static final String MSG_ERROR = "Error al realizar la operación";
    public static final String MSG_CAMPOS_REQUERIDOS = "Por favor, complete todos los campos requeridos";
    public static final String MSG_CONFIRMACION = "¿Desea continuar?";
    
    // Conexión a BD
    public static final String BD_NOMBRE = "estacionamiento";
    public static final String BD_PUERTO = "3306";
}
