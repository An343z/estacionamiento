package com.estacionamiento.api;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiSqlClient {
    private static final String API_KEY = "ppark-api-2026-change-this-key";
    private final ApiClient apiClient = new ApiClient();

    public QueryResult query(String sql, List<SqlParam> params) throws SQLException {
        Map<String, Object> response = request("query", sql, params);
        Object rowsValue = response.get("rows");
        Object columnsValue = response.get("columns");
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        if (columnsValue instanceof List<?>) {
            for (Object column : (List<?>) columnsValue) {
                columns.add(String.valueOf(column));
            }
        }
        if (rowsValue instanceof List<?>) {
            for (Object rowValue : (List<?>) rowsValue) {
                if (rowValue instanceof Map<?, ?>) {
                    rows.add(toStringMap((Map<?, ?>) rowValue));
                }
            }
        }
        return new QueryResult(columns, rows);
    }

    public UpdateResult update(String sql, List<SqlParam> params) throws SQLException {
        Map<String, Object> response = request("update", sql, params);
        return new UpdateResult(
                toInt(response.get("affectedRows")),
                valueToString(response.get("lastInsertId"))
        );
    }

    private Map<String, Object> request(String mode, String sql, List<SqlParam> params) throws SQLException {
        String json = buildRequest(mode, sql, params);
        try {
            ApiClient.ApiResponse response = apiClient.postJson("/sql.php", json);
            Map<String, Object> parsed = SimpleJson.parseObject(response.getBody());
            if (!response.isSuccess() || !Boolean.TRUE.equals(parsed.get("ok"))) {
                String message = valueToString(parsed.get("message"));
                String error = valueToString(parsed.get("error"));
                throw new SQLException(error == null ? message : message + ": " + error);
            }
            return parsed;
        } catch (IOException e) {
            throw new SQLException("No se pudo conectar con la API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Solicitud interrumpida", e);
        }
    }

    private String buildRequest(String mode, String sql, List<SqlParam> params) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"key\":\"").append(JsonUtils.escape(API_KEY)).append("\",")
                .append("\"mode\":\"").append(JsonUtils.escape(mode)).append("\",")
                .append("\"sql\":\"").append(JsonUtils.escape(sql)).append("\",")
                .append("\"params\":[");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            SqlParam param = params.get(i);
            builder.append('{')
                    .append("\"type\":\"").append(JsonUtils.escape(param.type)).append("\",")
                    .append("\"value\":");
            if (param.value == null) {
                builder.append("null");
            } else if (param.value instanceof Number || param.value instanceof Boolean) {
                builder.append(param.value);
            } else {
                builder.append('"').append(JsonUtils.escape(String.valueOf(param.value))).append('"');
            }
            builder.append('}');
        }
        builder.append("]}");
        return builder.toString();
    }

    private Map<String, Object> toStringMap(Map<?, ?> source) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return map;
    }

    static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    static String valueToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public static class SqlParam {
        final Object value;
        final String type;

        public SqlParam(Object value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    public static class QueryResult {
        private final List<String> columns;
        private final List<Map<String, Object>> rows;

        QueryResult(List<String> columns, List<Map<String, Object>> rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public List<String> getColumns() {
            return columns;
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }
    }

    public static class UpdateResult {
        private final int affectedRows;
        private final String lastInsertId;

        UpdateResult(int affectedRows, String lastInsertId) {
            this.affectedRows = affectedRows;
            this.lastInsertId = lastInsertId;
        }

        public int getAffectedRows() {
            return affectedRows;
        }

        public String getLastInsertId() {
            return lastInsertId;
        }
    }
}
