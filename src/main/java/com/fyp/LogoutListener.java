package com.fyp;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by o_connor on 19-Aug-14.
 */
public class LogoutListener extends VerticalLayout implements Button.ClickListener {

    private VaadinSession session;
    private final UI ui;

    protected static final String APP_URL = "http://archive.embl.de:8080/archiveApp-1.0/";

    public LogoutListener (VaadinSession session, UI ui) {
        this.session = session;
        this.ui = ui;
    }

    public void buttonClick(Button.ClickEvent event) {

        // end the session and redirect the user to login page
        System.out.println("Logging out...");

        try {
            // "Logout" the user
            session.close();

            // display login page
            ui.getPage().setLocation(APP_URL);

        } catch (Exception e) {
            System.out.println("Error!");
            Notification.show("Error encountered. Please try again");
            e.printStackTrace();
        }

    }

}