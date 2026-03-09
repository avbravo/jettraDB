package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public class Table extends Component {
    private List<String> headers = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();

    public Table(String id) {
        super(id);
        this.styleClass = "j3d-table";
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public void addHeader(String header) {
        headers.add(header);
    }

    public void addRow(List<String> row) {
        rows.add(row);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<div class='relative overflow-x-auto'><table id='%s' class='%s'%s>", id, styleClass,
                renderAttributes()));

        // Header
        sb.append("<thead><tr>");
        for (String header : headers) {
            sb.append(String.format("<th scope='col'>%s</th>", header));
        }
        sb.append("</tr></thead>");

        // Body
        sb.append("<tbody>");
        for (List<String> row : rows) {
            sb.append("<tr>");
            for (String cell : row) {
                sb.append(String.format("<td class='px-6 py-4'>%s</td>", cell));
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");

        sb.append("</table></div>");
        return sb.toString();
    }
}
