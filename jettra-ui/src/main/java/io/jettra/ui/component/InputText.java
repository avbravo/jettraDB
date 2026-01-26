package io.jettra.ui.component;

public class InputText extends Component {
    private String value = "";
    private String placeholder = "";

    private String type = "text";

    public InputText(String id) {
        super(id);
        this.styleClass = "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500";
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String render() {
        return String.format("<input type='%s' id='%s' name='%s' value='%s' placeholder='%s' class='%s'%s />", type, id, id, value, placeholder, styleClass, renderAttributes());
    }
}
