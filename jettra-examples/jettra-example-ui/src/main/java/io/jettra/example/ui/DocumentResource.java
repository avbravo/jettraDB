package io.jettra.example.ui;

import io.jettra.example.ui.client.PlacementDriverClient;
import io.jettra.example.ui.model.Node;
import io.jettra.ui.component.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Path("/dashboard/document")
public class DocumentResource {
    private static final Logger LOG = Logger.getLogger(DocumentResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Inject
    @RestClient
    PlacementDriverClient pdClient;

    @Context
    HttpHeaders headers;

    private String getAuthToken() {
        if (headers.getCookies().containsKey("auth_token")) {
            String token = headers.getCookies().get("auth_token").getValue();
            if (token != null && token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            return token;
        }
        return null;
    }

    private Node findStorageNode(String token) {
        try {
            List<Node> nodes = pdClient.getNodes("Bearer " + token);
            return nodes.stream()
                    .filter(n -> "STORAGE".equals(n.getRole()) && "ONLINE".equals(n.getStatus()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOG.error("Error finding storage node", e);
            return null;
        }
    }

    @GET
    @Path("/explorer")
    @Produces(MediaType.TEXT_HTML)
    public Response getExplorer(@QueryParam("db") String db, @QueryParam("col") String col) {
        Div container = new Div("document-explorer-" + col);
        container.setStyleClass("space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500");

        // Header
        Div header = new Div("doc-header");
        header.setStyleClass("flex flex-wrap justify-between items-center gap-4 mb-6");

        Div titleGroup = new Div("title-group");
        Label title = new Label("doc-title", "Document Explorer: " + col);
        title.setStyleClass("text-2xl font-black text-white tracking-tight");
        titleGroup.addComponent(title);
        header.addComponent(titleGroup);

        // All controls in a Form for automatic inclusion in HTMX requests
        Form filterForm = new Form("doc-filters");
        filterForm.setStyleClass("flex flex-wrap gap-3 items-center");
        filterForm.addAttribute("hx-get", "/dashboard/document/list");
        filterForm.addAttribute("hx-target", "#document-list-container");
        filterForm.addAttribute("hx-trigger",
                "change from:select, change from:input[type='checkbox'], keyup delay:500ms from:input[type='text']");

        // Hidden fields for context
        InputText hiddenDb = new InputText("hidden-db");
        hiddenDb.setType("hidden");
        hiddenDb.addAttribute("name", "db");
        hiddenDb.setValue(db);
        hiddenDb.setStyleClass("");
        filterForm.addComponent(hiddenDb);

        InputText hiddenCol = new InputText("hidden-col");
        hiddenCol.setType("hidden");
        hiddenCol.addAttribute("name", "col");
        hiddenCol.setValue(col);
        hiddenCol.setStyleClass("");
        filterForm.addComponent(hiddenCol);

        // Add Button
        Button addBtn = new Button("btn-add-doc",
                "<svg class='w-5 h-5' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 4v16m8-8H4'></path></svg>");
        addBtn.setStyleClass(
                "p-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-500/20");
        addBtn.addAttribute("hx-get", String.format("/dashboard/document/add-form?db=%s&col=%s", db, col));
        addBtn.addAttribute("hx-target", "#doc-modal-body");
        addBtn.addAttribute("type", "button");
        addBtn.addAttribute("onclick", "openDocumentModal()");
        addBtn.addAttribute("title", "Add Document");
        filterForm.addComponent(addBtn);

        // Search Input
        InputText searchInput = new InputText("doc-search");
        searchInput.addAttribute("placeholder", "Search documents...");
        searchInput.addAttribute("name", "doc-search");
        searchInput.setStyleClass(
                "pl-4 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-lg text-sm text-slate-200 focus:ring-2 focus:ring-indigo-500 outline-none w-64 transition-all");
        filterForm.addComponent(searchInput);

        // View Mode Select
        SelectOne viewMode = new SelectOne("doc-view-mode");
        viewMode.addAttribute("name", "doc-view-mode");
        viewMode.setStyleClass(
                "bg-slate-900 border border-slate-700 rounded-lg p-2 text-xs text-slate-300 font-bold focus:ring-2 focus:ring-indigo-500 outline-none");
        viewMode.addOption("table", "TABLE VIEW");
        viewMode.addOption("json", "JSON VIEW");
        viewMode.addOption("tree", "TREE VIEW");
        filterForm.addComponent(viewMode);

        // Resolve Refs Checkbox
        Div resolveGroup = new Div("resolve-group");
        resolveGroup.setStyleClass(
                "flex items-center gap-2 px-3 py-2 bg-slate-900/50 rounded-lg border border-indigo-500/20");

        InputText checkResolve = new InputText("doc-resolve");
        checkResolve.setType("checkbox");
        checkResolve.addAttribute("name", "doc-resolve");
        checkResolve.addAttribute("value", "true");
        checkResolve.setStyleClass("w-4 h-4 accent-indigo-500 cursor-pointer");
        resolveGroup.addComponent(checkResolve);

        Label resolveLabel = new Label("lbl-resolve", "Resolve Refs");
        resolveLabel.setStyleClass("text-xs font-semibold text-indigo-300 cursor-pointer select-none");
        resolveGroup.addComponent(resolveLabel);
        filterForm.addComponent(resolveGroup);

        header.addComponent(filterForm);
        container.addComponent(header);

        // Document List Container
        Div listContainer = new Div("document-list-container");
        listContainer.addAttribute("hx-get", String.format("/dashboard/document/list?db=%s&col=%s", db, col));
        listContainer.addAttribute("hx-trigger", "load, refreshDocuments from:body");
        listContainer.addAttribute("hx-include", "#doc-filters");
        container.addComponent(listContainer);

        return Response.ok(container.render()).build();
    }

    @GET
    @Path("/add-form")
    @Produces(MediaType.TEXT_HTML)
    public Response getAddForm(@QueryParam("db") String db, @QueryParam("col") String col) {
        return renderDocumentForm(db, col, "", "{\n  \"name\": \"New Document\"\n}");
    }

    @POST
    @Path("/edit-form")
    @Produces(MediaType.TEXT_HTML)
    public Response getEditForm(@QueryParam("db") String db, @QueryParam("col") String col,
            @QueryParam("jettraID") String jettraID, @QueryParam("json") String json,
            @FormParam("json") String formJson) {
        if (formJson != null)
            json = formJson;

        if (json == null || json.isEmpty() || "null".equals(json)) {
            // Try to fetch from storage node if json is missing
            String token = getAuthToken();
            if (token != null) {
                Node storeNode = findStorageNode(token);
                if (storeNode != null) {
                    try {
                        String url = String.format("http://%s/api/v1/document/%s?jettraID=%s", storeNode.getAddress(),
                                col,
                                java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8));
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("Authorization", "Bearer " + token)
                                .GET()
                                .build();
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            json = response.body();
                        }
                    } catch (Exception e) {
                        LOG.error("Error fetching document for edit", e);
                    }
                }
            }
        }
        return renderDocumentForm(db, col, jettraID, json);
    }

    private Response renderDocumentForm(String db, String col, String jettraID, String json) {
        Form form = new Form("document-form");
        form.setStyleClass("space-y-4");

        // Context fields
        InputText dbInput = new InputText("form-db");
        dbInput.setType("hidden");
        dbInput.addAttribute("name", "db");
        dbInput.setValue(db);
        form.addComponent(dbInput);

        InputText colInput = new InputText("form-col");
        colInput.setType("hidden");
        colInput.addAttribute("name", "col");
        colInput.setValue(col);
        form.addComponent(colInput);

        InputText idInput = new InputText("form-jettraID");
        idInput.setType("hidden");
        idInput.addAttribute("name", "jettraID");
        idInput.setValue(jettraID);
        form.addComponent(idInput);

        // JSON Content
        Label label = new Label("lbl-json", "JSON Payload");
        label.setStyleClass("block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1");
        form.addComponent(label);

        TextArea textArea = new TextArea("form-json");
        textArea.addAttribute("name", "json");

        String prettyJson = json;
        try {
            if (json != null && !json.trim().isEmpty()) {
                Object obj = mapper.readValue(json, Object.class);
                prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
        } catch (Exception e) {
            LOG.warn("Could not pretty print JSON for form: " + e.getMessage());
        }

        textArea.setValue(prettyJson == null ? "" : prettyJson);
        textArea.setStyleClass(
                "w-full h-64 bg-slate-900 border border-slate-700 text-indigo-300 font-mono text-sm p-4 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none resize-vertical");
        form.addComponent(textArea);

        // Submit Button (Self-closing the modal via Flowbite)
        Button submitBtn = new Button("btn-submit-doc",
                jettraID == null || jettraID.isEmpty() ? "Create Document" : "Save Changes");
        submitBtn.setStyleClass(
                "w-full px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-indigo-500/20");
        submitBtn.addAttribute("hx-post", "/dashboard/document/save");
        submitBtn.addAttribute("hx-target", "#document-list-container");
        submitBtn.addAttribute("hx-include", "closest form");
        submitBtn.addAttribute("hx-swap", "none");
        submitBtn.addAttribute("type", "button");
        submitBtn.addAttribute("data-modal-target", "document-modal");
        // Removed data-modal-hide to let HX-Trigger handle closure after success
        form.addComponent(submitBtn);

        return Response.ok(form.render()).build();
    }

    @GET
    @Path("/delete-form")
    @Produces(MediaType.TEXT_HTML)
    public Response getDeleteForm(@QueryParam("db") String db, @QueryParam("col") String col,
            @QueryParam("jettraID") String jettraID) {
        // Use a Form to ensure parameters are passed correctly without JSON escaping
        // issues
        Form container = new Form("delete-confirm-form");
        container.setStyleClass("text-center space-y-6 p-4");

        // Hidden inputs for context
        InputText dbInput = new InputText("del-db");
        dbInput.setType("hidden");
        dbInput.addAttribute("name", "db");
        dbInput.setValue(db);
        container.addComponent(dbInput);

        InputText colInput = new InputText("del-col");
        colInput.setType("hidden");
        colInput.addAttribute("name", "col");
        colInput.setValue(col);
        container.addComponent(colInput);

        InputText idInput = new InputText("del-jettraID");
        idInput.setType("hidden");
        idInput.addAttribute("name", "jettraID");
        idInput.setValue(jettraID);
        container.addComponent(idInput);

        Div iconBox = new Div("del-icon-box");
        iconBox.setStyleClass(
                "w-16 h-16 bg-rose-500/10 rounded-full flex items-center justify-center mx-auto border border-rose-500/20");
        iconBox.addComponent(new Label("del-icon-svg",
                "<svg class='w-8 h-8 text-rose-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'></path></svg>"));
        container.addComponent(iconBox);

        Div msgDiv = new Div("del-msg-div");
        msgDiv.setStyleClass("text-slate-300");
        msgDiv.addComponent(new Label("del-msg-text",
                String.format("Are you sure you want to delete document %s? This action cannot be undone.", jettraID)));
        container.addComponent(msgDiv);

        Div btnContainer = new Div("del-btn-container");
        btnContainer.setStyleClass("flex gap-3 justify-center");

        Button cancelBtn = new Button("btn-cancel-delete", "Cancel");
        cancelBtn.setStyleClass(
                "px-6 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg font-medium transition-all");
        cancelBtn.addAttribute("onclick", "closeDocumentDeleteModal()");
        cancelBtn.addAttribute("type", "button");
        btnContainer.addComponent(cancelBtn);

        Button confirmBtn = new Button("btn-confirm-delete", "Yes, Delete Document");
        confirmBtn.setStyleClass(
                "px-6 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg font-bold transition-all shadow-lg shadow-rose-500/20");
        confirmBtn.addAttribute("hx-post", "/dashboard/document/delete");
        confirmBtn.addAttribute("hx-target", "#document-list-container");
        confirmBtn.addAttribute("hx-swap", "none");
        // Include the form values (closest form)
        confirmBtn.addAttribute("hx-include", "closest form");
        // Ensure modal closes on click (optimistic) or update handling
        confirmBtn.addAttribute("onclick", "closeDocumentDeleteModal()");
        confirmBtn.addAttribute("type", "button");
        btnContainer.addComponent(confirmBtn);

        container.addComponent(btnContainer);
        return Response.ok(container.render()).build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public Response getDocumentList(@QueryParam("db") String db,
            @QueryParam("col") String col,
            @QueryParam("doc-search") String search,
            @QueryParam("doc-resolve") @DefaultValue("false") boolean resolveRefs,
            @QueryParam("doc-current-page") @DefaultValue("1") int page,
            @QueryParam("doc-view-mode") @DefaultValue("table") String viewMode) {
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Node storeNode = findStorageNode(token);
        if (storeNode == null) {
            Div errorDiv = new Div("no-store-node-err");
            errorDiv.setStyleClass("p-4 text-rose-400 bg-rose-400/10 rounded-lg border border-rose-400/20");
            errorDiv.addComponent(new Label("lbl-err", "Error: No online STORAGE nodes found."));
            return Response.ok(errorDiv.render()).build();
        }

        try {
            int pageSize = 10;
            StringBuilder urlBuilder = new StringBuilder(
                    String.format("http://%s/api/v1/document/%s", storeNode.getAddress(), col));
            urlBuilder.append("?page=").append(page);
            urlBuilder.append("&size=").append(pageSize);
            if (search != null && !search.trim().isEmpty()) {
                urlBuilder.append("&search=")
                        .append(java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8));
            }
            if (resolveRefs) {
                urlBuilder.append("&resolveRefs=true");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> rawDocs = mapper.readValue(response.body(), new TypeReference<List<String>>() {
                });

                Div listWrapper = new Div("list-wrapper-container");
                listWrapper.setStyleClass("space-y-4");

                if (rawDocs.isEmpty()) {
                    Div emptyDiv = new Div("empty-docs-msg");
                    emptyDiv.setStyleClass("text-center py-12 text-slate-500 italic");
                    emptyDiv.addComponent(new Label("lbl-empty", "No documents found."));
                    listWrapper.addComponent(emptyDiv);
                } else {
                    if ("json".equalsIgnoreCase(viewMode)) {
                        listWrapper.addComponent(new Label("json-view", renderJsonView(rawDocs)));
                    } else if ("tree".equalsIgnoreCase(viewMode)) {
                        listWrapper.addComponent(new Label("tree-view", renderTreeView(rawDocs)));
                    } else {
                        listWrapper.addComponent(new Label("table-view", renderTableView(db, col, rawDocs)));
                    }
                }

                // Pagination Row
                Div pagination = new Div("list-pagination");
                pagination.setStyleClass(
                        "flex items-center justify-between p-4 border-t border-slate-800 bg-slate-900/30 rounded-b-xl");

                Label pageInfo = new Label("page-info", String.format("Page %d", page));
                pageInfo.setStyleClass("text-xs font-mono text-slate-500");
                pagination.addComponent(pageInfo);

                Div pagerBtns = new Div("pager-btns");
                pagerBtns.setStyleClass("flex gap-2");

                Button prev = new Button("btn-prev", "Previous");
                prev.setStyleClass(
                        "px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-bold rounded-lg transition-all border border-slate-700 disabled:opacity-30");
                if (page > 1) {
                    prev.addAttribute("hx-get", String
                            .format("/dashboard/document/list?db=%s&col=%s&doc-current-page=%d", db, col, page - 1));
                    prev.addAttribute("hx-target", "#document-list-container");
                    prev.addAttribute("hx-include", "#doc-filters");
                } else {
                    prev.addAttribute("disabled", "true");
                }
                pagerBtns.addComponent(prev);

                Button next = new Button("btn-next", "Next");
                next.setStyleClass(
                        "px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-bold rounded-lg transition-all border border-slate-700 disabled:opacity-30");
                if (rawDocs.size() == pageSize) {
                    next.addAttribute("hx-get", String
                            .format("/dashboard/document/list?db=%s&col=%s&doc-current-page=%d", db, col, page + 1));
                    next.addAttribute("hx-target", "#document-list-container");
                    next.addAttribute("hx-include", "#doc-filters");
                } else {
                    next.addAttribute("disabled", "true");
                }
                pagerBtns.addComponent(next);

                pagination.addComponent(pagerBtns);
                listWrapper.addComponent(pagination);

                return Response.ok(listWrapper.render()).build();
            } else {
                Div errorDiv = new Div("list-err");
                errorDiv.setStyleClass("p-4 text-rose-400 bg-rose-400/10 rounded-lg");
                errorDiv.addComponent(new Label("lbl-list-err", "Error fetching documents: " + response.statusCode()));
                return Response.ok(errorDiv.render()).build();
            }
        } catch (Exception e) {
            LOG.error("Error fetching documents", e);
            Div errorDiv = new Div("list-catch-err");
            errorDiv.setStyleClass("p-4 text-rose-400 bg-rose-400/10 rounded-lg");
            errorDiv.addComponent(new Label("lbl-catch-err", "Error: " + e.getMessage()));
            return Response.ok(errorDiv.render()).build();
        }
    }

    private String renderTableView(String db, String col, List<String> rawDocs) throws Exception {
        Table table = new Table("doc-table-" + col);
        table.setStyleClass("w-full text-left border-collapse");

        // Collect dynamic headers
        List<String> headers = new ArrayList<>();
        headers.add("jettraID"); // Always first
        headers.add("Version"); // Explicitly add Version column

        List<Map<String, Object>> docs = new ArrayList<>();
        for (String raw : rawDocs) {
            try {
                Map<String, Object> map = mapper.readValue(raw, new TypeReference<Map<String, Object>>() {
                });
                docs.add(map);
                for (String key : map.keySet()) {
                    if (!headers.contains(key) && !key.equals("jettraID") && !key.startsWith("_")) {
                        headers.add(key);
                    }
                }
            } catch (Exception e) {
                LOG.error("Skipping malformed document in Table View: " + e.getMessage());
                java.util.Map<String, Object> errMap = new java.util.HashMap<>();
                errMap.put("jettraID", "ERROR-PARSING");
                errMap.put("_raw", raw == null ? "null" : raw);
                docs.add(errMap);
            }
        }

        // Add headers to table
        for (String h : headers) {
            table.addHeader(h);
        }
        table.addHeader("Actions");

        for (Map<String, Object> doc : docs) {
            List<String> row = new ArrayList<>();
            String jettraID = String.valueOf(doc.get("jettraID"));

            // Render cells for each header
            for (String h : headers) {
                Object val;
                if ("Version".equals(h)) {
                    val = doc.get("_version");
                } else {
                    val = doc.get(h);
                }
                String valStr = (val == null) ? "-" : String.valueOf(val);
                if (valStr.length() > 50)
                    valStr = valStr.substring(0, 47) + "...";

                Span span = new Span("cell-" + h + "-" + jettraID, valStr);
                if (h.equals("jettraID")) {
                    span.setStyleClass("text-[10px] text-indigo-400 bg-indigo-400/10 px-1 rounded font-mono");
                } else if (h.equals("Version")) {
                    span.setStyleClass("text-xs text-emerald-400 font-mono font-bold bg-emerald-400/10 px-1 rounded");
                } else {
                    span.setStyleClass("text-xs text-slate-400 font-mono");
                }

                // Add raw data to ID cell for Edit logic
                if (h.equals("jettraID")) {
                    try {
                        String raw = mapper.writeValueAsString(doc);
                        span.addAttribute("data-raw", raw.replace("\"", "&quot;"));
                        // Fixed: Removed getStyleClass() call
                        span.setStyleClass(
                                "text-[10px] text-indigo-400 bg-indigo-400/10 px-1 rounded font-mono content-snippet");
                    } catch (Exception e) {
                    }
                }

                row.add(span.render());
            }

            Div actions = new Div("actions-" + jettraID);
            actions.setStyleClass("flex gap-2 items-center");

            Button editBtn = new Button("edit-" + jettraID,
                    "<svg class='w-4 h-4' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z'></path></svg>");
            editBtn.setStyleClass("p-1 hover:text-amber-400 text-slate-500 transition-colors cursor-pointer");

            String encodedID = jettraID;
            try {
                encodedID = java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
            }

            // Use POST to send large JSON and avoid 'null' issues
            editBtn.addAttribute("hx-post",
                    String.format("/dashboard/document/edit-form?db=%s&col=%s&jettraID=%s", db, col, encodedID));
            // Modified selector to find tr -> content-snippet
            editBtn.addAttribute("hx-vals",
                    "js:{json: event.target.closest('tr').querySelector('.content-snippet').getAttribute('data-raw')}");
            editBtn.addAttribute("hx-target", "#doc-modal-body");
            editBtn.addAttribute("onclick", "openDocumentModal()");
            actions.addComponent(editBtn);

            Button delBtn = new Button("del-" + jettraID,
                    "<svg class='w-4 h-4' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'></path></svg>");
            delBtn.setStyleClass("p-1 hover:text-red-400 text-slate-500 transition-colors cursor-pointer");
            delBtn.addAttribute("hx-get",
                    String.format("/dashboard/document/delete-form?db=%s&col=%s&jettraID=%s", db, col, encodedID));
            delBtn.addAttribute("hx-target", "#doc-del-body");
            delBtn.addAttribute("onclick", "openDocumentDeleteModal()");
            actions.addComponent(delBtn);

            // Versions Button
            Button verBtn = new Button("ver-" + jettraID,
                    "<svg class='w-4 h-4' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z'></path></svg>");
            verBtn.setStyleClass("p-1 hover:text-cyan-400 text-slate-500 transition-colors cursor-pointer");
            verBtn.addAttribute("title", "Versions History");
            verBtn.addAttribute("hx-get",
                    String.format("/dashboard/document/versions?db=%s&col=%s&jettraID=%s", db, col, encodedID));
            verBtn.addAttribute("hx-target", "#versions-modal-body");
            verBtn.addAttribute("onclick", "openVersionsModal()");
            actions.addComponent(verBtn);

            row.add(actions.render());
            table.addRow(row);
        }
        return table.render();
    }

    private String renderJsonView(List<String> rawDocs) {
        Div container = new Div("json-view-container");
        container.setStyleClass("space-y-4 p-4 mt-4");

        for (int i = 0; i < rawDocs.size(); i++) {
            Div item = new Div("json-item-" + i);
            item.setStyleClass(
                    "bg-slate-950 p-4 rounded-xl border border-slate-800 font-mono text-xs text-indigo-300 overflow-x-auto shadow-inner");

            Label code = new Label("json-code-" + i, rawDocs.get(i));
            // Use pre-wrap style via CSS class if possible, or wrap in a pre tag if
            // Jettra-UI Label allows it.
            // Since Label just renders the text, we might need a way to preserve
            // formatting.
            // Let's assume the styleClass font-mono and overflow-x-auto on the parent help.
            item.addComponent(code);
            container.addComponent(item);
        }
        return container.render();
    }

    private String renderTreeView(List<String> rawDocs) {
        Div container = new Div("tree-view-wrapper");
        container.setStyleClass("p-4 space-y-6 mt-4");

        for (int i = 0; i < rawDocs.size(); i++) {
            try {
                Map<String, Object> map = mapper.readValue(rawDocs.get(i), new TypeReference<Map<String, Object>>() {
                });
                String id = String.valueOf(map.getOrDefault("jettraID", "doc-" + i));

                Card card = new Card("tree-card-" + i, id);
                card.setStyleClass("bg-slate-900 border border-slate-800 rounded-xl overflow-hidden");

                Tree tree = new Tree("tree-comp-" + i);
                tree.setStyleClass("text-sm text-slate-300");
                Tree.TreeNode root = new Tree.TreeNode(id,
                        "<svg class='w-4 h-4 text-indigo-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'></path></svg>");
                buildTreeFromMap(root, map);
                tree.addNode(root);

                card.addComponent(tree);
                container.addComponent(card);
            } catch (Exception e) {
                container.addComponent(new Label("tree-err-" + i,
                        "<div class='text-rose-400 p-4 bg-rose-400/10 rounded'>Error parsing document tree</div>"));
            }
        }
        return container.render();
    }

    private void buildTreeFromMap(Tree.TreeNode parent, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?>) {
                Tree.TreeNode node = new Tree.TreeNode(key,
                        "<svg class='w-3 h-3 text-slate-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3l-2-2H5a2 2 0 00-2 2z'></path></svg>");
                buildTreeFromMap(node, (Map<String, Object>) value);
                parent.addChild(node);
            } else if (value instanceof List<?>) {
                Tree.TreeNode node = new Tree.TreeNode(key + " [" + ((List<?>) value).size() + " items]",
                        "<svg class='w-3 h-3 text-slate-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M4 6h16M4 10h16M4 14h16M4 18h16'></path></svg>");
                parent.addChild(node);
            } else {
                parent.addChild(new Tree.TreeNode(key + ": " + value,
                        "<svg class='w-3 h-3 text-slate-500' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M7 20l4-16m2 16l4-16'></path></svg>"));
            }
        }
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response saveDocument(@FormParam("db") String db, @FormParam("col") String col,
            @FormParam("jettraID") String jettraID, @FormParam("json") String json) {
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Node storeNode = findStorageNode(token);
        if (storeNode == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("No STORAGE nodes found").build();

        try {
            // Sanitize JSON - remove system fields
            String sanitizedJson = json;
            if (json != null && !json.trim().isEmpty()) {
                if ("{}".equals(json.trim())) {
                    return Response.ok("<script>alert('Error: Document cannot be empty or just {}.');</script>")
                            .build();
                }
                try {
                    Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                    });
                    map.remove("jettraID");
                    map.remove("_version");
                    map.remove("_lastModified");
                    map.remove("_tags");
                    sanitizedJson = mapper.writeValueAsString(map);
                } catch (Exception e) {
                    // If parsing fails, use original, but might be risky.
                    // Assuming valid JSON from UI. If invalid, backend might reject.
                }
            }

            String url = String.format("http://%s/api/v1/document/%s", storeNode.getAddress(), col);
            if (jettraID != null && !jettraID.trim().isEmpty()) {
                url += "?jettraID=" + java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(sanitizedJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String successMsg = jettraID == null || jettraID.strip().isEmpty() ? "Document created successfully!"
                        : "Document updated successfully!";
                return Response.ok()
                        .header("HX-Trigger", String.format("{\"refreshDocuments\": \"%s\"}", successMsg))
                        .build();
            } else {
                return Response.ok("<script>alert('Error saving document (" + response.statusCode() + "): "
                        + response.body().replace("'", "\\'") + "');</script>").build();
            }
        } catch (Exception e) {
            return Response.ok("<script>alert('Error: " + e.getMessage().replace("'", "\\'") + "');</script>").build();
        }
    }

    @GET
    @Path("/versions")
    @Produces(MediaType.TEXT_HTML)
    public Response getVersionsList(@QueryParam("db") String db, @QueryParam("col") String col,
            @QueryParam("jettraID") String jettraID) {
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Node storeNode = findStorageNode(token);
        if (storeNode == null)
            return Response.ok("<div class='text-rose-400'>No storage node available</div>").build();

        try {
            String url = String.format("http://%s/api/v1/document/%s/%s/versions",
                    storeNode.getAddress(), col,
                    java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> versions = mapper.readValue(response.body(), new TypeReference<List<String>>() {
                });

                Div container = new Div("versions-container");
                container.setStyleClass("space-y-3");

                // Sort by _version desc
                List<Map<String, Object>> versionMaps = new ArrayList<>();
                for (String v : versions) {
                    versionMaps.add(mapper.readValue(v, new TypeReference<Map<String, Object>>() {
                    }));
                }
                versionMaps.sort((a, b) -> {
                    Integer v1 = (Integer) a.getOrDefault("_version", 0);
                    Integer v2 = (Integer) b.getOrDefault("_version", 0);
                    return v2.compareTo(v1);
                });

                if (versionMaps.isEmpty()) {
                    container.addComponent(new Label("no-versions", "No version history found."));
                }

                for (Map<String, Object> vMap : versionMaps) {
                    String vNum = String.valueOf(vMap.get("_version"));
                    String vDate = String.valueOf(vMap.get("_lastModified"));
                    String vRaw = mapper.writeValueAsString(vMap);

                    String prettyVJson = vRaw;
                    try {
                        prettyVJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(vMap);
                    } catch (Exception e) {
                    }

                    Div row = new Div("ver-row-" + vNum);
                    row.setStyleClass(
                            "flex flex-col p-3 bg-slate-900 rounded-lg border border-slate-800 hover:border-indigo-500/30 transition-all gap-3");

                    Div headerRow = new Div("ver-header-" + vNum);
                    headerRow.setStyleClass("flex justify-between items-center w-full");

                    Div info = new Div("ver-info-" + vNum);
                    info.addComponent(new Label("ver-lbl-" + vNum,
                            "<span class='font-bold text-indigo-400'>Version " + vNum + "</span>"));
                    info.addComponent(new Label("date-lbl-" + vNum,
                            "<span class='text-xs text-slate-500 block'>" + vDate + "</span>"));
                    headerRow.addComponent(info);

                    Button restore = new Button("btn-restore-" + vNum, "Restore");
                    restore.setStyleClass(
                            "px-3 py-1 bg-indigo-600 hover:bg-indigo-500 text-white text-xs rounded font-medium shadow-lg shadow-indigo-500/20");

                    String attrJson = vRaw.replace("\"", "&quot;");
                    restore.addAttribute("data-restore-json", attrJson);
                    String encodedID = java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8);
                    restore.addAttribute("hx-post",
                            "/dashboard/document/restore-version?db=" + db + "&col=" + col + "&jettraID=" + encodedID);
                    restore.addAttribute("hx-vals", "js:{json: event.target.getAttribute('data-restore-json')}");
                    restore.addAttribute("hx-target", "#versions-modal-body");
                    headerRow.addComponent(restore);

                    row.addComponent(headerRow);

                    // Details block for revision content
                    Label details = new Label("ver-details-" + vNum,
                            "<details class='group w-full'>" +
                                    "  <summary class='text-[10px] text-slate-500 cursor-pointer uppercase tracking-widest font-bold hover:text-slate-300 transition-colors list-none flex items-center gap-1'>"
                                    +
                                    "    <svg class='w-3 h-3 transition-transform group-open:rotate-90' fill='none' stroke='currentColor' viewBox='0 0 24 24'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='3' d='M9 5l7 7-7 7'></path></svg>"
                                    +
                                    "    View Content" +
                                    "  </summary>" +
                                    "  <pre class='mt-2 p-3 bg-black/40 rounded-lg text-[11px] text-indigo-300/80 font-mono overflow-x-auto border border-slate-800/50 whitespace-pre-wrap'>"
                                    +
                                    prettyVJson +
                                    "  </pre>" +
                                    "</details>");
                    row.addComponent(details);

                    container.addComponent(row);
                }

                return Response.ok(container.render()).build();
            } else {
                return Response.ok("<div class='text-rose-400'>Failed to fetch versions (Status: "
                        + response.statusCode() + ")</div>").build();
            }
        } catch (Exception e) {
            LOG.error("Error fetching versions", e);
            return Response.ok("<div class='text-rose-400'>Error: " + e.getMessage() + "</div>").build();
        }
    }

    @POST
    @Path("/restore-version")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response restoreVersion(@QueryParam("db") String db, @QueryParam("col") String col,
            @QueryParam("jettraID") String jettraID, @FormParam("json") String json) {
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Node storeNode = findStorageNode(token);
        if (storeNode == null)
            return Response.ok("<script>alert('Error: No storage node available');</script>").build();

        String version = "0";
        try {
            Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            version = String.valueOf(map.getOrDefault("_version", "0"));
        } catch (Exception e) {
            LOG.error("Error parsing version json", e);
            return Response.ok("<script>alert('Error parsing version data');</script>").build();
        }

        try {
            String encodedID = jettraID != null
                    ? java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8)
                    : "";
            String url = String.format("http://%s/api/v1/document/%s/%s/restore/%s",
                    storeNode.getAddress(), col,
                    encodedID,
                    version);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Response.ok()
                        .header("HX-Trigger",
                                "{\"refreshDocuments\": \"Version " + version + " restored successfully!\"}")
                        .build();
            } else {
                return Response.ok("<script>alert('Error restoring version: " + response.statusCode() + "');</script>")
                        .build();
            }
        } catch (Exception e) {
            return Response.ok("<script>alert('Error: " + e.getMessage() + "');</script>").build();
        }
    }

    @POST
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@FormParam("db") String db, @FormParam("col") String col,
            @FormParam("jettraID") String jettraID) {
        String token = getAuthToken();
        if (token == null)
            return Response.status(Response.Status.UNAUTHORIZED).build();

        Node storeNode = findStorageNode(token);
        if (storeNode == null)
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("No STORAGE nodes found").build();

        try {
            // FIXED: Use path parameter for jettraID
            String url = String.format("http://%s/api/v1/document/%s/%s", storeNode.getAddress(), col,
                    java.net.URLEncoder.encode(jettraID, java.nio.charset.StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Response.ok()
                        .header("HX-Trigger", "{\"refreshDocuments\": \"Document deleted successfully!\"}")
                        .build();
            } else {
                return Response
                        .ok("<script>alert('Error deleting document: " + response.statusCode() + " "
                                + response.body().replace("'", "\\'") + "');</script>")
                        .build();
            }
        } catch (Exception e) {
            return Response.ok("<script>alert('Error: " + e.getMessage() + "');</script>").build();
        }
    }
}
