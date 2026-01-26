package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public class Tree extends Component {
    private List<TreeNode> nodes = new ArrayList<>();

    public Tree(String id) {
        super(id);
        this.styleClass = "tree-container space-y-2";
    }

    public void addNode(TreeNode node) {
        nodes.add(node);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<ul id='%s' class='%s'%s>", id, styleClass, renderAttributes()));
        for (TreeNode node : nodes) {
            sb.append(node.render());
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public static class TreeNode {
        private String label;
        private String icon;
        private List<TreeNode> children = new ArrayList<>();
        private String hxGet;
        private String hxTarget;

        public TreeNode(String label) {
            this.label = label;
        }

        public TreeNode(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }

        public void addChild(TreeNode child) {
            children.add(child);
        }

        public void setHxGet(String hxGet) { this.hxGet = hxGet; }
        public void setHxTarget(String hxTarget) { this.hxTarget = hxTarget; }

        public String render() {
            StringBuilder sb = new StringBuilder();
            sb.append("<li>");
            
            String attrs = "";
            if (hxGet != null) attrs += " hx-get='" + hxGet + "'";
            if (hxTarget != null) attrs += " hx-target='" + hxTarget + "'";

            sb.append("<div class='flex items-center p-2 text-base font-normal text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer'").append(attrs).append(">");
            if (icon != null) {
                sb.append(icon); // Assume HTML for icon
            }
            sb.append("<span class='ml-3'>").append(label).append("</span>");
            sb.append("</div>");

            if (!children.isEmpty()) {
                sb.append("<ul class='py-2 space-y-2 ml-4'>");
                for (TreeNode child : children) {
                    sb.append(child.render());
                }
                sb.append("</ul>");
            }
            sb.append("</li>");
            return sb.toString();
        }
    }
}
