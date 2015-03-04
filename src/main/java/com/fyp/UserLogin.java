package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Neil on 20/01/2015.
 */
@SuppressWarnings("serial")
public class UserLogin extends VerticalLayout implements View {

    private VerticalLayout root;
    private FormLayout loginForm;
    private Label header;
    private TextField loginNameField;
    private PasswordField password;
    private Button loginButton;
    private String enteredUsername;
    private String enteredPassword;

    protected static final String LOGIN = "";
    protected static final String ADMIN = "admin";

    protected static String USER_NAME;
    protected static int USER_ACC_NUM;
    protected static String USER_FORENAME;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        initLayout();
        initLogin();
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
        loginForm.addComponent(loginNameField);

        password = new PasswordField("Password");
        password.setNullSettingAllowed(false);
        loginForm.addComponent(password);

        loginButton = new Button("Login");
        loginButton.addStyleName("buttons");
        loginForm.addComponent(loginButton);

        initTextfieldListeners();
    }

    /**
     * Click listener for login button, which authenticates user then navigates to appropriate view
     */
    private void initLogin() {

        loginButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                enteredUsername = loginNameField.getValue().trim();
                enteredPassword = password.getValue();

                if ( isValidInputProvided()) {

                    System.out.println("Username provided: " + enteredUsername);

                    try {
                        if (Database.attemptLogin(enteredUsername, enteredPassword) ) {
                            System.out.println("Login Successful!");

                            if (enteredUsername.equals(ADMIN)) {
                                System.out.println("Navigating to admin home...");
                                navigateToView(AdminHomeView.ADMIN_HOME);
                            } else {
                                System.out.println("Navigating to user (non-admin) home...");
                                navigateToView(UserHomeView.USER_HOME);
                            }

                        } else {
                            System.out.println("Login Failed!");
                            new Notification("Login Failed", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                            //Notification.show("Login Failed");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    /**
     * Creates the session information and then navigates to requested view
     *
     * @param view  string containing view to which to navigate
     */
    private void navigateToView(String view) throws SQLException {

            ResultSet userData = Database.getList("SELECT accountID, forename FROM users WHERE userID = \"" + enteredUsername + "\"");
            userData.next();

            // setup session global details
            USER_NAME = enteredUsername;
            USER_ACC_NUM = userData.getInt("accountID");
            USER_FORENAME = userData.getString("forename");

            // store current user info in the session
            getSession().setAttribute("user", USER_NAME);
            getSession().setAttribute("accountID", USER_ACC_NUM);
            getSession().setAttribute("userForename", USER_FORENAME);

            // navigate to desired view
            UI.getCurrent().getNavigator().navigateTo(view);
    }

    /**
     * Set the text listeners to change the class names for CSS
     */
    private void initTextfieldListeners() {
        loginNameField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                // Get the entered username
                String enteredValue = event.getProperty().getValue().toString().trim();

                if (enteredValue.length() != 0) {
                    loginNameField.removeStyleName("emptyField");
                } else {
                    loginNameField.addStyleName("emptyField");
                }
            }
        });

        password.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                // Get the entered password
                String enteredValue = event.getProperty().getValue().toString();

                if (enteredValue.length() != 0) {
                    password.removeStyleName("emptyField");
                } else {
                    password.addStyleName("emptyField");
                }
            }
        });

        loginNameField.setImmediate(true);
        password.setImmediate(true);

    }

    /**
     * Validates user input for username and password
     * Ensures no empty information entered and applies CSS styles to TextFields accordingly
     *
     * @return      true if user input information valid; otherwise, false
     */
    private boolean isValidInputProvided() {

        if ( enteredUsername.equals("") ) {
            System.out.println("Username not provided");
            loginNameField.addStyleName("emptyField");
            new Notification("A username must be provided", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
            return false;
        } else if (enteredPassword.equals("") ) {
            System.out.println("Password not provided");
            password.addStyleName("emptyField");
            new Notification("A password must be provided", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
            return false;
        }

        loginNameField.removeStyleName("emptyField");
        password.removeStyleName("emptyField");
        return true;
    }


} //end of class