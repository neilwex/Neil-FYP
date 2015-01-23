package com.fyp;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import javax.servlet.annotation.WebServlet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by o_connor on 28-Jul-14.
 */
@Theme("mytheme")
public class MainUI extends UI{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MainUI.class)
    public static class Servlet extends VaadinServlet {}

    private VerticalLayout content;

    //private Panel explorerPanel;
    private VerticalLayout root;
    private FormLayout loginForm;
    private Label header;
    private TextField loginNameField;
    private PasswordField password;

    private Button adminButton;
    private Button lecturerButton;

    private String enteredUsername;
    private String enteredPassword;
    private String primary_group;
    private String user_email;

    protected static final String LOGIN = "";
    @Override
    protected void init(VaadinRequest vaadinRequest) {

        initLayout();
        initButtons();
    }

    private void initLayout() {
        VerticalLayout view = new VerticalLayout();
        view.setSizeFull();
        HorizontalLayout title = new HorizontalLayout();
        Label heading = new Label("Main Menu");
        title.addComponent(heading);
        view.addComponent(title);
        HorizontalLayout form = new HorizontalLayout();
        form.setSpacing(true);
        view.addComponent(form);
        adminButton = new Button("admin");
        lecturerButton = new Button("lecturer");
        form.addComponent(adminButton);
        form.addComponent(lecturerButton);
        setContent(view);

        //this.setHeight("400px");
        view.setComponentAlignment(title,Alignment.MIDDLE_CENTER);
        view.setComponentAlignment(form, Alignment.MIDDLE_CENTER);
        Page.getCurrent().setTitle("Start");
    }


    private void initButtons() {
        adminButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                System.out.println("Admin Button clicked");

                //navigate to admin page
                //navigateToView(ArchivingBrowser.ARCHIVE_BROWSER);


            }

        });

        lecturerButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                System.out.println("Admin Button clicked");

                String passwordToEncrypt = "test";
                MessageDigest messageDigest = null;
                try {
                    messageDigest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                messageDigest.update(passwordToEncrypt.getBytes());
                String encryptedString = new String(messageDigest.digest());
                String uname = "Neil";
/*

                try {
                    Database.connect();
                    Database.addUser(uname,encryptedString);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
*/



                //navigate to admin page
                //navigateToView(ArchivingBrowser.ARCHIVE_BROWSER);


            }

        });
    }

   /* @Override
    protected void init(VaadinRequest request) {

        content = new VerticalLayout();
        setContent(content);

        Navigator navigator = new Navigator(this, content);

        navigator.addView(UserLogin.LOGIN, UserLogin.class );
        navigator.addView(ArchivingBrowser.ARCHIVE_BROWSER, ArchivingBrowser.class);
        navigator.addView(RetrievingBrowser.RETRIEVAL_BROWSER, RetrievingBrowser.class);

        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {

                // check if a user is logged in
                boolean isLoggedIn = getSession().getAttribute("user") != null;

                // check if desired page is the login page
                boolean isNewViewLoginView = event.getNewView() instanceof UserLogin;

                // check what page user was previously on
                boolean isOldViewRetrieveView = event.getOldView() instanceof RetrievingBrowser;
                boolean isOldViewArchiveView = event.getOldView() instanceof ArchivingBrowser;

                if (!isLoggedIn && !isNewViewLoginView) {

                    // redirect to login view if not already logged in
                    getNavigator().navigateTo(UserLogin.LOGIN);
                    return false;

                } else if (isLoggedIn && isOldViewRetrieveView && !(event.getNewView() instanceof RetrievingBrowser) ) {

                    // redirect user to Retrieve page
                    getNavigator().navigateTo(RetrievingBrowser.RETRIEVAL_BROWSER);
                    Notification.show("You are already logged in");
                    return false;
                } else if (isLoggedIn && isOldViewArchiveView && !(event.getNewView() instanceof ArchivingBrowser) ) {

                    // redirect user to Archive page
                    getNavigator().navigateTo(ArchivingBrowser.ARCHIVE_BROWSER);
                    Notification.show("You are already logged in");
                    return false;
                }

                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {}

        });

    }
*/
    }