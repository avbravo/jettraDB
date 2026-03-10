package io.jettra.ui.component;

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
                this.styleClass = "flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0 relative z-10";

                Div card = new Div(id + "-card");
                card.setStyleClass(
                                "w-full j3d-card md:mt-0 sm:max-w-md xl:p-0 backdrop-blur-3xl bg-slate-900/40 border border-white/10 shadow-[0_0_30px_rgba(99,102,241,0.2)] rounded-[2.5rem] transition-all duration-500 hover:rotate-y-12 hover:rotate-x-12 [transform-style:preserve-3d]");

                Div cardBody = new Div(id + "-card-body");
                cardBody.setStyleClass("p-8 space-y-8");

                Div header = new Div(id + "-header");
                header.setStyleClass("text-center mb-8");

                Label titleLabel = new Label(id + "-title", title.toUpperCase());
                titleLabel.setStyleClass(
                                "text-2xl font-black leading-tight tracking-[0.2em] text-white italic bg-gradient-to-r from-white to-indigo-400 bg-clip-text text-transparent");
                header.addComponent(titleLabel);

                Div line = new Div(id + "-line");
                line.setStyleClass("h-1 w-12 bg-indigo-600 mx-auto mt-2 rounded-full shadow-[0_0_10px_#4f46e5]");
                header.addComponent(line);

                cardBody.addComponent(header);

                Form form = new Form(id + "-form");
                form.setStyleClass("space-y-6");

                // Username
                Div userGroup = new Div(id + "-user-group");
                Label userLabel = new Label(username.getId() + "-lbl", "Identity Identifier");
                userLabel.setStyleClass("block mb-2 text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]");
                userGroup.addComponent(userLabel);
                username.setStyleClass(
                                "bg-slate-950/80 border border-white/10 text-white text-sm rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 block w-full p-4 transition-all placeholder:text-slate-600");
                userGroup.addComponent(username);
                form.addComponent(userGroup);

                // Password
                Div passGroup = new Div(id + "-pass-group");
                Label passLabel = new Label(password.getId() + "-lbl", "Encryption Key");
                passLabel.setStyleClass("block mb-2 text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]");
                passGroup.addComponent(passLabel);
                password.setStyleClass(
                                "bg-slate-950/80 border border-white/10 text-white text-sm rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 block w-full p-4 transition-all placeholder:text-slate-600");
                passGroup.addComponent(password);
                form.addComponent(passGroup);

                // Role (optional)
                if (roleSelect != null) {
                        Div roleGroup = new Div(id + "-role-group");
                        Label roleLabel = new Label(roleSelect.getId() + "-lbl", "Authorization Level");
                        roleLabel.setStyleClass(
                                        "block mb-2 text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em]");
                        roleGroup.addComponent(roleLabel);
                        roleSelect.setStyleClass(
                                        "bg-slate-950/80 border border-white/10 text-white text-sm rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 block w-full p-4 transition-all");
                        roleGroup.addComponent(roleSelect);
                        form.addComponent(roleGroup);
                }

                loginButton.setStyleClass(
                                "w-full text-white bg-indigo-600 hover:bg-indigo-500 active:scale-[0.98] font-black uppercase tracking-widest rounded-2xl text-sm px-5 py-4 text-center transition-all shadow-[0_10px_15px_-3px_rgba(99,102,241,0.3)] hover:shadow-[0_20px_25px_-5px_rgba(99,102,241,0.4)] mt-4");
                form.addComponent(loginButton);

                cardBody.addComponent(form);
                card.addComponent(cardBody);

                this.addComponent(card);
        }

        @Override
        public String render() {
                // Since Login extends Container, it has children (the card we built)
                // We override render just to set the wrapping container styling which we
                // handled in buildLayout
                // Actually, Container implementation might assume it's just a holder.
                // Let's use custom render to ensure the "flex..." classes on THIS component are
                // applied to the wrapper div.
                // Wait, Container doesn't have a strict "render myself" in base, it depends on
                // impl.
                // Div extends Container. Login extends Container.
                // Component base renderAttributes is available.
                String attrs = renderAttributes();
                return String.format("<section id='%s' class='%s'%s>%s</section>",
                                id, styleClass, attrs, renderChildren()); // Using renderChildren from Container
        }
}
