package io.jettra.ui.component;

public class Navbar extends Container {
    private String brandName = "JettraDB";
    private String brandLogo = "/logo/jettra-logo.png";
    private Container rightContent;

    public Navbar(String id) {
        super(id);
        this.styleClass = "border-b border-white/10 px-4 py-2.5 fixed left-0 right-0 top-0 z-50 transition-all duration-300";
        this.addAttribute("style", "background: rgba(15, 23, 42, 0.9); backdrop-filter: blur(10px);");
        this.rightContent = new Div(id + "-right");
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setBrandLogo(String brandLogo) {
        this.brandLogo = brandLogo;
    }

    public void setRightContent(Container rightContent) {
        this.rightContent = rightContent;
    }

    public void addRightComponent(Component component) {
        rightContent.addComponent(component);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<nav id='%s' class='%s'%s>", id, styleClass, renderAttributes()));
        sb.append("<div class='flex flex-wrap justify-between items-center'>");

        // Left side: Brand + Sidebar Toggle
        sb.append("<div class='flex justify-start items-center'>");
        sb.append(
                "<button data-drawer-target='drawer-navigation' data-drawer-toggle='drawer-navigation' aria-controls='drawer-navigation' class='p-2 mr-2 text-slate-400 rounded-lg md:hidden hover:bg-white/5 focus:outline-none'>");
        sb.append(
                "<svg class='w-6 h-6' fill='none' stroke='currentColor' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z' clip-rule='evenodd'></path></svg>");
        sb.append("</button>");

        sb.append("<a href='/' class='flex items-center justify-between mr-4'>");
        if (brandLogo != null) {
            sb.append(String.format("<img src='%s' class='mr-3 h-8' alt='%s Logo' />", brandLogo, brandName));
        }
        sb.append(String.format("<span class='self-center text-2xl font-bold brand-text whitespace-nowrap'>%s</span>",
                brandName));
        sb.append("</a>");
        sb.append("</div>");

        // Right side: Profile, etc.
        sb.append("<div class='flex items-center lg:order-2'>");
        sb.append(rightContent.render());
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</nav>");
        return sb.toString();
    }
}
