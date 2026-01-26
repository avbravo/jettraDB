package io.jettra.example.ui;

import io.jettra.ui.component.*;
import io.jettra.ui.template.Page;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class LoginResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String login() {
        Div container = new Div("login-page");
        container.setStyleClass("min-h-screen bg-gradient-to-br from-slate-900 to-indigo-950 flex items-center justify-center p-4");

        Div card = new Div("login-card");
        card.setStyleClass("w-full max-w-md bg-white/10 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl p-8");

        Div logoContainer = new Div("logo-container");
        logoContainer.setStyleClass("flex flex-col items-center mb-8");
        
        Component logo = new Component("logo-img") {
            @Override
            public String render() {
                return "<img src='/logo/jettra-logo.png' alt='JettraDB Logo' class='w-16 h-16 mb-4 drop-shadow-lg'>";
            }
        };
        logoContainer.addComponent(logo);
        
        Label title = new Label("title", "JettraDB Admin");
        title.setStyleClass("text-3xl font-bold text-white bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-violet-400");
        logoContainer.addComponent(title);
        
        card.addComponent(logoContainer);

        Form form = new Form("login-form");
        form.setStyleClass("space-y-6");
        form.setHxPost("/auth/login");
        form.setHxTarget("#login-page"); // Or body

        Div userGroup = new Div("user-group");
        Label userLabel = new Label("user-label", "Email Address");
        userLabel.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        InputText email = new InputText("email");
        email.setPlaceholder("admin@jettra.io");
        email.setStyleClass("w-full px-4 py-3 bg-slate-900/50 border border-white/10 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 transition-all outline-none");
        userGroup.addComponent(userLabel);
        userGroup.addComponent(email);
        form.addComponent(userGroup);

        Div passGroup = new Div("pass-group");
        Label passLabel = new Label("pass-label", "Password");
        passLabel.setStyleClass("block text-sm font-medium text-slate-300 mb-2");
        Password password = new Password("password");
        password.setStyleClass("w-full px-4 py-3 bg-slate-900/50 border border-white/10 rounded-xl text-white focus:ring-2 focus:ring-indigo-500 transition-all outline-none");
        passGroup.addComponent(passLabel);
        passGroup.addComponent(password);
        form.addComponent(passGroup);

        Button loginBtn = new Button("btn-login", "Sign In");
        loginBtn.setStyleClass("w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all shadow-lg shadow-indigo-500/30 active:scale-[0.98]");
        form.addComponent(loginBtn);

        card.addComponent(form);
        container.addComponent(card);

        Page page = new Page();
        page.setTitle("JettraDB Login");
        page.setContent(container.render());
        
        return page.render();
    }
}
