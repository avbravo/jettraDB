package io.jettra.ui.component;

import io.jettra.ui.event.EventListener;
import io.jettra.ui.event.JettraEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Component {
    protected String id;
    protected String styleClass = "";
    protected Map<String, String> attributes = new HashMap<>();
    protected List<EventListener> listeners = new ArrayList<>();

    public Component(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getStyleClass() { return styleClass; }
    public void setStyleClass(String styleClass) { this.styleClass = styleClass; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public List<EventListener> getListeners() { return listeners; }
    public void setListeners(List<EventListener> listeners) { this.listeners = listeners; }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }
    
    // HTMX Helpers
    public void setHxGet(String url) { addAttribute("hx-get", url); }
    public void setHxPost(String url) { addAttribute("hx-post", url); }
    public void setHxPut(String url) { addAttribute("hx-put", url); }
    public void setHxDelete(String url) { addAttribute("hx-delete", url); }
    public void setHxTarget(String selector) { addAttribute("hx-target", selector); }
    public void setHxSwap(String swap) { addAttribute("hx-swap", swap); }
    public void setHxTrigger(String trigger) { addAttribute("hx-trigger", trigger); }
    public void setHxConfirm(String message) { addAttribute("hx-confirm", message); }
    public void setHxInclude(String selector) { addAttribute("hx-include", selector); }

    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }
    
    public void fireEvent(JettraEvent event) {
        for(EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    protected String renderAttributes() {
        if (attributes.isEmpty()) {
            return "";
        }
        return attributes.entrySet().stream()
                .map(e -> " " + e.getKey() + "=\"" + e.getValue() + "\"")
                .reduce("", (a, b) -> a + b);
    }

    public abstract String render();
}
