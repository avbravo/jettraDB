package io.jettra.ui.component;

public class Label extends Component {
    private String text;

    public Label(String id, String text) {
        super(id);
        this.text = text;
        this.styleClass = "block mb-2 text-sm font-medium text-gray-900 dark:text-white"; // Flowbite label
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @Override
    public String render() {
        return String.format("<label id='%s' class='%s'%s>%s</label>", id, styleClass, renderAttributes(), text);
    }
}
