package io.jettra.example.ui.form;

import io.jettra.ui.component.Button;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;
import io.jettra.ui.component.Label;
import io.jettra.ui.component.Password;

public class ChangePasswordForm extends Form {

    private Label errorLabel;
    private Button changeBtn;

    public ChangePasswordForm(String id) {
        super(id);
        init();
    }

    private void init() {
        this.setStyleClass("space-y-6");
        this.setHxPost("/dashboard/security/change-password");
        this.setHxTarget("#" + this.getId());
        this.setHxSwap("outerHTML");

        errorLabel = new Label("password-error-msg", "");
        errorLabel.setStyleClass("hidden");
        this.addComponent(errorLabel);

        // Current Password
        this.addComponent(createFieldGroup("current-password", "Current Password", "oldPassword"));

        // New Password
        this.addComponent(createFieldGroup("new-password", "New Password", "newPassword"));

        // Confirm Password
        this.addComponent(createFieldGroup("confirm-password", "Confirm New Password", "confirmPassword"));

        // Change Button
        changeBtn = new Button("btn-change-password", "Update Password");
        changeBtn.setStyleClass("w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-indigo-500/30 active:scale-[0.98]");
        changeBtn.addAttribute("type", "submit");
        this.addComponent(changeBtn);
    }

    private Div createFieldGroup(String id, String labelText, String name) {
        Div group = new Div(id + "-group");
        Label label = new Label(id + "-label", labelText);
        label.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        
        Password input = new Password(name);
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
    
    public void setSuccess(String message) {
        if (message != null && !message.isEmpty()) {
            errorLabel.setText(message);
            errorLabel.setStyleClass("p-4 mb-4 text-sm text-green-800 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400 block");
        }
    }
}
