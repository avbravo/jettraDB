package io.jettra.ui.component;

import java.util.LinkedHashMap;
import java.util.Map;

public class SelectOne extends Component {
    private Map<String, String> options = new LinkedHashMap<>();
    private String selectedValue;

    public SelectOne(String id) {
        super(id);
        this.styleClass = "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500";
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public void addOption(String value, String label) {
        options.put(value, label);
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        String nameAttr = attributes.containsKey("name") ? "" : String.format(" name='%s'", id);
        sb.append(String.format("<select id='%s'%s class='%s'%s>", id, nameAttr, styleClass, renderAttributes()));
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String selected = entry.getKey().equals(selectedValue) ? " selected" : "";
            sb.append(String.format("<option value='%s'%s>%s</option>", entry.getKey(), selected, entry.getValue()));
        }
        sb.append("</select>");
        return sb.toString();
    }
}
