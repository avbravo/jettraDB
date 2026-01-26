package io.jettra.ui.component;

public class Form extends Container {
    public Form(String id) {
        super(id);
    }

    @Override
    public String render() {
         return String.format("<form id='%s' class='%s'%s>%s</form>", id, styleClass, renderAttributes(), renderChildren());
    }
}
