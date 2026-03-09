package io.jettra.ui.component;

public class Badge extends Component {
    private String label;

    public Badge(String id, String label) {
        super(id);
        this.label = label;
    }

    public Badge setColor(String color) {
        return this;
    }

    @Override
    public String render() {
        String baseClass = "j3d-badge text-xs font-medium mr-2 px-2.5 py-0.5 rounded";
        String colorClass = ""; // Colors are handled by j3d-badge now, or can be extended

        return String.format("<span id='%s' class='%s %s'%s>%s</span>", id, baseClass, colorClass, renderAttributes(),
                label);
    }
}
