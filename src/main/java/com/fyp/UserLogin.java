package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Neil on 20/01/2015.
 */
@SuppressWarnings("serial")
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
        //loginNameField.setRequired(true);
        //loginNameField.setRequiredError("Please enter your login name.");
        loginForm.addComponent(loginNameField);

        password = new PasswordField("Password");
        //password.setRequired(true);
        //password.setRequiredError("Please enter your login password.");
        password.setNullSettingAllowed(false);
        loginForm.addComponent(password);

        loginButton = new Button("Login");
        loginButton.addStyleName("buttons");
        loginForm.addComponent(loginButton);

        initTextfieldListeners();
    }

    /**
     * Click listener for login button, which authenticates user then displays application choice
     */
    private void initLogin() {

        loginButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                enteredUsername = loginNameField.getValue().trim();
                enteredPassword = password.getValue();

                if ( isValidInputProvided()) {

                    System.out.println("\nUsername provided: " + enteredUsername);

                /*    boolean userCreated = false;

                        try {
                            userCreated = Database.createUser("cat","Cat","Power","cat");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                    System.out.println("User Created Successfully?: " + userCreated);*/

                    try {
                        if (Database.attemptLogin(enteredUsername, enteredPassword) ) {
                            System.out.println("Login Successful!");

                            if (enteredUsername.equals(ADMIN)) {
                                System.out.println("Navigating to admin home...");
                                navigateToView(AdminHomeView.ADMIN_HOME);
                            } else {
                                System.out.println("Navigating to user (non-admin) home...");
                                navigateToView(UserHomeView.USER_HOME);
                                //navigateToView(TestView.TEST_VIEW);
                            }

                        } else {
                            System.out.println("Login Failed!");
                            Notification.show("Login Failed");
                        }

                        //displayOptionWindow(loginMessage);

                        //if Admin, navigate to Admin page

                        //else (if any other user, navigate to their home page)
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    /**
     * Displays pop-up window for selecting which application to access
     * Redirects user to correct view based on which button is selected
     */
    private void displayOptionWindow(String message) {

        // create window to allow user to select which task they require
        final Window optionWindow = new Window("Login");
        optionWindow.setModal(true);
        //optionWindow.setClosable(false);
        optionWindow.setResizable(false);
        optionWindow.setImmediate(true);
        optionWindow.center();

        // set the layout
        final VerticalLayout optionContent = new VerticalLayout();
        optionContent.setMargin(true);
        optionContent.setSpacing(true);
        optionContent.setSizeUndefined();

        // label containing notification info for user
        String notification = "Please select which application you require";
        Label label = new Label (message);
        optionContent.addComponent(label);
        optionContent.setComponentAlignment(label, Alignment.TOP_CENTER);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        optionContent.addComponent(buttons);

        Button archiveButton = new Button("Request Archive");
        Button retrieveButton = new Button("Request Retrieval");

        /*
        archiveButton.addStyleName("buttons");
        retrieveButton.addStyleName("buttons");
        buttons.addComponent(archiveButton);
        buttons.addComponent(retrieveButton);
        */
        optionWindow.setContent(optionContent);
        getUI().addWindow(optionWindow);

        archiveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                optionWindow.close();
                System.out.println("Accessing Archive Application...");

                //attempt mount (if required)
                if ( true ) { //mountUserAccountIfRequired
                    try {
                        navigateToView(UserHomeView.USER_HOME);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        retrieveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                optionWindow.close();
                System.out.println("Accessing Retrieve Application...");

                try {
                    navigateToView(RetrievingBrowser.RETRIEVAL_BROWSER);
                } catch (SQLException e) {
                    e.printStackTrace();
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
            getSession().setAttribute("accountID", USER_FORENAME);


            System.out.println(getSession().getAttribute("user").toString());
            System.out.println(getSession().getAttribute("accountID").toString());

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
            //loginNameField.getRequiredError();
            Notification.show("A username must be provided");
            return false;
        } else if (enteredPassword.equals("") ) {
            System.out.println("Password not provided");
            password.addStyleName("emptyField");
            //password.getRequiredError();
            Notification.show("A password must be provided");
            return false;
        }

        loginNameField.removeStyleName("emptyField");
        password.removeStyleName("emptyField");
        return true;
    }


    /**
     * class for window to be displayed whilst mounting
     */
    private class CallBack {

        private Window notificationWindow;

        public CallBack() {

            // create window for displaying 'wait' notification
            notificationWindow = new Window("Accessing your account");
            notificationWindow.setModal(true);
            notificationWindow.setClosable(false);
            notificationWindow.setResizable(false);
            notificationWindow.setImmediate(true);
            notificationWindow.center();

            // set the layout
            VerticalLayout notificationContent = new VerticalLayout();
            notificationContent.setMargin(true);
            notificationContent.setSpacing(true);
            notificationContent.setSizeUndefined();

            // label containing notification info for user
            String notification = "The file system is mounting.<br>" +
                                  "This may take some time. Please be patient...";
            Label message1 = new Label (notification, ContentMode.HTML);
            notificationContent.addComponent(message1);
            notificationContent.setComponentAlignment(message1, Alignment.MIDDLE_CENTER);

            // gif loading image
            Resource res = new ThemeResource("img/waiting1.gif");
            Image image = new Image(null, res);
            notificationContent.addComponent(image);
            notificationContent.setComponentAlignment(image, Alignment.BOTTOM_CENTER);

            notificationWindow.setContent(notificationContent);
        }

        public void start() {
            // Open pop-up in the UI
            getUI().addWindow(notificationWindow);
            getUI().setPollInterval(500);
        }

        public void done(UI ui) {
            //System.out.println("Closing 'loading' pop-up window...");
            ui.setPollInterval(-1);
            notificationWindow.close();
        }

    }

    /**
     * class for executing the mount (with a separate thread)
     */
    private class MyThread extends Thread {

        private CallBack callback;
        private String execCommand;
        private UI ui;

        public MyThread(CallBack callback, String execCommand, UI ui) {
            this.callback = callback;
            this.execCommand = execCommand;
            this.ui = ui;
        }

        @Override
        public void run() {
            boolean mountComplete;

            System.out.println("Mounting...");
            try {
                // mount and wait until complete
                Runtime.getRuntime().exec(execCommand).waitFor();
                mountComplete = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                mountComplete = false;
            } catch (IOException e) {
                e.printStackTrace();
                mountComplete = false;
            }

            if (mountComplete) {
                System.out.println("Mounting complete");
                callback.done(ui);
            }
        }

    }


}