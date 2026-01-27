package io.jettra.ui.component;

public class TextArea extends Component {
    private String value = "";
    private String placeholder = "";
    private int rows = 4;

    public TextArea(String id) {
        super(id);
        this.styleClass = "block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500";
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
        return String.format("<textarea id='%s' name='%s' rows='%d' placeholder='%s' class='%s'%s>%s</textarea>",
                id, id, rows, placeholder, styleClass, renderAttributes(), value);
    }
}
