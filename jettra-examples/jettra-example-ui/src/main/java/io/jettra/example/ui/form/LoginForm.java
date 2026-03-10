package io.jettra.example.ui.form;

import io.jettra.ui.component.Button;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Form;
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Label;
import io.jettra.ui.component.Password;

public class LoginForm extends Form {

    private Label errorLabel;
    private Button loginBtn;

    public LoginForm(String id) {
        super(id);
        init();
    }

    public Button getLoginButton() {
        return loginBtn;
    }

    public void setError(String error) {
        if (error != null && !error.isEmpty()) {
            errorLabel.setText(error);
            errorLabel.setStyleClass(
                    "p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 block");
        }
    }

    private void init() {
        this.setStyleClass(
                "space-y-6 bg-slate-900/40 backdrop-blur-xl p-8 rounded-[2.5rem] border border-white/10 shadow-[0_0_20px_rgba(99,102,241,0.3)]");
        this.setHxPost("/auth/login");
        this.setHxTarget("#" + this.getId()); // Target itself for replacement on error
        this.setHxSwap("outerHTML");

        errorLabel = new Label("error-msg", "");
        errorLabel.setStyleClass(
                "hidden p-4 mb-4 text-sm text-rose-400 bg-rose-500/10 border border-rose-500/20 rounded-xl text-center font-bold uppercase tracking-wider");
        this.addComponent(errorLabel);

        // Email Input
        Div emailGroup = new Div("email-group");
        Label emailLabel = new Label("email-label", "Identity Identifier");
        emailLabel.setStyleClass("block mb-2 text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]");
        InputText emailInput = new InputText("email");
        emailInput.setPlaceholder("Username / Admin ID");
        emailInput.setStyleClass(
                "bg-slate-950/80 border border-white/10 text-white text-sm rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 block w-full p-4 transition-all placeholder:text-slate-600");

        emailGroup.addComponent(emailLabel);
        emailGroup.addComponent(emailInput);
        this.addComponent(emailGroup);

        // Password Input
        Div passGroup = new Div("pass-group");
        Label passLabel = new Label("pass-label", "Encryption Key");
        passLabel.setStyleClass("block mb-2 text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]");
        Password passInput = new Password("password");
        passInput.setStyleClass(
                "bg-slate-950/80 border border-white/10 text-white text-sm rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 block w-full p-4 transition-all placeholder:text-slate-600");

        passGroup.addComponent(passLabel);
        passGroup.addComponent(passInput);
        this.addComponent(passGroup);

        // Login Button
        loginBtn = new Button("btn-login", "Initialize Link");
        loginBtn.setStyleClass(
                "w-full text-white bg-indigo-600 hover:bg-indigo-500 active:scale-[0.98] font-black uppercase tracking-widest rounded-2xl text-sm px-5 py-4 text-center transition-all shadow-[0_10px_15px_-3px_rgba(99,102,241,0.3)] hover:shadow-[0_20px_25px_-5px_rgba(99,102,241,0.4)]");
        loginBtn.addAttribute("type", "submit");

        this.addComponent(loginBtn);
    }
}
