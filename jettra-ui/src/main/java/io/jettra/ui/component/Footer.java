package io.jettra.ui.component;

public class Footer extends Container {

    public Footer() {
        super("main-footer");
        // Matches .footer class in index.html, but using Tailwind utility classes for
        // consistency
        // footer class: margin-top: auto; padding-top: 2rem; border-top: 1px ...
        // text-align: center; color: ... flex-direction: column ...
        this.styleClass = "mt-auto pt-8 border-t border-gray-100 dark:border-gray-800 text-center text-sm text-slate-500 flex flex-col items-center gap-2 pb-4";
    }

    @Override
    public String render() {
        // We will hardcode the content structure as requested to match jettra-web's
        // specific look
        // logic could be added to make it dynamic if needed, but for now exact match is
        // goal.

        return String.format(
                "<footer id='%s' class='pt-8 border-t border-white/5 text-center text-sm text-slate-500 flex flex-col items-center gap-2'>"
                        +
                        "  <div class='flex items-center gap-2 justify-center opacity-40'>" +
                        "    <img src='/logo/jettra-logo.png' alt='Logo' class='w-5 h-5'>" +
                        "    <span class='font-semibold'>JettraDB</span>" +
                        "  </div>" +
                        "  <div>&copy; 2024 JettraDB. All rights reserved.</div>" +
                        "  <div class='text-[10px] text-slate-600 uppercase tracking-widest'>v1.0.0-beta</div>" +
                        "</footer>",
                id);
    }
}
