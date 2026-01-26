package io.jettra.ui.component;

import java.util.ArrayList;
import java.util.List;

public class Login extends Container {
    private InputText username;
    private Password password;
    private Button loginButton;
    private SelectOne roleSelect;
    private String title = "Sign in to your account";
    
    public Login(String id, InputText username, Password password, Button loginButton) {
        super(id);
        this.username = username;
        this.password = password;
        this.loginButton = loginButton;
        buildLayout();
    }

    public Login(String id, InputText username, Password password, Button loginButton, SelectOne roleSelect) {
        super(id);
        this.username = username;
        this.password = password;
        this.loginButton = loginButton;
        this.roleSelect = roleSelect;
        buildLayout();
    }
    
    public void setTitle(String title) {
        this.title = title;
        // Rebuild layout to update title
        children.clear();
        buildLayout(); 
    }

    private void buildLayout() {
        this.styleClass = "flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0";

        Div card = new Div(id + "-card");
        card.setStyleClass("w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 dark:bg-gray-800 dark:border-gray-700");
        
        Div cardBody = new Div(id + "-card-body");
        cardBody.setStyleClass("p-6 space-y-4 md:space-y-6 sm:p-8");
        
        Label titleLabel = new Label(id + "-title", title);
        titleLabel.setStyleClass("text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white");
        cardBody.addComponent(titleLabel);
        
        Form form = new Form(id + "-form");
        form.setStyleClass("space-y-4 md:space-y-6");
        
        // Username
        Div userGroup = new Div(id + "-user-group");
        Label userLabel = new Label(username.getId() + "-lbl", "Username");
        userGroup.addComponent(userLabel);
        userGroup.addComponent(username);
        form.addComponent(userGroup);
        
        // Password
        Div passGroup = new Div(id + "-pass-group");
        Label passLabel = new Label(password.getId() + "-lbl", "Password");
        passGroup.addComponent(passLabel);
        passGroup.addComponent(password);
        form.addComponent(passGroup);

        // Role (optional)
        if (roleSelect != null) {
            Div roleGroup = new Div(id + "-role-group");
            Label roleLabel = new Label(roleSelect.getId() + "-lbl", "Role");
            roleGroup.addComponent(roleLabel);
            roleGroup.addComponent(roleSelect);
            form.addComponent(roleGroup);
        }
        
        form.addComponent(loginButton);
        
        cardBody.addComponent(form);
        card.addComponent(cardBody);
        
        this.addComponent(card);
    }

    @Override
    public String render() {
        // Since Login extends Container, it has children (the card we built)
        // We override render just to set the wrapping container styling which we handled in buildLayout
        // Actually, Container implementation might assume it's just a holder.
        // Let's use custom render to ensure the "flex..." classes on THIS component are applied to the wrapper div.
        // Wait, Container doesn't have a strict "render myself" in base, it depends on impl.
        // Div extends Container. Login extends Container.
        // Component base renderAttributes is available.
        String attrs = renderAttributes();
        return String.format("<section id='%s' class='%s'%s>%s</section>", 
            id, styleClass, attrs, renderChildren()); // Using renderChildren from Container
    }
}
