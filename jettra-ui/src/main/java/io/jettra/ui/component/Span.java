package io.jettra.ui.component;

public class Span extends Container {
    private String text;

    public Span(String id) {
        super(id);
    }

    public Span(String id, String text) {
        super(id);
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String render() {
        return String.format("<span id='%s' class='%s'%s>%s%s</span>",
                id, styleClass != null ? styleClass : "", renderAttributes(),
                text != null ? text : "", renderChildren());
    }
}
