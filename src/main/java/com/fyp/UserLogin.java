package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by o_connor on 16-Jul-14.
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

                    if ( authenticateLDAPUser() ) {

                        //Display window with two buttons
                        displayOptionWindow();
                    }
                }

            }
        });
    }

    /**
     * Authenticates the user with LDAP using simple authentication
     * Creates a HashTable containing group name/id, from which user's group is found
     * Closes the LDAP connection after successful authentication
     *
     * @return      true if LDAP authentication is successful; otherwise, false
     */
    private boolean authenticateLDAPUser() {

        String ldapURL = "ldap://ocs.embl.org:389";
        String loginFailed = "Login unsuccessful. Please enter your details again";

        // set up the environment for creating the initial context
        Hashtable<String, String> anonEnv = new Hashtable<String, String>();
        anonEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        anonEnv.put(Context.PROVIDER_URL, ldapURL);
        anonEnv.put(Context.SECURITY_AUTHENTICATION, "none");

        // boolean to record success of method
        boolean successful = false;

        try {

            // create the initial context with anonymous authentication
            DirContext ctx = new InitialDirContext(anonEnv);

            try {

                // search for account belonging to provided username
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> userSearch = ctx.search("", "(&(objectClass=posixAccount)(uid=" + enteredUsername + "))", controls);

                String posixGroupNumber = "";
                LinkedList accountsFound = new LinkedList();
                while (userSearch.hasMore()) {
                    SearchResult result = userSearch.next();
                    //System.out.println(result.toString());
                    accountsFound.add(result.getName());

                    // get the user's group number
                    Attribute groupIdAttribute = result.getAttributes().get("gidnumber");
                    if (groupIdAttribute != null) {
                        posixGroupNumber = (String)groupIdAttribute.get();
                    }
                    //System.out.println("Posix Group Number: " + posixGroupNumber);

                    // get the user's email
                    Attribute mailAttribute = result.getAttributes().get("mail");
                    user_email = (String) mailAttribute.get();
                    //System.out.println("User Email: " + user_email);
                }

                System.out.println("Accounts with given username found: " + accountsFound.size());

                // if exactly one entry is found (with a possix group), use this entry for simple authentication
                if (accountsFound.size() != 1 || posixGroupNumber.isEmpty() ) {
                    System.out.println("Account not found");
                } else {
                    String dn = accountsFound.get(0).toString();
                    //System.out.println(dn);

                    try {
                        // close the original context
                        ctx.close();

                        // set up the environment for simple authentication
                        Hashtable<String, String> simpleEnv = new Hashtable<String, String>();
                        simpleEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                        simpleEnv.put(Context.PROVIDER_URL, ldapURL);
                        simpleEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
                        simpleEnv.put(Context.SECURITY_PRINCIPAL, dn);
                        simpleEnv.put(Context.SECURITY_CREDENTIALS, enteredPassword);

                        try {
                            // create the initial context with simple authentication
                            DirContext ctx2 = new InitialDirContext(simpleEnv);

                            try {
                                System.out.println("LDAP connection successfully opened");

                                // create HashTable storing groupNames and groupIDs
                                Hashtable<String, String> groups = new Hashtable<String, String>();
                                NamingEnumeration<SearchResult> groupSearch = ctx2.search("", "(objectClass=posixGroup)", controls);
                                while (groupSearch.hasMore()) {
                                    SearchResult testSearchResult = groupSearch.next();
                                    Attributes attributes = testSearchResult.getAttributes();
                                    String groupName = (String) (attributes.get("cn").get());
                                    if (groupName.equals("cng") ) {
                                        groupName = "its";
                                    }
                                    String gidNumber = (String) (attributes.get("gidnumber").get());

                                    if (!groups.containsKey(gidNumber)) {
                                        groups.put(gidNumber, groupName); // add if not already in HashTable
                                    }
                                }
                                //printHashTable(groups);

                                // find user's group from the HashTable
                                if (groups.containsKey(posixGroupNumber)) {
                                    primary_group = groups.get(posixGroupNumber);
                                }

                                if (primary_group.isEmpty()) {
                                    System.out.println("No group associated with user found");

                                } else {
                                    System.out.print("LDAP Authentication complete ");
                                    System.out.println("(User " + enteredUsername + " belongs to group: " + primary_group + ")");

                                    try {
                                        // close the connection
                                        ctx2.close();
                                        System.out.println("LDAP connection successfully closed");
                                        successful = true;

                                    } catch (Exception e) {
                                        System.out.println("Closing of LDAP connection failed");
                                        e.printStackTrace();
                                    }
                                }

                            } catch (Exception e) {
                                System.out.println("User/group search failed");
                                e.printStackTrace();
                            }

                        } catch (AuthenticationException e) {
                            System.out.println("Authentication Exception: LDAP connection failed");
                            e.printStackTrace();
                        } catch (NamingException ne) {
                            System.out.println("Naming Exception: LDAP connection failed");
                            ne.printStackTrace();
                        }

                    } catch (NamingException e) {
                        System.out.println("Anonymous context not closed properly!");
                        e.printStackTrace();
                    }

                } // end of else

            } catch (NamingException e) {
                System.out.println("Initial context search failed!");
                e.printStackTrace();
            }

        } catch (NamingException e) {
            System.out.println("Anonymous authentication initial context failed");
            e.printStackTrace();
        } finally {
            if (! successful) {
                System.out.println("LDAP Authentication Failure");
                Notification.show(loginFailed);
            }
            return successful;

        }
    }

    /**
     * Displays pop-up window for selecting which application to access
     * Redirects user to correct view based on which button is selected
     */
    private void displayOptionWindow() {

        // create window to allow user to select which task they require
        final Window optionWindow = new Window("Select application");
        optionWindow.setModal(true);
        optionWindow.setClosable(false);
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
        Label message = new Label (notification);
        optionContent.addComponent(message);
        optionContent.setComponentAlignment(message, Alignment.TOP_CENTER);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        optionContent.addComponent(buttons);

        Button archiveButton = new Button("Request Archive");
        Button retrieveButton = new Button("Request Retrieval");
        archiveButton.addStyleName("buttons");
        retrieveButton.addStyleName("buttons");
        buttons.addComponent(archiveButton);
        buttons.addComponent(retrieveButton);

        optionWindow.setContent(optionContent);
        getUI().addWindow(optionWindow);

        archiveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                optionWindow.close();
                System.out.println("Accessing Archive Application...");

                //attempt mount (if required)
                if ( mountUserAccountIfRequired() ) {
                    navigateToView(ArchivingBrowser.ARCHIVE_BROWSER);
                }
            }
        });

        retrieveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                optionWindow.close();
                System.out.println("Accessing Retrieve Application...");

                navigateToView(RetrievingBrowser.RETRIEVAL_BROWSER);
            }
        });
    }

    /**
     * Mounts the user account (if required) using their group and login details
     *
     * @return      true if the user mount is not required
     *              OR if the user mount is required and is successful
     *              otherwise, returns false
     */
    private boolean mountUserAccountIfRequired() {

        // boolean to record success of method
        boolean successful = true;

        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDirectory);

        File file = new File(workingDirectory + "/groupShareMounts/" + primary_group + "/" + enteredUsername);

        // if directory doesn't already exist, create it
        if (!file.exists()) {
            System.out.println("Creating directory in which to mount...");
            boolean result = file.mkdirs();
            System.out.println(result ? "Directory for mounting created" : "Directory for mounting not created");
            if (!result) {
                System.out.println("Problem creating directory");
                successful = false;
            }
        } else if (!file.isDirectory()) {
            // there is something with this name which is NOT a directory => new dir. can't be created, what to do...
            System.out.println("File exists but is not a directory...");
        }

        int directoryContents = file.list().length;
        //System.out.println("Contents size : " + directoryContents );

        if (successful) {
            // if no contents, needs to mount
            if (directoryContents == 0) {
                System.out.println("Directory contents empty - mount required");

                try {

                    // display window for waiting
                    CallBack cb = new CallBack();
                    cb.start();

                    // mount command with user's details
                    String command = "/usr/bin/smbmount //localhost/storage groupShareMounts/" + primary_group + "/"
                            + enteredUsername + " -o username=" + enteredUsername + ",password=" + enteredPassword;

                    // create new thread and run the mount command with the user's details
                    MyThread thread = new MyThread(cb, command, getUI());
                    thread.start();


                } catch (Exception e) {
                    System.out.println("Mount unsuccessful!");
                    successful = false;
                    e.printStackTrace();
                }

            } else {
                System.out.println("Directory contents not empty - mount not required");
            }
        }

        if (! successful) {
            Notification.show("Mount unsuccessful!");
        }

        return successful;
    }

    /**
     * Creates the session information and then navigates to requested view
     *
     * @param view  string containing view to which to navigate
     */
    private void navigateToView(String view) {

            // store the current user in the service session
            getSession().setAttribute("user", enteredUsername);
            getSession().setAttribute("group", primary_group);
            getSession().setAttribute("email", user_email);

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
            loginNameField.getRequiredError();
            Notification.show("A username must be provided");
            return false;
        } else if (enteredPassword.equals("") ) {
            System.out.println("Password not provided");
            password.addStyleName("emptyField");
            password.getRequiredError();
            Notification.show("A password must be provided");
            return false;
        }

        loginNameField.removeStyleName("emptyField");
        password.removeStyleName("emptyField");
        return true;
    }

    /**
     * Prints out the Hashtable for group ID/name pairs
     *
     * @param groups Hashtable containing group ID mapped to group name
     */
    private void printHashTable(Hashtable<String, String> groups) {

        System.out.println("Number of HashTable entries: " + groups.size());
        System.out.println("\nHashTable contents (Group Id: Group Name):");
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + ": " + value);
        }
        System.out.println("");
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