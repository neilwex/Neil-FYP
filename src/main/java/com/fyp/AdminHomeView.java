package com.fyp;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

/**
 * Created by Neil on 27/01/2015.
 */
public class AdminHomeView extends CssLayout implements View {

    //private Panel explorerPanel;
    private VerticalLayout root;
    protected static final String ADMIN_HOME = "adminHome";

    private HorizontalLayout horizontalLayout_2;
    private Button logoutButton;
    private Label label_2;
    private AbsoluteLayout mainLayout;
    private CustomComponent c;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        initLayout();
    }

    /**
     * Sets up layout
     */
    private void initLayout() {

        setWidth("100.0%");
        setHeight("100.0%");

        mainLayout = new AbsoluteLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        addComponent(mainLayout);
        Button donkey = new Button("Donkey");
        mainLayout.addComponent(donkey, "top:20.0px;left:640.0px;");


        //addComponent(root);
        // root content
       /* root = new VerticalLayout();
        root.setSizeFull();
        addComponent(root);
        //this.setHeight("400px");
        //this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
*/

        // root content

        Page.getCurrent().setTitle("Admin");

        // horizontalLayout_2
        //HorizontalLayout horizontalLayout_2 = buildHorizontalLayout_2("Neil");
        //c.com
        //mainLayout.addComponent(horizontalLayout_2, "top:20.0px;left:640.0px;");

        //mainLayout.addComponent(new Label("NEIL TESTING THIS"));
        System.out.println("Need to add in the ADMIN home here!!!");
        //initTextfieldListeners();
    }

    private HorizontalLayout buildHorizontalLayout_2(String user) {
        // common part: create layout
        horizontalLayout_2 = new HorizontalLayout();
        horizontalLayout_2.setImmediate(false);
        horizontalLayout_2.setWidth("-1px");
        horizontalLayout_2.setHeight("-1px");
        horizontalLayout_2.setMargin(false);
        horizontalLayout_2.setSpacing(true);

        // label_2
        label_2 = new Label();
        label_2.setImmediate(false);
        label_2.setWidth("-1px");
        label_2.setHeight("-1px");
        label_2.setValue("Logged in: " + user);
        horizontalLayout_2.addComponent(label_2);
        horizontalLayout_2.setComponentAlignment(label_2, new Alignment(48));

        // logoutButton
        logoutButton = new Button();
        logoutButton.setCaption("Logout");
        logoutButton.setImmediate(false);
        logoutButton.setWidth("-1px");
        logoutButton.setHeight("-1px");
        horizontalLayout_2.addComponent(logoutButton);

        return horizontalLayout_2;
    }

}


