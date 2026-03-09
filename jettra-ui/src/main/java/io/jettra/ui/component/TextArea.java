package io.jettra.ui.component;

public class TextArea extends Component {
    private String value = "";
    private String placeholder = "";
    private int rows = 4;

    public TextArea(String id) {
        super(id);
        this.styleClass = "j3d-textarea block p-2.5 w-full text-sm rounded-lg";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public String render() {
        String nameAttr = attributes.containsKey("name") ? "" : String.format(" name='%s'", id);
        return String.format("<textarea id='%s'%s rows='%d' placeholder='%s' class='%s'%s>%s</textarea>",
                id, nameAttr, rows, placeholder, styleClass, renderAttributes(), value);
    }
}
