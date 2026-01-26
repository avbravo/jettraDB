package io.jettra.ui.component;

public class Password extends InputText {
    
    public Password(String id) {
        super(id);
        this.setType("password");
        this.setPlaceholder("••••••••");
    }
}
