package io.jettra.ui.component;

public class Div extends Container {
    public Div(String id) {
        super(id);
    }
    
    @Override
    public String render() {
        return String.format("<div id='%s' class='%s'%s>%s</div>", 
            id, styleClass, renderAttributes(), renderChildren());
    }
}
