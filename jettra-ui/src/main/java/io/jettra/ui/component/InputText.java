package io.jettra.ui.component;

public class InputText extends Component {
    private String value = "";
    private String placeholder = "";

    private String type = "text";

    public InputText(String id) {
        super(id);
        this.styleClass = "j3d-input block w-full p-2.5";
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String render() {
        String nameAttr = attributes.containsKey("name") ? "" : String.format(" name='%s'", id);
        return String.format("<input type='%s' id='%s'%s value='%s' placeholder='%s' class='%s'%s />", type, id,
                nameAttr, value, placeholder, styleClass, renderAttributes());
    }
}
