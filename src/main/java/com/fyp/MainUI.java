package com.fyp;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import javax.servlet.annotation.WebServlet;

/**
 * Created by Neil on 20/01/2015.
 */
@Theme("mytheme")
public class MainUI extends UI{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MainUI.class)
    public static class Servlet extends VaadinServlet {}

    private VerticalLayout content;

    protected static final String LOGIN = "login";

    @Override
    protected void init(VaadinRequest request) {

        content = new VerticalLayout();
        setContent(content);

        Navigator navigator = new Navigator(this, content);

        navigator.addView(UserLogin.LOGIN, UserLogin.class );
        navigator.addView(AdminHomeView.ADMIN_HOME, AdminHomeView.class);
        navigator.addView(UserHomeView.USER_HOME, UserHomeView.class);

        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {

                // check if a user is logged in
                boolean isLoggedIn = getSession().getAttribute("user") != null;

                // check if desired page is the login page
                boolean isLoginView = event.getNewView() instanceof UserLogin;

                if (!isLoggedIn && !isLoginView) {

                    // redirect to login view if not already logged in
                    getNavigator().navigateTo(UserLogin.LOGIN);
                    return false;

                } else if (isLoggedIn && isLoginView) {

                    // redirect user to Retrieve page
                    getNavigator().navigateTo(UserHomeView.USER_HOME);
                    Notification.show("You are already logged in");
                    return false;
                } /*else if (isLoggedIn && isOldViewArchiveView && !(event.getNewView() instanceof UserHomeView) ) {

                    // redirect user to Archive page
                    getNavigator().navigateTo(UserHomeView.USER_HOME);
                    Notification.show("You are already logged in");
                    return false;
                }*/

                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {}

        });

    }

}