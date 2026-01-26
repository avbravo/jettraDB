package io.jettra.ui.component;

public class Card extends Container {
    private String title;
    private String subTitle;
    private String headerImage;

    public Card(String id) {
        super(id);
        this.styleClass = "futu-card p-6";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<div id='%s' class='%s'%s>", id, styleClass, renderAttributes()));

        if (headerImage != null) {
            sb.append(String.format("<img class='rounded-t-lg' src='%s' alt='' />", headerImage));
        }

        if (title != null) {
            sb.append("<h5 class='mb-2 text-2xl font-bold tracking-tight text-gray-900 dark:text-white'>").append(title)
                    .append("</h5>");
        }

        if (subTitle != null) {
            sb.append("<p class='font-normal text-gray-700 dark:text-gray-400'>").append(subTitle).append("</p>");
        }

        sb.append(renderChildren());
        sb.append("</div>");
        return sb.toString();
    }
}
