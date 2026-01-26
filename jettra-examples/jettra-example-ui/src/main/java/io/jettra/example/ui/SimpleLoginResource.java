package io.jettra.example.ui;

import io.jettra.ui.component.Button;
import io.jettra.ui.component.InputText;
import io.jettra.ui.component.Login;
import io.jettra.ui.component.Password;
import io.jettra.ui.template.Page;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/simple-login")
public class SimpleLoginResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String login() {
        InputText user = new InputText("username");
        user.setPlaceholder("Username or Email");
        
        Password pass = new Password("password");
        
        Button btn = new Button("btn-login", "Sign In");
        btn.setStyleClass("w-full text-white bg-blue-600 hover:bg-blue-700 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800");
        btn.setHxPost("/auth/login");
        btn.setHxTarget("body");
        
        Login login = new Login("simple-login", user, pass, btn);
        login.setTitle("Welcome Back");
        
        
        Page page = new Page();
        page.setTitle("Jettra Simple Login");
        page.setContent(login.render());
        
        return page.render();
    }
}
