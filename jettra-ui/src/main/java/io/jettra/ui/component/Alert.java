package io.jettra.ui.component;

public class Alert extends Container {
    private String type = "info"; // info, danger, success, warning

    public Alert(String id, String message) {
        super(id);
        this.addComponent(new Label(id + "-text", message));
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String render() {
        String baseClass = "j3d-alert p-4 mb-4 text-sm rounded-lg";
        String colorClass = switch (type) {
            case "danger" -> "j3d-alert-danger";
            case "success" -> "j3d-alert-success";
            case "warning" -> "j3d-alert-warning";
            default -> "j3d-alert-info";
        };

        return String.format("<div id='%s' class='%s %s %s' role='alert'%s>%s</div>",
                id, baseClass, colorClass, styleClass, renderAttributes(), renderChildren());
    }
}
