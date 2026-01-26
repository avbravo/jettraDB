package io.jettra.ui.template;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String title = "Jettra Application";
    private String content = "";
    private List<String> scripts = new ArrayList<>();
    private List<String> scriptContents = new ArrayList<>();
    private List<String> cssLinks = new ArrayList<>();

    public Page() {
        // Default dependencies
        addCss("https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600&display=swap");
        addScript("https://cdn.tailwindcss.com");

        // Configure tailwind and global styles
        addScriptContent(
                "tailwind.config = { darkMode: 'class', theme: { extend: { colors: { primary: {'50':'#eff6ff','100':'#dbeafe','200':'#bfdbfe','300':'#93c5fd','400':'#60a5fa','500':'#6366f1','600':'#4f46e5','700':'#4338ca','800':'#3730a3','900':'#312e81','950':'#1e1b4b'} } } } }");

        addScript("https://unpkg.com/htmx.org@2.0.4");
        addCss("https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.css");

        // Global Futuristic styles
        addScriptContent("""
                    const style = document.createElement('style');
                    style.textContent = `
                        :root {
                            --primary: #6366f1;
                            --secondary: #a855f7;
                            --bg: #0f172a;
                            --header-bg: rgba(15, 23, 42, 0.9);
                            --text: #f8fafc;
                        }
                        body {
                            font-family: 'Outfit', sans-serif !important;
                            background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%) !important;
                            color: var(--text) !important;
                            min-height: 100vh;
                        }
                        .brand-text {
                            background: linear-gradient(to right, #818cf8, #c084fc);
                            -webkit-background-clip: text;
                            background-clip: text;
                            -webkit-text-fill-color: transparent;
                        }
                        .futu-card {
                            background: rgba(30, 41, 59, 0.7) !important;
                            backdrop-filter: blur(12px);
                            border: 1px solid rgba(255, 255, 255, 0.1) !important;
                            border-radius: 1rem;
                            transition: all 0.3s ease;
                        }
                        .futu-card:hover {
                            transform: translateY(-5px);
                            border-color: rgba(99, 102, 241, 0.4) !important;
                            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3), 0 8px 10px -6px rgba(0, 0, 0, 0.3);
                        }
                        .status-dot {
                            width: 10px;
                            height: 10px;
                            border-radius: 50%;
                            display: inline-block;
                        }
                    `;
                    document.head.appendChild(style);
                """);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void addScript(String src) {
        scripts.add(src);
    }

    public void addScriptContent(String content) {
        scriptContents.add(content);
    }

    public void addCss(String href) {
        cssLinks.add(href);
    }

    public String render() {
        StringBuilder head = new StringBuilder();
        for (String css : cssLinks) {
            head.append(String.format("<link href=\"%s\" rel=\"stylesheet\" />\n", css));
        }
        for (String script : scripts) {
            head.append(String.format("<script src=\"%s\"></script>\n", script));
        }
        for (String content : scriptContents) {
            head.append(String.format("<script>%s</script>\n", content));
        }

        // Flowbite JS needs to be at end of body usually, but we can put it here or
        // keep it separate in the logic
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
