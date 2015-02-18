package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.sql.ResultSet;
import java.sql.SQLException;

//import org.tepi.filtertable.FilterTable;

/**
 * Created by Neil on 25/01/2015.
 */
@SuppressWarnings("serial")
public class UserHomeView extends VerticalLayout implements View {

    private VaadinSession session;
    private VerticalLayout root;

    private Window addNewModuleWindow;
    private TabSheet tabsheet;
    protected static final String USER_HOME = "userHome";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        session = getSession();

        try {
            initLayout();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up content for the UI
     */
    private void initLayout() throws SQLException {

        // root content
        root = new VerticalLayout();
        root.addStyleName("mainContent");
        root.setSizeFull();
        root.setSpacing(true);
        addComponent(root);
        this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
        Page.getCurrent().setTitle("User Home");

        // session information
        SessionInfo sessionInfo1 = new SessionInfo(session, getUI());
        root.addComponent(sessionInfo1);
        root.setComponentAlignment(sessionInfo1, Alignment.TOP_RIGHT);

        Label newHeading = new Label("Welcome back, " + UserLogin.USER_FORENAME );
        newHeading.addStyleName("heading");
        newHeading.setWidth("100%");
        root.addComponent(newHeading);

        Label instructions = new Label("Please click on the tabs below to explore your modules.<br>" +
                "Alternatively, you can add new module information by clicking below.<br>" +
                "Please note: All newly added modules must be first approved by the administrators before they can be used." , ContentMode.HTML);

        instructions.addStyleName("instructions");
        instructions.setHeight("90px");
        root.addComponent(instructions);

        Button addModule = new Button("Add Module", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                displayAddModuleWindow();
            }
        });
        addModule.addStyleName("buttons");
        root.addComponent(addModule);

        // add tabsheet with module data for current user
        final ResultSet moduleInfo = Database.getModuleStats(UserLogin.USER_ACC_NUM);
        tabsheet = new ModulesTabSheet(moduleInfo);
        root.addComponent(tabsheet);
    }

    /**
     * Display form for adding new module
     */
    private void displayAddModuleWindow() {

        // Create a sub-window and set the content
        addNewModuleWindow = new Window("Add New Module");
        addNewModuleWindow.setWidth("440px");
        addNewModuleWindow.setHeight("-1px");
        addNewModuleWindow.setModal(true);
        addNewModuleWindow.setResizable(false);
        addNewModuleWindow.center();
        addNewModuleWindow.setImmediate(true);

        /// Create form for user data
        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        addNewModuleWindow.setContent(form);

        final TextField moduleCode = new TextField("Module Code");
        moduleCode.addValueChangeListener(new FieldChangeListener(moduleCode));
        form.addComponent(moduleCode);

        final TextField moduleTitle = new TextField("Module Title");
        moduleTitle.addValueChangeListener(new FieldChangeListener(moduleTitle));
        form.addComponent(moduleTitle);

        final ComboBox creditCombo = new ComboBox("Credit Weighting");
        form.addComponent(creditCombo);

        // Add selections for combo box
        creditCombo.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT_DEFAULTS_ID);
        creditCombo.setNullSelectionAllowed(false);
        creditCombo.setNewItemsAllowed(false);
        creditCombo.addItem(new Integer(5));
        creditCombo.addItem(new Integer(10));
        creditCombo.addItem(new Integer(15));
        creditCombo.addItem(new Integer(20));

        //set combo box default to first item
        creditCombo.setValue(creditCombo.getItemIds().iterator().next());

        final TextField ca = new TextField("Continuous Assessment (%)");
        ca.setConverter(Integer.class);
        ca.setValue("20");
        form.addComponent(ca);

        final TextField exam = new TextField("Exam (%)");
        exam.setConverter(Integer.class);
        exam.setValue("80");
        form.addComponent(exam);

        //listener for updating exam figure when ca is changed
        ca.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                System.out.println("CA new value is: " + ca.getValue());

                try {
                    int newValue;
                    if (ca.getValue() == null || ca.getValue().trim() == null || ca.getValue().trim().isEmpty() ) {
                        newValue = 0 ;
                    } else {
                        newValue = (Integer) ca.getConvertedValue();
                    }

                    int examValue = 100 - newValue;
                    if (examValue > 100 || examValue < 0) {
                        ca.addStyleName("emptyField");
                        new Notification("Invalid range: Please enter a number 0-100",
                                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                    } else {
                        ca.removeStyleName("emptyField");
                        exam.setValue(Integer.toString(examValue));
                    }
                } catch (Exception e) {
                    ca.setValue("0");
                    exam.setValue("100");
                }
            }
        });

        //listener for updating ca figure when exam is changed
        exam.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                try {

                    int newValue;
                    if (exam.getValue() == null || exam.getValue().trim() == null || exam.getValue().trim().isEmpty() ) {
                        newValue = 0 ;
                    } else {
                        newValue = (Integer) exam.getConvertedValue();
                    }

                    int caValue = 100 - newValue;
                    if (caValue > 100 || caValue < 0) {
                        exam.addStyleName("emptyField");
                        new Notification("Invalid range: Please enter a number 0-100",
                                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                    } else {
                        exam.removeStyleName("emptyField");
                        ca.setValue(Integer.toString(caValue));
                    }
                } catch (Exception e) {
                    exam.setValue("0");
                    ca.setValue("100");
                }
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button submit = new Button("Submit", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                // check all fields are valid. If not, highlight red
                boolean allFieldsValid = true;

                if ( moduleCode.getValue().trim().isEmpty() ) {
                    moduleCode.addStyleName("emptyField");
                    allFieldsValid = false;
                }

                if ( moduleTitle.getValue().trim().isEmpty() ) {
                    moduleTitle.addStyleName("emptyField");
                    allFieldsValid = false;
                }

                try {

                    int intExamValue = (Integer) exam.getConvertedValue();
                    int intCaValue = (Integer) ca.getConvertedValue();

                    if (intExamValue > 100 || intExamValue < 0 || intCaValue > 100 || intCaValue < 0) {
                        allFieldsValid = false;
                    }

                } catch (Converter.ConversionException e) {
                    allFieldsValid = false;
                }

                if (! allFieldsValid) {
                    new Notification("All fields must be completed correctly - please fill highlighted fields",
                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                } else {
                    Notification.show("Form complete!");

                    String trimmedModuleCode = moduleCode.getValue().trim();
                    String trimmedModuleTitle = moduleTitle.getValue().trim();

                    try {

                        boolean moduleCodeExists = Database.checkModuleCode(trimmedModuleCode);

                        if (moduleCodeExists) {
                            moduleCode.addStyleName("emptyField");
                            new Notification("The module code entered already exists - please enter a unique module code",
                                    Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                        } else {

                            boolean addedSuccessfully = Database.addNewModule(trimmedModuleCode, trimmedModuleTitle, creditCombo.getValue().toString(),
                                    ca.getValue(), exam.getValue(), UserLogin.USER_ACC_NUM);

                            if (addedSuccessfully) {
                                Notification.show("New module successfully added");

                                // refresh page to display new module
                                UI.getCurrent().getNavigator().navigateTo(UserHomeView.USER_HOME);
                            } else {
                                new Notification("An error was encountered - failed to add new module",
                                        Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        new Notification("An error was encountered - failed to add new module",
                                Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                    }

                }

            }
        });

        Button cancel = new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                addNewModuleWindow.close();
            }
        });

        submit.addStyleName("buttons");
        cancel.addStyleName("buttons");
        buttons.addComponent(submit);
        buttons.addComponent(cancel);
        form.addComponent(buttons);

        // display whether file will be kept/deleted after archiving
        String notification = "Your request has been sent to the IT Department who will notify you when archiving has been completed.<br>" +
                              "You have requested for your selected file to be ... after archiving";
        Label fileStatusText = new Label(notification, ContentMode.HTML);

        // Open window in the UI
        getUI().addWindow(addNewModuleWindow);
    }


} //end of class