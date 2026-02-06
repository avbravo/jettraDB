package io.jettra.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryBuilder {
    private final Map<String, Object> query = new HashMap<>();

    public static QueryBuilder query() {
        return new QueryBuilder();
    }

    public QueryBuilder eq(String field, Object value) {
        query.put(field, value);
        return this;
    }

    public QueryBuilder gt(String field, Object value) {
        query.put(field, Map.of("$gt", value));
        return this;
    }

    public QueryBuilder lt(String field, Object value) {
        query.put(field, Map.of("$lt", value));
        return this;
    }

    public QueryBuilder gte(String field, Object value) {
        query.put(field, Map.of("$gte", value));
        return this;
    }

    public QueryBuilder lte(String field, Object value) {
        query.put(field, Map.of("$lte", value));
        return this;
    }

    public QueryBuilder ne(String field, Object value) {
        query.put(field, Map.of("$ne", value));
        return this;
    }

    public String build() {
        return "{" + query.entrySet().stream()
                .map(e -> String.format("\"%s\": %s", e.getKey(), formatValue(e.getValue())))
                .collect(Collectors.joining(", ")) + "}";
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return "{" + map.entrySet().stream()
                    .map(e -> String.format("\"%s\": %s", e.getKey(), formatValue(e.getValue())))
                    .collect(Collectors.joining(", ")) + "}";
        }
        return String.valueOf(value);
    }
}
