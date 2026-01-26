package io.jettra.ui.template;

import io.jettra.ui.component.Container;

public class Template {
    private Container top;
    private Container left;
    private Container center;
    private Container footer;

    public Container getTop() {
        return top;
    }

    public void setTop(Container top) {
        this.top = top;
    }

    public Container getLeft() {
        return left;
    }

    public void setLeft(Container left) {
        this.left = left;
    }

    public Container getCenter() {
        return center;
    }

    public void setCenter(Container center) {
        this.center = center;
    }

    public Container getFooter() {
        return footer;
    }

    public void setFooter(Container footer) {
        this.footer = footer;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class='min-h-screen flex flex-col'>");

        if (top != null) {
            sb.append(top.render());
        }

        sb.append("<div class='flex flex-1 pt-12'>"); // Adjust padding for fixed top bar

        if (left != null) {
            sb.append(left.render());
        }

        // Main Content Area
        String mainClass = left != null ? "flex-1 flex flex-col p-6 md:ml-64 transition-all"
                : "flex-1 flex flex-col p-6 transition-all";

        sb.append("<main class='" + mainClass + "'>");

        sb.append("<div class='flex-1'>");
        if (center != null) {
            sb.append(center.render());
        }
        sb.append("</div>");

        if (footer != null) {
            sb.append("<div class='mt-auto pt-10 pb-4'>");
            sb.append(footer.render());
            sb.append("</div>");
        }

        sb.append("</main>");
        sb.append("</div>"); // End flex container
        sb.append("</div>"); // End min-h-screen

        return sb.toString();
    }
}
