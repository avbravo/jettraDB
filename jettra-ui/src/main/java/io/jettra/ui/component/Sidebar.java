package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public class Sidebar extends Container {
    private List<SidebarItem> items = new ArrayList<>();

    public Sidebar(String id) {
        super(id);
        this.styleClass = "fixed top-0 left-0 z-40 w-64 h-screen pt-20 transition-transform -translate-x-full border-r border-gray-100/10 sm:translate-x-0";
        this.addAttribute("aria-label", "Sidebar");
    }

    public void addItem(SidebarItem item) {
        items.add(item);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "<aside id='%s' class='%s' style='background: rgba(15, 23, 42, 0.95); backdrop-filter: blur(10px);'%s>",
                id, styleClass, renderAttributes()));
        sb.append("<div class='h-full px-3 pb-4 overflow-y-auto'>");
        sb.append("<ul class='space-y-2 font-medium'>");

        for (SidebarItem item : items) {
            sb.append(item.render());
        }

        sb.append(renderChildren());
        sb.append("</ul>");
        sb.append("</div>");
        sb.append("</aside>");
        return sb.toString();
    }

    public static class SidebarItem {
        private String label;
        private String icon;
        private String hxGet;
        private String hxTarget;
        private String id;

        public SidebarItem(String id, String label, String icon) {
            this.id = id;
            this.label = label;
            this.icon = icon;
        }

        public void setHxGet(String hxGet) {
            this.hxGet = hxGet;
        }

        public void setHxTarget(String hxTarget) {
            this.hxTarget = hxTarget;
        }

        public String render() {
            String attrs = "";
            if (hxGet != null)
                attrs += " hx-get='" + hxGet + "'";
            if (hxTarget != null)
                attrs += " hx-target='" + hxTarget + "'";

            return String.format(
                    "<li>" +
                            "  <button id='%s' class='flex items-center w-full p-2 text-slate-300 transition duration-75 rounded-lg group hover:bg-indigo-600/20 hover:text-white'%s>"
                            +
                            "    %s" +
                            "    <span class='ml-3'>%s</span>" +
                            "  </button>" +
                            "</li>",
                    id, attrs, icon != null ? icon : "", label);
        }
    }
}
