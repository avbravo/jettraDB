package io.jettra.driver;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private String collection;
    private final List<String> conditions = new ArrayList<>();

    private Query() {}

    public static Query find() {
        return new Query();
    }

    public Query from(String collection) {
        this.collection = collection;
        return this;
    }

    public String getCollection() {
        return collection;
    }

    public Condition field(String name) {
        return new Condition(this, name);
    }

    public static class Condition {
        private final Query parent;
        private final String field;

        Condition(Query parent, String field) {
            this.parent = parent;
            this.field = field;
        }

        public Query eq(Object value) {
            parent.conditions.add("\"" + field + "\": " + formatValue(value));
            return parent;
        }

        public Query lt(Object value) {
            parent.conditions.add("\"" + field + "\": {\"$lt\": " + formatValue(value) + "}");
            return parent;
        }

        public Query gt(Object value) {
            parent.conditions.add("\"" + field + "\": {\"$gt\": " + formatValue(value) + "}");
            return parent;
        }

        public Query ne(Object value) {
            parent.conditions.add("\"" + field + "\": {\"$ne\": " + formatValue(value) + "}");
            return parent;
        }

        private String formatValue(Object value) {
            if (value instanceof String) return "\"" + value + "\"";
            return value.toString();
        }
    }

    public String build() {
        return "{" + String.join(", ", conditions) + "}";
    }
}
