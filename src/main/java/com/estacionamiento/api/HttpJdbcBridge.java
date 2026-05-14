package com.estacionamiento.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HttpJdbcBridge {
    private HttpJdbcBridge() {
    }

    public static Connection open() {
        return proxy(Connection.class, new ConnectionHandler());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class ConnectionHandler implements InvocationHandler {
        private boolean closed;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("prepareStatement".equals(name)) {
                return HttpJdbcBridge.proxy(PreparedStatement.class, new PreparedStatementHandler((String) args[0]));
            }
            if ("createStatement".equals(name)) {
                return HttpJdbcBridge.proxy(Statement.class, new StatementHandler());
            }
            if ("close".equals(name)) {
                closed = true;
                return null;
            }
            if ("isClosed".equals(name)) return closed;
            if ("isValid".equals(name)) return !closed;
            if ("getAutoCommit".equals(name)) return true;
            if ("setAutoCommit".equals(name) || "commit".equals(name) || "rollback".equals(name)) return null;
            if ("toString".equals(name)) return "HttpJdbcBridgeConnection";
            return defaultValue(method);
        }
    }

    private static class StatementHandler implements InvocationHandler {
        protected final ApiSqlClient client = new ApiSqlClient();
        protected List<Map<String, Object>> generatedKeys = Collections.emptyList();
        private boolean closed;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("executeQuery".equals(name)) {
                ApiSqlClient.QueryResult result = client.query((String) args[0], Collections.emptyList());
                return resultSet(result.getColumns(), result.getRows());
            }
            if ("executeUpdate".equals(name)) {
                ApiSqlClient.UpdateResult result = client.update((String) args[0], Collections.emptyList());
                generatedKeys = generatedKeyRows(result.getLastInsertId());
                return result.getAffectedRows();
            }
            if ("execute".equals(name)) {
                String sql = (String) args[0];
                if (isQuery(sql)) {
                    client.query(sql, Collections.emptyList());
                    return true;
                }
                client.update(sql, Collections.emptyList());
                return false;
            }
            if ("getGeneratedKeys".equals(name)) return resultSet(generatedKeys);
            if ("close".equals(name)) {
                closed = true;
                return null;
            }
            if ("isClosed".equals(name)) return closed;
            if ("toString".equals(name)) return "HttpJdbcBridgeStatement";
            return defaultValue(method);
        }
    }

    private static final class PreparedStatementHandler extends StatementHandler {
        private final String sql;
        private final Map<Integer, ApiSqlClient.SqlParam> params = new LinkedHashMap<>();

        private PreparedStatementHandler(String sql) {
            this.sql = sql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) {
                params.put((Integer) args[0], toParam(name, args));
                return null;
            }
            if ("clearParameters".equals(name)) {
                params.clear();
                return null;
            }
            if ("executeQuery".equals(name)) {
                ApiSqlClient.QueryResult result = client.query(sql, orderedParams());
                return resultSet(result.getColumns(), result.getRows());
            }
            if ("executeUpdate".equals(name)) {
                ApiSqlClient.UpdateResult result = client.update(sql, orderedParams());
                generatedKeys = generatedKeyRows(result.getLastInsertId());
                return result.getAffectedRows();
            }
            if ("execute".equals(name)) {
                if (isQuery(sql)) {
                    client.query(sql, orderedParams());
                    return true;
                }
                ApiSqlClient.UpdateResult result = client.update(sql, orderedParams());
                generatedKeys = generatedKeyRows(result.getLastInsertId());
                return false;
            }
            return super.invoke(proxy, method, args);
        }

        private List<ApiSqlClient.SqlParam> orderedParams() {
            if (params.isEmpty()) {
                return Collections.emptyList();
            }
            List<ApiSqlClient.SqlParam> ordered = new ArrayList<>();
            int max = Collections.max(params.keySet());
            for (int i = 1; i <= max; i++) {
                ordered.add(params.getOrDefault(i, new ApiSqlClient.SqlParam(null, "null")));
            }
            return ordered;
        }

        private ApiSqlClient.SqlParam toParam(String setter, Object[] args) {
            if ("setNull".equals(setter)) return new ApiSqlClient.SqlParam(null, "null");
            Object value = args[1];
            if (value instanceof Timestamp || value instanceof Date) {
                return new ApiSqlClient.SqlParam(String.valueOf(value), "string");
            }
            if (value instanceof Integer || value instanceof Long) {
                return new ApiSqlClient.SqlParam(value, "int");
            }
            if (value instanceof Boolean) {
                return new ApiSqlClient.SqlParam(value, "boolean");
            }
            if (value instanceof Number) {
                return new ApiSqlClient.SqlParam(value, "number");
            }
            return new ApiSqlClient.SqlParam(value == null ? null : String.valueOf(value), value == null ? "null" : "string");
        }
    }

    private static final class ResultSetHandler implements InvocationHandler {
        private final List<Map<String, Object>> rows;
        private final List<String> columns;
        private int index = -1;
        private Object lastValue;
        private boolean closed;

        private ResultSetHandler(List<String> columns, List<Map<String, Object>> rows) {
            this.rows = rows;
            this.columns = columns.isEmpty() && !rows.isEmpty() ? new ArrayList<>(rows.get(0).keySet()) : new ArrayList<>(columns);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("next".equals(name)) return ++index < rows.size();
            if ("close".equals(name)) {
                closed = true;
                return null;
            }
            if ("isClosed".equals(name)) return closed;
            if ("wasNull".equals(name)) return lastValue == null;
            if ("getMetaData".equals(name)) return resultSetMetaData(columns);
            if ("getString".equals(name)) return valueAsString(value(args[0]));
            if ("getObject".equals(name)) return value(args[0]);
            if ("getInt".equals(name)) return ApiSqlClient.toInt(value(args[0]));
            if ("getLong".equals(name)) return (long) ApiSqlClient.toInt(value(args[0]));
            if ("getDouble".equals(name)) return toDouble(value(args[0]));
            if ("getBoolean".equals(name)) return toBoolean(value(args[0]));
            if ("getTimestamp".equals(name)) return toTimestamp(value(args[0]));
            if ("getDate".equals(name)) return toDate(value(args[0]));
            if ("toString".equals(name)) return "HttpJdbcBridgeResultSet";
            return defaultValue(method);
        }

        private Object value(Object key) throws SQLException {
            if (index < 0 || index >= rows.size()) {
                throw new SQLException("ResultSet fuera de posicion");
            }
            String column = key instanceof Integer ? columns.get((Integer) key - 1) : String.valueOf(key);
            lastValue = rows.get(index).get(column);
            return lastValue;
        }
    }

    private static final class ResultSetMetaDataHandler implements InvocationHandler {
        private final List<String> columns;

        private ResultSetMetaDataHandler(List<String> columns) {
            this.columns = columns;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getColumnCount".equals(name)) return columns.size();
            if ("getColumnName".equals(name) || "getColumnLabel".equals(name)) return columns.get((Integer) args[0] - 1);
            if ("getColumnTypeName".equals(name)) return "VARCHAR";
            if ("getColumnClassName".equals(name)) return String.class.getName();
            return defaultValue(method);
        }
    }

    private static ResultSet resultSet(List<Map<String, Object>> rows) {
        return resultSet(Collections.emptyList(), rows);
    }

    private static ResultSet resultSet(List<String> columns, List<Map<String, Object>> rows) {
        return proxy(ResultSet.class, new ResultSetHandler(columns, rows));
    }

    private static ResultSetMetaData resultSetMetaData(List<String> columns) {
        return proxy(ResultSetMetaData.class, new ResultSetMetaDataHandler(columns));
    }

    private static List<Map<String, Object>> generatedKeyRows(String id) {
        if (id == null || id.isBlank() || "0".equals(id)) {
            return Collections.emptyList();
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("GENERATED_KEY", id);
        return Collections.singletonList(row);
    }

    private static boolean isQuery(String sql) {
        String normalized = sql == null ? "" : sql.trim().toLowerCase();
        return normalized.startsWith("select") || normalized.startsWith("show") || normalized.startsWith("describe") || normalized.startsWith("desc");
    }

    private static String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value == null || String.valueOf(value).isBlank()) return 0.0;
        return Double.parseDouble(String.valueOf(value));
    }

    private static boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        String text = String.valueOf(value);
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    private static Timestamp toTimestamp(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return Timestamp.valueOf(String.valueOf(value).replace('T', ' '));
    }

    private static Date toDate(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return Date.valueOf(String.valueOf(value).substring(0, 10));
    }

    private static Object defaultValue(Method method) {
        Class<?> type = method.getReturnType();
        if (type == Void.TYPE) return null;
        if (type == Boolean.TYPE) return false;
        if (type == Byte.TYPE) return (byte) 0;
        if (type == Short.TYPE) return (short) 0;
        if (type == Integer.TYPE) return 0;
        if (type == Long.TYPE) return 0L;
        if (type == Float.TYPE) return 0f;
        if (type == Double.TYPE) return 0d;
        if ("unwrap".equals(method.getName())) return null;
        if ("isWrapperFor".equals(method.getName())) return false;
        if (type == String.class) return "";
        if (method.getExceptionTypes().length > 0) {
            return null;
        }
        return null;
    }
}
