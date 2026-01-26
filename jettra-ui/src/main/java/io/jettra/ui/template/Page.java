package io.jettra.ui.template;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String title = "Jettra Application";
    private String content = "";
    private List<String> scripts = new ArrayList<>();
    private List<String> cssLinks = new ArrayList<>();

    public Page() {
        // Default dependencies
        addScript("https://cdn.tailwindcss.com");
        addScript("https://unpkg.com/htmx.org@1.9.10");
        addCss("https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.css");
    }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    public void addScript(String src) { scripts.add(src); }
    public void addCss(String href) { cssLinks.add(href); }

    public String render() {
        StringBuilder head = new StringBuilder();
        for (String css : cssLinks) {
            head.append(String.format("<link href=\"%s\" rel=\"stylesheet\" />\n", css));
        }
        for (String script : scripts) {
            head.append(String.format("<script src=\"%s\"></script>\n", script));
        }

        // Flowbite JS needs to be at end of body usually, but we can put it here or keep it separate in the logic
        // For simplicity, let's keep the core structure requested by user
        
        return """
               <!DOCTYPE html>
               <html lang="en">
               <head>
                   <meta charset="UTF-8">
                   <meta name="viewport" content="width=device-width, initial-scale=1.0">
                   <title>%s</title>
                   %s
               </head>
               <body class="bg-gray-50 dark:bg-gray-900">
                   %s
                   <script src="https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.js"></script>
               </body>
               </html>
               """.formatted(title, head.toString(), content);
    }
}
