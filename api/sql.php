<?php
declare(strict_types=1);

require __DIR__ . '/db.php';

const API_KEY = 'ppark-api-2026-change-this-key';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(['ok' => false, 'message' => 'Metodo no permitido'], 405);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!is_array($input) || ($input['key'] ?? '') !== API_KEY) {
    json_response(['ok' => false, 'message' => 'No autorizado'], 401);
    exit;
}

$mode = strtolower((string)($input['mode'] ?? ''));
$sql = trim((string)($input['sql'] ?? ''));
$params = $input['params'] ?? [];

if ($sql === '' || !is_array($params)) {
    json_response(['ok' => false, 'message' => 'Solicitud invalida'], 400);
    exit;
}

if (strpos($sql, ';') !== false) {
    json_response(['ok' => false, 'message' => 'Multiples sentencias no permitidas'], 400);
    exit;
}

$firstWord = strtolower(strtok(ltrim($sql), " \t\r\n"));
$queryWords = ['select', 'show', 'describe', 'desc'];
$updateWords = ['insert', 'update', 'delete'];

if ($mode === 'query' && !in_array($firstWord, $queryWords, true)) {
    json_response(['ok' => false, 'message' => 'Consulta no permitida'], 400);
    exit;
}

if ($mode === 'update' && !in_array($firstWord, $updateWords, true)) {
    json_response(['ok' => false, 'message' => 'Actualizacion no permitida'], 400);
    exit;
}

try {
    $pdo = db();
    $stmt = $pdo->prepare($sql);

    foreach (array_values($params) as $index => $param) {
        $value = $param['value'] ?? null;
        $type = strtolower((string)($param['type'] ?? 'string'));
        $pdoType = PDO::PARAM_STR;

        if ($value === null || $type === 'null') {
            $pdoType = PDO::PARAM_NULL;
        } elseif ($type === 'int' || $type === 'integer' || $type === 'long') {
            $pdoType = PDO::PARAM_INT;
            $value = (int)$value;
        } elseif ($type === 'bool' || $type === 'boolean') {
            $pdoType = PDO::PARAM_BOOL;
            $value = (bool)$value;
        }

        $stmt->bindValue($index + 1, $value, $pdoType);
    }

    $stmt->execute();

    if ($mode === 'query') {
        $columns = [];
        for ($i = 0; $i < $stmt->columnCount(); $i++) {
            $meta = $stmt->getColumnMeta($i);
            $columns[] = $meta['name'] ?? ('column_' . ($i + 1));
        }

        json_response([
            'ok' => true,
            'columns' => $columns,
            'rows' => $stmt->fetchAll(),
        ]);
        exit;
    }

    json_response([
        'ok' => true,
        'affectedRows' => $stmt->rowCount(),
        'lastInsertId' => $pdo->lastInsertId(),
    ]);
} catch (Throwable $e) {
    json_response([
        'ok' => false,
        'message' => 'Error SQL',
        'error' => $e->getMessage(),
    ], 500);
}
