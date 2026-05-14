<?php
declare(strict_types=1);

require __DIR__ . '/../db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response([
        'ok' => false,
        'message' => 'Metodo no permitido',
    ], 405);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!is_array($input)) {
    json_response([
        'ok' => false,
        'message' => 'JSON invalido',
    ], 400);
    exit;
}

$usuario = trim((string)($input['usuario'] ?? ''));
$contrasena = (string)($input['contrasena'] ?? '');

if ($usuario === '' || $contrasena === '') {
    json_response([
        'ok' => false,
        'message' => 'Usuario y contrasena son obligatorios',
    ], 400);
    exit;
}

try {
    $pdo = db();
    $stmt = $pdo->prepare(
        'SELECT u.*, e.nombre AS est_nombre
         FROM usuarios u
         LEFT JOIN estacionamientos e ON u.estacionamiento_id = e.id
         WHERE u.usuario = :usuario
           AND u.contrasena = :contrasena
           AND u.activo = true
         LIMIT 1'
    );
    $stmt->execute([
        ':usuario' => $usuario,
        ':contrasena' => $contrasena,
    ]);

    $user = $stmt->fetch();
    if (!$user) {
        json_response([
            'ok' => false,
            'message' => 'Usuario o contrasena incorrectos',
        ], 401);
        exit;
    }

    json_response([
        'ok' => true,
        'usuario' => [
            'id' => (int)$user['id'],
            'nombre' => $user['nombre'],
            'apellido' => $user['apellido'],
            'email' => $user['email'],
            'usuario' => $user['usuario'],
            'rol' => (int)$user['rol'],
            'estacionamiento_id' => $user['estacionamiento_id'] === null ? null : (int)$user['estacionamiento_id'],
            'nombre_estacionamiento' => $user['est_nombre'] ?? null,
            'activo' => (bool)$user['activo'],
            'fecha_creacion' => $user['fecha_creacion'] ?? null,
            'fecha_modificacion' => $user['fecha_modificacion'] ?? null,
        ],
    ]);
} catch (Throwable $e) {
    json_response([
        'ok' => false,
        'message' => 'No se pudo autenticar',
        'error' => $e->getMessage(),
    ], 500);
}
