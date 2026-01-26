package io.jettra.ui.component;

public class Button extends Component {
    private String label;

    public Button(String id, String label) {
        super(id);
        this.label = label;
        this.styleClass = "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800";
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String render() {
        // HTMX attributes are handled by renderAttributes()
        return String.format("<button id='%s' class='%s'%s>%s</button>", id, styleClass, renderAttributes(), label);
    }
}
