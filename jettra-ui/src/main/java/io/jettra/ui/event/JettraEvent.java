package io.jettra.ui.event;

import io.jettra.ui.component.Component;

public class JettraEvent {
    private Component source;
    private String type;
    private Object payload;

    public JettraEvent(Component source, String type, Object payload) {
        this.source = source;
        this.type = type;
        this.payload = payload;
    }

    public Component getSource() { return source; }
    public String getType() { return type; }
    public Object getPayload() { return payload; }
}
