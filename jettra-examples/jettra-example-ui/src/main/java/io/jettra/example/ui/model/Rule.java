package io.jettra.example.ui.model;

public class Rule {
    private String name;
    private String condition;
    private String action;
    private boolean active;

    public Rule() {
    }

    public Rule(String name, String condition, String action, boolean active) {
        this.name = name;
        this.condition = condition;
        this.action = action;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
