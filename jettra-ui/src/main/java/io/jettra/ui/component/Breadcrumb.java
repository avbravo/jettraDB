package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public class Breadcrumb extends Component {
    private List<BreadcrumbItem> items = new ArrayList<>();

    public Breadcrumb(String id) {
        super(id);
    }

    public void addItem(String label, String url) {
        items.add(new BreadcrumbItem(label, url));
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("<nav class='flex' aria-label='Breadcrumb'>");
        sb.append("<ol class='inline-flex items-center space-x-1 md:space-x-3'>");
        
        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);
            sb.append("<li class='inline-flex items-center'>");
            if (i > 0) {
                sb.append("<svg class='w-3 h-3 text-gray-400 mx-1' aria-hidden='true' xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 6 10'><path stroke='currentColor' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m1 9 4-4-4-4'/></svg>");
            }
            if (item.url != null) {
                sb.append(String.format("<a href='%s' class='inline-flex items-center text-sm font-medium text-gray-700 hover:text-blue-600 dark:text-gray-400 dark:hover:text-white'>%s</a>", item.url, item.label));
            } else {
                sb.append(String.format("<span class='ml-1 text-sm font-medium text-gray-500 md:ml-2 dark:text-gray-400'>%s</span>", item.label));
            }
            sb.append("</li>");
        }

        sb.append("</ol>");
        sb.append("</nav>");
        return sb.toString();
    }

    private static class BreadcrumbItem {
        String label;
        String url;

        BreadcrumbItem(String label, String url) {
            this.label = label;
            this.url = url;
        }
    }
}
