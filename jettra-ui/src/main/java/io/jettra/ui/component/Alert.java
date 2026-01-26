package io.jettra.ui.component;

public class Alert extends Container {
    private String type = "info"; // info, danger, success, warning

    public Alert(String id, String message) {
        super(id);
        this.addComponent(new Label(id + "-text", message));
    }

    public void setType(String type) { this.type = type; }

    @Override
    public String render() {
        String baseClass = "p-4 mb-4 text-sm rounded-lg";
        String colorClass = switch (type) {
            case "danger" -> "text-red-800 bg-red-50 dark:bg-gray-800 dark:text-red-400";
            case "success" -> "text-green-800 bg-green-50 dark:bg-gray-800 dark:text-green-400";
            case "warning" -> "text-yellow-800 bg-yellow-50 dark:bg-gray-800 dark:text-yellow-300";
            default -> "text-blue-800 bg-blue-50 dark:bg-gray-800 dark:text-blue-400";
        };
        
        return String.format("<div id='%s' class='%s %s %s' role='alert'%s>%s</div>", 
            id, baseClass, colorClass, styleClass, renderAttributes(), renderChildren());
    }
}
