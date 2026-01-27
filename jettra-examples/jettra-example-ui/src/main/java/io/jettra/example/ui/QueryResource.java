package io.jettra.example.ui;

import io.jettra.ui.component.*;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard/query")
public class QueryResource {

    @jakarta.ws.rs.core.Context
    jakarta.ws.rs.core.HttpHeaders headers;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getQueryView() {
        Div content = new Div("query-view");
        content.setStyleClass("max-width: 1200px; margin: 0 auto;");

        Label pageTitle = new Label("query-title", "SQL Query Console");
        pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
        content.addComponent(pageTitle);

        Card queryCard = new Card("query-card");

        Div description = new Div("query-desc");
        description.setStyleClass("text-gray-400 mb-4");
        description.addComponent(new Label("lbl-desc",
                "Execute SQL commands across all engines. Supported: SELECT, INSERT, UPDATE, DELETE."));
        queryCard.addComponent(description);

        TextArea sqlInput = new TextArea("sql-input");
        sqlInput.setPlaceholder("SELECT * FROM database.collection");
        sqlInput.setRows(6);
        sqlInput.setStyleClass(
                "w-full h-32 bg-slate-900/50 border border-slate-700 text-emerald-400 rounded-lg p-4 font-mono focus:ring-2 focus:ring-indigo-500 outline-none");
        queryCard.addComponent(sqlInput);

        Div optionsDiv = new Div("query-options");
        optionsDiv.setStyleClass("flex gap-6 items-center my-4");

        Label resolveRefsLabel = new Label("lbl-resolve-refs",
                "<label class='flex items-center gap-2 cursor-pointer text-slate-400 text-sm'>" +
                        "<input type='checkbox' id='sql-resolve-refs' class='w-4 h-4 rounded border-slate-700 bg-slate-800 text-indigo-600 focus:ring-indigo-500'> "
                        +
                        "Resolve References</label>");
        optionsDiv.addComponent(resolveRefsLabel);
        queryCard.addComponent(optionsDiv);

        Div buttonsDiv = new Div("query-buttons");
        buttonsDiv.setStyleClass("flex gap-3");

        Button runBtn = new Button("btn-run-query", "Run Query");
        runBtn.setStyleClass(
                "px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all");
        runBtn.addAttribute("onclick", "executeSqlQuery()");
        buttonsDiv.addComponent(runBtn);

        Button clearBtn = new Button("btn-clear-query", "Clear");
        clearBtn.setStyleClass(
                "px-6 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        clearBtn.addAttribute("onclick", "document.getElementById('sql-input').value = ''");
        buttonsDiv.addComponent(clearBtn);

        queryCard.addComponent(buttonsDiv);
        content.addComponent(queryCard);

        // Result container
        Div resultContainer = new Div("query-result-container");
        resultContainer.setStyleClass("hidden mt-8");

        Div resultHeader = new Div("result-header");
        resultHeader.setStyleClass("flex justify-between items-center mb-4");
        resultHeader.addComponent(
                new Label("lbl-res", "<h3 class='text-lg font-semibold text-white'>Execution Result</h3>"));

        Label statusBadge = new Label("query-status-badge", "SUCCESS");
        statusBadge.setStyleClass(
                "text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 uppercase font-bold tracking-wider");
        resultHeader.addComponent(statusBadge);
        resultContainer.addComponent(resultHeader);

        Card resultCard = new Card("result-card");
        resultCard.setStyleClass("p-0 overflow-hidden bg-slate-950 border border-slate-800");

        Div resultContent = new Div("query-result-content");
        resultContent.setStyleClass(
                "p-4 font-mono text-sm text-emerald-400 whitespace-pre-wrap max-h-[500px] overflow-y-auto");
        resultCard.addComponent(resultContent);

        resultContainer.addComponent(resultCard);
        content.addComponent(resultContainer);

        // Script for execution
        content.addComponent(new Label("query-script",
                "<script>" +
                        "async function executeSqlQuery() {" +
                        "  const sql = document.getElementById('sql-input').value.trim();" +
                        "  if (!sql) return;" +
                        "  const container = document.getElementById('query-result-container');" +
                        "  const content = document.getElementById('query-result-content');" +
                        "  const badge = document.getElementById('query-status-badge');" +
                        "  container.classList.remove('hidden');" +
                        "  content.innerText = 'Executing query...';" +
                        "  badge.innerText = 'PENDING';" +
                        "  badge.className = 'text-[10px] px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-500 border border-amber-500/20 uppercase font-bold tracking-wider';"
                        +
                        "  try {" +
                        "    const resolveRefs = document.getElementById('sql-resolve-refs').checked;" +
                        "    const response = await fetch('/api/v1/sql', {" +
                        "      method: 'POST'," +
                        "      headers: { 'Content-Type': 'application/json' }," +
                        "      body: JSON.stringify({ sql, resolveRefs })" +
                        "    });" +
                        "    const data = await response.json();" +
                        "    content.innerText = JSON.stringify(data, null, 2);" +
                        "    if (response.ok) {" +
                        "      badge.innerText = 'SUCCESS';" +
                        "      badge.className = 'text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 uppercase font-bold tracking-wider';"
                        +
                        "    } else {" +
                        "      badge.innerText = 'ERROR ' + response.status;" +
                        "      badge.className = 'text-[10px] px-2.5 py-0.5 rounded-full bg-rose-500/10 text-rose-500 border border-rose-500/20 uppercase font-bold tracking-wider';"
                        +
                        "    }" +
                        "  } catch (e) {" +
                        "    content.innerText = 'Error: ' + e.message;" +
                        "    badge.innerText = 'FAILED';" +
                        "    badge.className = 'text-[10px] px-2.5 py-0.5 rounded-full bg-rose-500/10 text-rose-500 border border-rose-500/20 uppercase font-bold tracking-wider';"
                        +
                        "  }" +
                        "}" +
                        "</script>"));

        return Response.ok(content.render()).build();
    }
}
