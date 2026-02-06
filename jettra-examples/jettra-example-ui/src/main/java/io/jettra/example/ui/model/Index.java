package io.jettra.example.ui.model;

public class Index {
    private String name;
    private String field;
    private String type;

    public Index() {
    }

    public Index(String name, String field, String type) {
        this.name = name;
        this.field = field;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
