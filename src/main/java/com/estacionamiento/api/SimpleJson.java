package com.estacionamiento.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJson {
    private SimpleJson() {
    }

    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = parse(json);
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private static final class Parser {
        private final String json;
        private int index;

        private Parser(String json) {
            this.json = json == null ? "" : json;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= json.length()) {
                return null;
            }
            char c = json.charAt(index);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (json.startsWith("true", index)) {
                index += 4;
                return Boolean.TRUE;
            }
            if (json.startsWith("false", index)) {
                index += 5;
                return Boolean.FALSE;
            }
            if (json.startsWith("null", index)) {
                index += 4;
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            index++;
            skipWhitespace();
            while (index < json.length() && json.charAt(index) != '}') {
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (index < json.length() && json.charAt(index) == ',') {
                    index++;
                    skipWhitespace();
                }
            }
            expect('}');
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            index++;
            skipWhitespace();
            while (index < json.length() && json.charAt(index) != ']') {
                list.add(parseValue());
                skipWhitespace();
                if (index < json.length() && json.charAt(index) == ',') {
                    index++;
                    skipWhitespace();
                }
            }
            expect(']');
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < json.length()) {
                char c = json.charAt(index++);
                if (c == '"') {
                    break;
                }
                if (c == '\\' && index < json.length()) {
                    char escaped = json.charAt(index++);
                    switch (escaped) {
                        case '"': builder.append('"'); break;
                        case '\\': builder.append('\\'); break;
                        case '/': builder.append('/'); break;
                        case 'b': builder.append('\b'); break;
                        case 'f': builder.append('\f'); break;
                        case 'n': builder.append('\n'); break;
                        case 'r': builder.append('\r'); break;
                        case 't': builder.append('\t'); break;
                        case 'u':
                            String hex = json.substring(index, Math.min(index + 4, json.length()));
                            builder.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                            break;
                        default: builder.append(escaped);
                    }
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        private Number parseNumber() {
            int start = index;
            while (index < json.length() && "-+.0123456789eE".indexOf(json.charAt(index)) >= 0) {
                index++;
            }
            String number = json.substring(start, index);
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number);
            }
            return Long.parseLong(number);
        }

        private void skipWhitespace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index < json.length() && json.charAt(index) == expected) {
                index++;
            }
        }
    }
}
