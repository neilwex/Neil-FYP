package com.fyp;

import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Created by Neil on 15/02/2015.
 */
public class SessionInfo extends HorizontalLayout{

    HorizontalLayout sessionInfo;
    Label userDetails;
    Button logoutButton;

    public SessionInfo (VaadinSession session, UI ui) {

        sessionInfo = new HorizontalLayout();
        sessionInfo.setSpacing(true);
        sessionInfo.setHeight("100px");
        sessionInfo.setWidth(null);
        sessionInfo.setMargin(new MarginInfo(true, true, false, false));
        addComponent(sessionInfo);

        // display session information and logout button
        Label userDetails = new Label("Logged in: " + UserLogin.USER_NAME);
        sessionInfo.addComponent(userDetails);

        // logout button
        logoutButton = new Button("Logout");
        //logoutButton.addStyleName(BaseTheme.BUTTON_LINK);
        logoutButton.addStyleName("buttons");
        //logoutButton.setSizeUndefined();
        sessionInfo.addComponent(logoutButton);
        logoutButton.addClickListener(new LogoutListener(session, ui));
    }
}
