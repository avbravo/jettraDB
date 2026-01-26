package io.jettra.ui.component;

public class Badge extends Component {
    private String label;
    private String color = "blue"; // blue, gray, red, green, yellow, indigo, purple, pink

    public Badge(String id, String label) {
        super(id);
        this.label = label;
    }

    public void setColor(String color) { this.color = color; }

    @Override
    public String render() {
        String baseClass = "text-xs font-medium mr-2 px-2.5 py-0.5 rounded";
        String colorClass = switch (color) {
            case "red" -> "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300";
            case "green" -> "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300";
            case "yellow" -> "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300";
            case "indigo" -> "bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300";
            case "purple" -> "bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300";
            case "pink" -> "bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-300";
            case "gray" -> "bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300";
            default -> "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300";
        };
        
        return String.format("<span id='%s' class='%s %s'%s>%s</span>", id, baseClass, colorClass, renderAttributes(), label);
    }
}
