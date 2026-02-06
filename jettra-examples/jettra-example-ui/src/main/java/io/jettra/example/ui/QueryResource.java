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

                Label pageTitle = new Label("query-title", "Query Console");
                pageTitle.setStyleClass("text-2xl font-bold text-gray-900 dark:text-white mb-6");
                content.addComponent(pageTitle);

                Card queryCard = new Card("query-card");

                // Language Toggle
                Div toggleDiv = new Div("lang-toggle");
                toggleDiv.setStyleClass(
                                "flex gap-2 mb-6 p-1 bg-slate-800/50 rounded-xl w-fit border border-slate-700/50");

                String btnBase = "px-6 py-2 rounded-lg font-bold transition-all text-sm uppercase tracking-wider ";

                Button sqlBtn = new Button("btn-sql-mode", "SQL");
                sqlBtn.setStyleClass(btnBase + "bg-indigo-600 text-white shadow-lg shadow-indigo-500/20");
                sqlBtn.addAttribute("onclick", "setQueryMode('sql')");
                toggleDiv.addComponent(sqlBtn);

                Button mongoBtn = new Button("btn-mongo-mode", "MongoDB");
                mongoBtn.setStyleClass(btnBase + "text-slate-400 hover:text-slate-200");
                mongoBtn.addAttribute("onclick", "setQueryMode('mongo')");
                toggleDiv.addComponent(mongoBtn);

                queryCard.addComponent(toggleDiv);

                Div description = new Div("query-desc");
                description.setStyleClass("text-gray-400 mb-4 h-10");
                description.addComponent(new Label("desc-text",
                                "<span id='query-mode-hint'>Execute SQL commands across all engines. Supported: SELECT, INSERT, UPDATE, DELETE.</span>"));
                queryCard.addComponent(description);

                TextArea queryInput = new TextArea("query-input");
                queryInput.setPlaceholder("SELECT * FROM shop.users");
                queryInput.setRows(6);
                queryInput.setStyleClass(
                                "w-full h-32 bg-slate-950/80 border border-slate-700 text-emerald-400 rounded-lg p-4 font-mono focus:ring-2 focus:ring-indigo-500 outline-none transition-all placeholder:text-slate-600");
                queryCard.addComponent(queryInput);

                Div optionsDiv = new Div("query-options");
                optionsDiv.setStyleClass("flex gap-6 items-center my-4");

                Label resolveRefsLabel = new Label("lbl-resolve-refs",
                                "<label class='flex items-center gap-3 cursor-pointer group'>" +
                                                "<div class='relative'>" +
                                                "<input type='checkbox' id='query-resolve-refs' class='sr-only peer'> "
                                                +
                                                "<div class='w-10 h-5 bg-slate-700 rounded-full peer peer-checked:bg-indigo-600 transition-colors'></div>"
                                                +
                                                "<div class='absolute left-1 top-1 w-3 h-3 bg-white rounded-full transition-transform peer-checked:translate-x-5'></div>"
                                                +
                                                "</div>" +
                                                "<span class='text-slate-400 group-hover:text-slate-200 text-sm font-medium transition-colors'>Resolve References</span></label>");
                optionsDiv.addComponent(resolveRefsLabel);
                queryCard.addComponent(optionsDiv);

                Div buttonsDiv = new Div("query-buttons");
                buttonsDiv.setStyleClass("flex gap-3");

                Button runBtn = new Button("btn-run-query", "Run Query");
                runBtn.setStyleClass(
                                "px-8 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all transform active:scale-95 shadow-lg shadow-indigo-600/20");
                runBtn.addAttribute("onclick", "executeQuery()");
                buttonsDiv.addComponent(runBtn);

                Button clearBtn = new Button("btn-clear-query", "Clear");
                clearBtn.setStyleClass(
                                "px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
                clearBtn.addAttribute("onclick", "document.getElementById('query-input').value = ''");
                buttonsDiv.addComponent(clearBtn);

                queryCard.addComponent(buttonsDiv);
                content.addComponent(queryCard);

                // Result container (Same as before but styled better)
                Div resultContainer = new Div("query-result-container");
                resultContainer.setStyleClass("hidden mt-8 animate-in fade-in slide-in-from-top-4 duration-500");

                Div resultHeader = new Div("result-header");
                resultHeader.setStyleClass("flex justify-between items-center mb-4");
                resultHeader.addComponent(
                                new Label("lbl-res",
                                                "<h3 class='text-lg font-semibold text-white flex items-center gap-2'><svg class='w-5 h-5 text-indigo-400' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'></path></svg> Execution Result</h3>"));

                Label statusBadge = new Label("query-status-badge", "SUCCESS");
                statusBadge.setStyleClass(
                                "text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-500 border border-emerald-500/20 uppercase font-bold tracking-wider transition-all");
                resultHeader.addComponent(statusBadge);
                resultContainer.addComponent(resultHeader);

                Card resultCard = new Card("result-card");
                resultCard.setStyleClass(
                                "p-0 overflow-hidden bg-slate-950 border border-slate-800 shadow-2xl rounded-xl");

                Div resultContent = new Div("query-result-content");
                resultContent.setStyleClass(
                                "p-4 font-mono text-sm text-emerald-400 whitespace-pre-wrap max-h-[600px] overflow-y-auto custom-scrollbar");
                resultCard.addComponent(resultContent);

                resultContainer.addComponent(resultCard);
                content.addComponent(resultContainer);

                // Script for execution and mode switching
                content.addComponent(new Label("query-script",
                                "<script>" +
                                                "let currentMode = 'sql';" +
                                                "function setQueryMode(mode) {" +
                                                "  currentMode = mode;" +
                                                "  const sqlBtn = document.getElementById('btn-sql-mode');" +
                                                "  const mongoBtn = document.getElementById('btn-mongo-mode');" +
                                                "  const hint = document.getElementById('query-mode-hint');" +
                                                "  const input = document.getElementById('query-input');" +
                                                "  const title = document.querySelector('#query-title');" +
                                                "  if (mode === 'sql') {" +
                                                "    sqlBtn.className = 'px-6 py-2 rounded-lg font-bold transition-all text-sm uppercase tracking-wider bg-indigo-600 text-white shadow-lg shadow-indigo-500/20';"
                                                +
                                                "    mongoBtn.className = 'px-6 py-2 rounded-lg font-bold transition-all text-sm uppercase tracking-wider text-slate-400 hover:text-slate-200';"
                                                +
                                                "    hint.innerText = 'Execute SQL commands across all engines. Supported: SELECT, INSERT, UPDATE, DELETE.';"
                                                +
                                                "    input.placeholder = 'SELECT * FROM shop.users';" +
                                                "    title.innerText = 'SQL Query Console';" +
                                                "  } else {" +
                                                "    mongoBtn.className = 'px-6 py-2 rounded-lg font-bold transition-all text-sm uppercase tracking-wider bg-indigo-600 text-white shadow-lg shadow-indigo-500/20';"
                                                +
                                                "    sqlBtn.className = 'px-6 py-2 rounded-lg font-bold transition-all text-sm uppercase tracking-wider text-slate-400 hover:text-slate-200';"
                                                +
                                                "    hint.innerText = 'Execute MongoDB commands. Example: db.users.find({age: {$gt: 20}})';"
                                                +
                                                "    input.placeholder = 'db.users.find({})';" +
                                                "    title.innerText = 'MongoDB Console';" +
                                                "  }" +
                                                "}" +
                                                "async function executeQuery() {" +
                                                "  const query = document.getElementById('query-input').value.trim();" +
                                                "  if (!query) return;" +
                                                "  const container = document.getElementById('query-result-container');"
                                                +
                                                "  const content = document.getElementById('query-result-content');" +
                                                "  const badge = document.getElementById('query-status-badge');" +
                                                "  container.classList.remove('hidden');" +
                                                "  content.innerText = 'Executing ' + currentMode.toUpperCase() + ' query...';"
                                                +
                                                "  badge.innerText = 'PENDING';" +
                                                "  badge.className = 'text-[10px] px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-500 border border-amber-500/20 uppercase font-bold tracking-wider';"
                                                +
                                                "  try {" +
                                                "    const resolveRefs = document.getElementById('query-resolve-refs').checked;"
                                                +
                                                "    const endpoint = currentMode === 'sql' ? '/api/v1/sql' : '/api/v1/mongo';"
                                                +
                                                "    const body = currentMode === 'sql' ? { sql: query, resolveRefs } : { query, resolveRefs };"
                                                +
                                                "    const response = await fetch(endpoint, {" +
                                                "      method: 'POST'," +
                                                "      headers: { 'Content-Type': 'application/json' }," +
                                                "      body: JSON.stringify(body)" +
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
