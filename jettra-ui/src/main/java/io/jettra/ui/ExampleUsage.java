package io.jettra.ui;

import io.jettra.ui.component.Button;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Label;
import io.jettra.ui.component.SelectOne;
import io.jettra.ui.component.Table;
import io.jettra.ui.template.Template;
import java.util.Arrays;

public class ExampleUsage {

    public static void main(String[] args) {
        // Create a template
        Template template = new Template();

        // Top Section: Header
        Div header = new Div("header");
        header.setStyleClass("bg-blue-600 p-4 text-white font-bold text-xl");
        header.addComponent(new Label("app-title", "Jettra Application"));
        template.setTop(header);

        // LEft Section: Navigation
        Div nav = new Div("nav");
        nav.setStyleClass("p-4 space-y-2");
        nav.addComponent(new Button("btn-dashboard", "Dashboard"));
        nav.addComponent(new Button("btn-users", "Users"));
        nav.addComponent(new Button("btn-settings", "Settings"));
        template.setLeft(nav);

        // Center Section: Form and Table
        Div content = new Div("content");
        content.setStyleClass("space-y-6");

        // Form
        Form form = new Form("user-form");
        form.addComponent(new Label("lbl-name", "Name"));
        form.addComponent(new InputText("inp-name"));
        
        form.addComponent(new Label("lbl-role", "Role"));
        SelectOne roleInfo = new SelectOne("sel-role");
        roleInfo.addOption("admin", "Administrator");
        roleInfo.addOption("user", "User");
        form.addComponent(roleInfo);
        
        Button saveBtn = new Button("btn-save", "Save User");
        saveBtn.setHxPost("/api/users");
        saveBtn.setHxTarget("#user-table");
        saveBtn.setHxSwap("outerHTML");
        form.addComponent(saveBtn);
        
        content.addComponent(form);

        // Table
        Table table = new Table("user-table");
        table.addHeader("Name");
        table.addHeader("Role");
        table.addRow(Arrays.asList("Alice", "Administrator"));
        table.addRow(Arrays.asList("Bob", "User"));
        
        content.addComponent(table);
        
        template.setCenter(content);

        // Footer
        Div footer = new Div("footer");
        footer.setStyleClass("bg-gray-200 p-2 text-center text-sm");
        footer.addComponent(new Label("lbl-copy", "© 2024 JettraDB"));
        template.setFooter(footer);

        // Render everything to HTML
        System.out.println(template.render());
    }
}
