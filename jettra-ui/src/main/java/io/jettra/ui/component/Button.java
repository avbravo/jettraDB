package io.jettra.ui.component;

public class Button extends Component {
    private String label;

    public Button(String id, String label) {
        super(id);
        this.label = label;
        this.styleClass = "j3d-button";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String render() {
        // HTMX attributes are handled by renderAttributes()
        return String.format("<button id='%s' class='%s'%s>%s</button>", id, styleClass, renderAttributes(), label);
    }
}
