<?php
declare(strict_types=1);

require __DIR__ . '/db.php';

try {
    $pdo = db();
    $result = $pdo->query('SELECT DATABASE() AS database_name, NOW() AS server_time')->fetch();

    json_response([
        'ok' => true,
        'message' => 'Conexion exitosa',
        'database' => $result['database_name'] ?? null,
        'server_time' => $result['server_time'] ?? null,
    ]);
} catch (Throwable $e) {
    json_response([
        'ok' => false,
        'message' => 'No se pudo conectar a la base de datos',
        'error' => $e->getMessage(),
    ], 500);
}
