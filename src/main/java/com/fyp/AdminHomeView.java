package com.fyp;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

/**
 * Created by Neil on 27/01/2015.
 */
public class AdminHomeView extends VerticalLayout implements View {


    private VerticalLayout root;
    protected static final String ADMIN_HOME = "adminHome";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        initLayout();
    }

    /**
     * Sets up layout
     */
    private void initLayout() {

        // root content
        root = new VerticalLayout();
        root.addStyleName("mainContent");
        root.setSizeFull();
        root.setSpacing(true);
        addComponent(root);
        this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
        Page.getCurrent().setTitle("Admin Home");


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


        System.out.println("Need to add in the ADMIN home here!!!");
        //initTextfieldListeners();
    }

}


