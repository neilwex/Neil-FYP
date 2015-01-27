package com.fyp;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

/**
 * Created by Neil on 27/01/2015.
 */
public class AdminMainView {

    public class UserLogin extends VerticalLayout implements View {

        //private Panel explorerPanel;
        private VerticalLayout root;
        private FormLayout loginForm;
        private Label header;
        private TextField loginNameField;
        private PasswordField password;
        private Button loginButton;
        private String enteredUsername;
        private String enteredPassword;
        private String primary_group;
        private String user_email;

        protected static final String LOGIN = "";

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent event) {
            initLayout();
            //initLogin();
        }

        /**
         * Sets up login form
         */
        private void initLayout() {

            // root content
            root = new VerticalLayout();
            root.setSizeFull();
            addComponent(root);
            this.setHeight("400px");
            this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
            Page.getCurrent().setTitle("Login");

            // login form
            loginForm = new FormLayout();
            loginForm.setSpacing(true);
            root.addComponent(loginForm);
            root.setSizeFull();
            root.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
            loginForm.setWidth(null);

            header = new Label("Please login to use the application");
            loginForm.addComponent(header);

            loginNameField = new TextField("Username");
            loginNameField.setRequired(true);
            loginNameField.setRequiredError("Please enter your login name.");
            loginForm.addComponent(loginNameField);

            password = new PasswordField("Password");
            password.setRequired(true);
            password.setRequiredError("Please enter your login password.");
            password.setNullSettingAllowed(false);
            loginForm.addComponent(password);

            loginButton = new Button("Login");
            loginButton.addStyleName("buttons");
            loginForm.addComponent(loginButton);

            //initTextfieldListeners();
        }

    }

}
