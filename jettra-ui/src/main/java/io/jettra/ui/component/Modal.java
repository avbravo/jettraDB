package io.jettra.ui.component;

public class Modal extends Container {
    private String title;
    private Container footer;

    public Modal(String id, String title) {
        super(id);
        this.title = title;
        this.styleClass = "fixed top-0 left-0 right-0 z-50 hidden w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full";
        this.addAttribute("tabindex", "-1");
        this.addAttribute("aria-hidden", "true");
        this.footer = new Div(id + "-footer");
        this.footer.setStyleClass("flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600");
    }

    public void setFooter(Container footer) { this.footer = footer; }
    public void addFooterComponent(Component component) {
        footer.addComponent(component);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<div id='%s' %s class='%s'>", id, renderAttributes(), styleClass));
        sb.append("<div class='relative w-full max-w-2xl max-h-full'>");
        // Modal content
        sb.append("<div class='relative bg-white rounded-lg shadow dark:bg-gray-700'>");
        // Modal header
        sb.append("<div class='flex items-start justify-between p-4 border-b rounded-t dark:border-gray-600'>");
        sb.append(String.format("<h3 class='text-xl font-semibold text-gray-900 dark:text-white'>%s</h3>", title));
        sb.append("<button type='button' class='text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ml-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white' data-modal-hide='").append(id).append("'>");
        sb.append("<svg class='w-3 h-3' aria-hidden='true' xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 14 14'><path stroke='currentColor' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6'/></svg>");
        sb.append("<span class='sr-only'>Close modal</span>");
        sb.append("</button>");
        sb.append("</div>");
        // Modal body
        sb.append("<div class='p-6 space-y-6'>");
        sb.append(renderChildren());
        sb.append("</div>");
        // Modal footer
        if (!footer.getChildren().isEmpty()) {
            sb.append(footer.render());
        }
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
