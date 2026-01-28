package io.jettra.example.ui.form;

import io.jettra.ui.component.Button;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Label;
import io.jettra.ui.component.SelectOne;

public class DatabaseForm extends Form {

    private Label errorLabel;

    public DatabaseForm(String id) {
        super(id);
        init();
    }

    private void init() {
        this.setStyleClass("space-y-6 bg-slate-900/40 p-8 rounded-2xl border border-white/5 backdrop-blur-xl shadow-2xl");
        this.setHxPost("/dashboard/database/save");
        this.setHxTarget("#main-content-view");
        this.setHxSwap("innerHTML");

        errorLabel = new Label("db-error-msg", "");
        errorLabel.setStyleClass("hidden");
        this.addComponent(errorLabel);

        // Header with Vibe
        Div header = new Div("form-header");
        header.setStyleClass("mb-8");
        Label subtitle = new Label("form-sub", "Unleash your data's potential.");
        subtitle.setStyleClass("text-indigo-400 text-xs uppercase tracking-widest font-bold mb-1 block");
        header.addComponent(subtitle);
        this.addComponent(header);

        // Name
        this.addComponent(createFieldGroup("db-name", "Database Name", "name"));

        // Engine
        Div engineGroup = new Div("engine-group");
        Label engineLabel = new Label("engine-label", "Engine");
        engineLabel.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        SelectOne engineSelect = new SelectOne("engine");
        engineSelect.addOption("Multi-Model", "Multi-Model (Recommended)");
        engineSelect.addOption("Document", "Document Store");
        engineSelect.addOption("Key-Value", "Key-Value Store");
        engineSelect.addOption("Graph", "Graph Database");
        engineSelect.addOption("Time-Series", "Time-Series");
        engineSelect.addOption("Vector", "Vector Search");
        engineGroup.addComponent(engineLabel);
        engineGroup.addComponent(engineSelect);
        this.addComponent(engineGroup);

        // Storage
        Div storageGroup = new Div("storage-group");
        Label storageLabel = new Label("storage-label", "Storage");
        storageLabel.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        SelectOne storageSelect = new SelectOne("storage");
        storageSelect.addOption("STORE", "Persistent (Disk)");
        storageSelect.addOption("MEMORY", "In-Memory (RAM)");
        storageGroup.addComponent(storageLabel);
        storageGroup.addComponent(storageSelect);
        this.addComponent(storageGroup);

        // Submit Button
        Button submitBtn = new Button("btn-save-db", "INITIALIZE DATABASE");
        submitBtn.setStyleClass("w-full py-4 bg-gradient-to-r from-indigo-600 to-crimson-600 hover:from-indigo-500 hover:to-crimson-500 text-white font-black rounded-xl transition-all shadow-lg shadow-indigo-500/20 active:scale-[0.97] tracking-tighter text-lg");
        submitBtn.addAttribute("type", "submit");
        this.addComponent(submitBtn);
    }

    private Div createFieldGroup(String id, String labelText, String name) {
        Div group = new Div(id + "-group");
        Label label = new Label(id + "-label", labelText);
        label.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        
        InputText input = new InputText(name);
        input.setPlaceholder("my_database");
        input.setStyleClass("w-full px-4 py-3 bg-slate-900/50 border border-white/10 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 transition-all outline-none");
        
        group.addComponent(label);
        group.addComponent(input);
        return group;
    }

    public void setError(String error) {
        if (error != null && !error.isEmpty()) {
            errorLabel.setText(error);
            errorLabel.setStyleClass("p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 block");
        } else {
            errorLabel.setStyleClass("hidden");
        }
    }
}
