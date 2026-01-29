package io.jettra.example.ui.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Collection {
    private String name;
    private String engine; // "Document", "Column", etc.

    public Collection() {
    }

    public Collection(String name, String engine) {
        this.name = name;
        this.engine = engine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }
}
