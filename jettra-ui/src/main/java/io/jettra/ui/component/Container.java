package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public abstract class Container extends Component {
    protected List<Component> children = new ArrayList<>();

    public Container(String id) {
        super(id);
    }

    public void addComponent(Component component) {
        children.add(component);
    }
    
    public List<Component> getChildren() { return children; }
    
    protected String renderChildren() {
        StringBuilder sb = new StringBuilder();
        for (Component child : children) {
            sb.append(child.render());
        }
        return sb.toString();
    }
}
