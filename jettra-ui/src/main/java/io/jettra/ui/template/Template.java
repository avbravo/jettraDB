package io.jettra.ui.template;

import io.jettra.ui.component.Container;
import io.jettra.ui.component.Navbar;
import io.jettra.ui.component.Sidebar;

public class Template {
    private Container top;
    private Container left;
    private Container center;
    private Container footer;

    public Container getTop() { return top; }
    public void setTop(Container top) { this.top = top; }

    public Container getLeft() { return left; }
    public void setLeft(Container left) { this.left = left; }

    public Container getCenter() { return center; }
    public void setCenter(Container center) { this.center = center; }

    public Container getFooter() { return footer; }
    public void setFooter(Container footer) { this.footer = footer; }

    public String render() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<div class='antialiased bg-gray-50 dark:bg-gray-900'>");
        
        if (top != null) {
            if (top instanceof Navbar) {
                sb.append(top.render());
            } else {
                sb.append("<nav class='fixed top-0 z-50 w-full bg-white border-b border-gray-200 dark:bg-gray-800 dark:border-gray-700'>");
                sb.append(top.render());
                sb.append("</nav>");
            }
        }
        
        if (left != null) {
            if (left instanceof Sidebar) {
                sb.append(left.render());
            } else {
                sb.append("<aside id='logo-sidebar' class='fixed top-0 left-0 z-40 w-64 h-screen pt-20 transition-transform -translate-x-full bg-white border-r border-gray-200 sm:translate-x-0 dark:bg-gray-800 dark:border-gray-700' aria-label='Sidebar'>");
                sb.append("<div class='h-full px-3 pb-4 overflow-y-auto bg-white dark:bg-gray-800'>");
                sb.append(left.render());
                sb.append("</div>");
                sb.append("</aside>");
            }
        }
        
        // Main Content Area
        String mainClass = left != null ? "p-4 md:ml-64 h-auto pt-20" : "p-4 h-auto pt-20";
        
        sb.append("<main class='" + mainClass + "'>");
        
        if (center != null) {
            sb.append(center.render());
        }
        
        if (footer != null) {
            sb.append("<div class='mt-4'>");
            sb.append(footer.render());
            sb.append("</div>");
        }
        
        sb.append("</main>");
        sb.append("</div>");
        
        return sb.toString();
    }
}
