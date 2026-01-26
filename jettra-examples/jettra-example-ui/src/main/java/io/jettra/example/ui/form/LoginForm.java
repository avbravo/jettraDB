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
             errorLabel.setStyleClass("p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 block");
        }
    }
    
    private void init() {
        this.setStyleClass("space-y-4 md:space-y-6");
        this.setHxPost("/auth/login");
        this.setHxTarget("#" + this.getId()); // Target itself for replacement on error
        this.setHxSwap("outerHTML");
        
        errorLabel = new Label("error-msg", "");
        errorLabel.setStyleClass("hidden");
        this.addComponent(errorLabel);
        
        // Email Input
        Div emailGroup = new Div("email-group");
        Label emailLabel = new Label("email-label", "Your email");
        emailLabel.setStyleClass("block mb-2 text-sm font-medium text-gray-900 dark:text-white");
        InputText emailInput = new InputText("email");
        emailInput.setPlaceholder("name@company.com");
        // Using standard Tailwind classes for input
        emailInput.setStyleClass("bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500");
        
        emailGroup.addComponent(emailLabel);
        emailGroup.addComponent(emailInput);
        this.addComponent(emailGroup);

        // Password Input
        Div passGroup = new Div("pass-group");
        Label passLabel = new Label("pass-label", "Password");
        passLabel.setStyleClass("block mb-2 text-sm font-medium text-gray-900 dark:text-white");
        Password passInput = new Password("password");
        passInput.setStyleClass("bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500");
        
        passGroup.addComponent(passLabel);
        passGroup.addComponent(passInput);
        this.addComponent(passGroup);

        // Login Button
        loginBtn = new Button("btn-login", "Sign in");
        loginBtn.setStyleClass("w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800");
        loginBtn.addAttribute("type", "submit");
        
        // Logical event listener as requested (conceptual logic)
        loginBtn.addEventListener(event -> {
             System.out.println("Processing login event for component: " + event.getSource().getId());
        });
        
        this.addComponent(loginBtn);
    }
}
