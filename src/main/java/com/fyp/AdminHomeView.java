package com.fyp;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Neil on 27/01/2015.
 */
public class AdminHomeView extends VerticalLayout implements View {


    private VerticalLayout root;
    protected static final String ADMIN_HOME = "adminHome";
    private VaadinSession session;
    private VerticalLayout contentLayout;
    private TabSheet tabsheet;

    private String VIEW_MODS = "View Modules";
    private String VIEW_STUDENTS = "View Students";
    private String GEN_REPORT = "Generate Report";
    private String VIEW_PENDING = "View Pending Modules";
    private String ADD_USER = "Add User";

    private NativeSelect studentSelection;
    private Container studentMenu;
    private VerticalLayout studentInfo;
    private Container gradesContainer;

    private final String MODULE = "Module";
    private final String CREDITS = "Credits";
    private final String CA = "CA";
    private final String EXAM = "Exam";
    private final String RESULT = "Result";
    private final String PERCENT = "Percentage";
    private final String AWARD = "Award";
    private final String GPA = "GPA Grade";


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        session = getSession();
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

        // session information
        SessionInfo sessionInfo = new SessionInfo(session, getUI());
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);

        Label newHeading = new Label("Admin Home");
        newHeading.addStyleName("heading");
        newHeading.setWidth("100%");
        root.addComponent(newHeading);

        MenuBar menuBar = new MenuBar();
        menuBar.addStyleName("menuBar");
        root.addComponent(menuBar);

        contentLayout = new VerticalLayout();

        // Define a common menu command for all the menu items.
        MenuBar.Command mycommand = new MenuBar.Command() {
            MenuBar.MenuItem previous = null;

            public void menuSelected(MenuBar.MenuItem selectedItem) {
                System.out.println("getText(): " + selectedItem.getText());
                System.out.println("getText(): " + selectedItem);

                if (previous != null) {
                    previous.setStyleName(null);
                }
                System.out.println("selectedItem: " + selectedItem);
                selectedItem.setStyleName("highlight");
                previous = selectedItem;

                if (selectedItem.getText().equals(VIEW_MODS)) {

                    try {
                        displayModulesContent();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (selectedItem.getText().equals(VIEW_STUDENTS)) {

                    try {
                        displayStudentsContent();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else if (selectedItem.getText().equals(VIEW_PENDING)) {

                    try {
                        displayPendingModulesContent();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                } else if (selectedItem.getText().equals(ADD_USER)) {

                    displayAddUserContent();
                }

            }
        };

        menuBar.addItem(VIEW_MODS,null,mycommand);
        menuBar.addItem(VIEW_STUDENTS,null,mycommand);
        menuBar.addItem(GEN_REPORT,null,mycommand);
        menuBar.addItem(VIEW_PENDING,null,mycommand);
        menuBar.addItem(ADD_USER,null,mycommand);

        contentLayout.addComponent(new Label("Please choose an option from the above navigation menu"));
        root.addComponent(contentLayout);
    }

    /**
     * displays details of students' grades
     */
    private void displayStudentsContent() throws SQLException {

        clearExistingContent();

        studentMenu = new IndexedContainer();
        studentMenu.addContainerProperty("Student", String.class, null);

        studentSelection = new NativeSelect("Select a student");
        studentSelection.setImmediate(true);
        studentSelection.setNullSelectionAllowed(false);

        ResultSet students = Database.getStudents();

        // Round percentage to 2 decimal points
        while (students.next()) {

            String studentNum = students.getString("student_num");

            // add details as a row to table
            studentMenu.addItem(studentNum);
            studentMenu.getContainerProperty(studentNum, "Student").setValue(studentNum);
        }

        // setup grid layout for each student
        studentInfo = new VerticalLayout();
        studentInfo.setSpacing(true);
        studentInfo.addStyleName("studentInfo");

        studentSelection.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

                contentLayout.removeComponent(studentInfo);
                studentInfo.removeAllComponents();

                try {
                    createStudentInfo(valueChangeEvent.getProperty().getValue().toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                contentLayout.addComponent(studentInfo);
            }
        });

        studentSelection.setContainerDataSource(studentMenu);

        contentLayout.addComponent(studentSelection);
        root.addComponent(contentLayout);
    }

    /**
     * creates grid with student information
     */
    private void createStudentInfo(final String student) throws SQLException {

        Label studentNum = new Label("Student Num: " + student);
        studentNum.addStyleName("studentNum");
        studentInfo.addComponent(studentNum);

        gradesContainer = new IndexedContainer();
        gradesContainer.addContainerProperty(MODULE, String.class, null);
        gradesContainer.addContainerProperty(CREDITS, Integer.class, null);
        gradesContainer.addContainerProperty(CA, Integer.class, null);
        gradesContainer.addContainerProperty(EXAM, Integer.class, null);
        gradesContainer.addContainerProperty(RESULT, Integer.class, null);
        gradesContainer.addContainerProperty(PERCENT, Double.class, null);
        gradesContainer.addContainerProperty(AWARD, String.class, null);
        gradesContainer.addContainerProperty(GPA, String.class, null);

        Table resultsTable = new Table();
        resultsTable.setSelectable(true);
        resultsTable.setContainerDataSource(gradesContainer);

        ResultSet results = Database.getStudentInfo(student);

        // For rounding to 2 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        int creditsFailed = 0;
        boolean passByComp = true;

        int totalCredits = 0;
        int totalMarks = 0;
        while (results.next()) {

            // get module specific data
            String moduleCode = results.getString("module_code");
            int ca = results.getInt("ca_mark");
            int exam = results.getInt("final_exam_mark");
            int credits = results.getInt("credit_weighting");
            double percentage = Double.parseDouble(df.format((ca + exam) * 5.0 / credits));

            // add details as a row to table
            gradesContainer.addItem(moduleCode);
            gradesContainer.getContainerProperty(moduleCode, MODULE).setValue(moduleCode);
            gradesContainer.getContainerProperty(moduleCode, CREDITS).setValue(credits);
            gradesContainer.getContainerProperty(moduleCode, CA).setValue(ca);
            gradesContainer.getContainerProperty(moduleCode, EXAM).setValue(exam);
            gradesContainer.getContainerProperty(moduleCode, RESULT).setValue(ca + exam);
            gradesContainer.getContainerProperty(moduleCode, PERCENT).setValue(percentage);
            gradesContainer.getContainerProperty(moduleCode, AWARD).setValue(GradeCalculator.getAward(percentage));
            gradesContainer.getContainerProperty(moduleCode, GPA).setValue(GradeCalculator.getGPGrade(percentage));

           if (percentage < Database.PASS_MARK) {
               creditsFailed += credits;
               if (percentage < Database.COMPENSATION_MARK) {
                   passByComp = false;
               }
           }

            totalCredits += credits;
            totalMarks += ca + exam;
        }

        double overallPercentage = totalMarks * 5.0 / totalCredits;

        // footer for totals/overall
        resultsTable.setFooterVisible(true);
        resultsTable.setColumnFooter(MODULE, "OVERALL");
        resultsTable.setColumnFooter(CREDITS, Integer.toString(totalCredits));
        resultsTable.setColumnFooter(PERCENT, df.format(overallPercentage));
        resultsTable.setColumnFooter(AWARD, GradeCalculator.getAward(overallPercentage));
        resultsTable.setColumnFooter(GPA, GradeCalculator.getGPGrade(overallPercentage));
        resultsTable.setPageLength(resultsTable.size());

        studentInfo.addComponent(resultsTable);

        String notification;
        if (totalCredits < 60) {
            notification = "System has results for only " + totalCredits + " credits for student " + student + ".<br>" +
                           "Please ensure all results have been submitted for this student";
        } else if (totalCredits > 60) {
            notification = "System has results for " + totalCredits + " credits for student " + student + ".<br>" +
                           "Students should only be registered for a maximum of 60 credits.<br>" +
                           "Please check these results again.";
        } else { //correct number of credits
            notification = "System has results for " + totalCredits + " credits for student " + student + ".<br>";

            if (creditsFailed == 0) {
                notification += "Student has passed all modules.";
            } else if (creditsFailed > 10) {
                notification += "Student has failed " + creditsFailed + " credits.<br>" +
                                "Student is therefore ineligible to pass by compensation.";
            } else { // failed between 0 and 10 credits

                notification += "Student " + student + " has failed " + creditsFailed + " credits.<br>";

                if (overallPercentage < Database.PASS_MARK) {
                    notification += "Overall average is below 40% so student is ineligible to pass by compensation.";
                } else if (passByComp) {
                    notification += "Student's average in credits failed is sufficient enough to pass by compensation.";
                } else {
                    notification += "Student's average in credits failed is not sufficient to pass by compensation.";
                }
            }

        }

        studentInfo.addComponent(new Label(notification, ContentMode.HTML));
        studentInfo.addComponent(new Button("Download Report"));

        StreamResource reportResource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                File file = null;
                try {

                    //get student data from database
                    ResultSet results = Database.getStudentInfo(student);

                    try {
                        jOpenDocumentCreateTest j = new jOpenDocumentCreateTest();

                        // create spreadsheet report for module
                        file = j.createStudentReport(student, results);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return ModulesTabSheet.getInputStream(file);
            }
        }, "Student" + student + "Report.ods");
        FileDownloader reportDownloader = new FileDownloader(reportResource);
        reportDownloader.extend((AbstractComponent) studentInfo.getComponent(3));

    }

    /**
     * displays details of pending modules for approval
     */
    private void displayModulesContent() throws SQLException {

        clearExistingContent();

        final ResultSet moduleInfo = Database.getAllModuleStats();
        tabsheet = new ModulesTabSheet(moduleInfo);
        contentLayout.addComponent(tabsheet);

        root.addComponent(contentLayout);
    }

    /**
     * displays details of pending modules for approval
     */
    private void displayPendingModulesContent() throws SQLException {
        clearExistingContent();

        Label info = new Label("Please select the tabs below to view pending modules");
        info.setHeight("40px");
        contentLayout.addComponent(info);

        // accordian component for displaying pending modules
        Accordion accordion = new Accordion();
        accordion.setWidth("595px");
        contentLayout.addComponent(accordion);

        ResultSet pendingModules =null;
        try {
            pendingModules = Database.getPendingModules();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //if no pending modules, set up empty tab
        if (!pendingModules.isBeforeFirst() ) {

            accordion.addTab(new Label(""), "No pending modules");
        } else {

            while (pendingModules.next()) {

                // setup grid layout for each tab
                GridLayout grid = new GridLayout(2, 8);
                grid.setMargin(new MarginInfo(true, false, true, true));
                grid.addStyleName("grid");
                grid.setSpacing(true);
                grid.setWidth(null);

                final String code = pendingModules.getString("code");
                String name = pendingModules.getString("name");
                int credits = pendingModules.getInt("credit_weighting");
                int ca = pendingModules.getInt("ca_mark_percentage");
                int exam = pendingModules.getInt("final_exam_percentage");
                String lecturer = pendingModules.getString("lecturer");

                // populate tab with module info
                grid.addComponent(new Label("Code:"));
                grid.addComponent(new Label(code));
                grid.addComponent(new Label("Name:"));
                grid.addComponent(new Label(name));
                grid.addComponent(new Label("Credits:"));
                grid.addComponent(new Label(Integer.toString(credits)));
                grid.addComponent(new Label("C.A:"));
                grid.addComponent(new Label(Integer.toString(ca)));
                grid.addComponent(new Label("Final Exam:"));
                grid.addComponent(new Label(Integer.toString(exam)));
                grid.addComponent(new Label("Lecturer:"));
                grid.addComponent(new Label(lecturer));

                grid.getComponent(0,0).setWidth("100px");
                grid.getComponent(1,0).setWidth("400px");

                grid.addComponent(new Button("Approve", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        Notification.show("New module approved");

                        try {
                            Database.approveModule(code);
                            Notification.show("The module " + code + " has been approved and added to the system");
                            displayPendingModulesContent();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }), 1,6);

                grid.addComponent(new Button("Reject", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {

                        try {
                            Database.deleteModule(code);
                            Notification.show("The module " + code + " has been rejected and deleted from the system");
                            displayPendingModulesContent();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    }
                }), 1, 7);

                grid.getComponent(1,7).setWidth("97px");
                grid.getComponent(1,6).addStyleName("buttons");
                grid.getComponent(1,7).addStyleName("buttons");

                accordion.addTab(grid, code);
            }

            Label l = new Label("");
            l.setHeight("100px");
            contentLayout.addComponent(l);

        }

        root.addComponent(contentLayout);
    }

    /**
     * displays form for adding new user to system
     */
    private void displayAddUserContent() {
        clearExistingContent();

        // form layout
        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        contentLayout.addComponent(form);

        // arraylist for storing text and password fields
        final ArrayList<AbstractTextField> fields = new ArrayList();

        final TextField forename = new TextField("First Name");
        forename.addValueChangeListener(new FieldChangeListener(forename));
        form.addComponent(forename);
        fields.add(forename);

        final TextField surname = new TextField("Surname");
        surname.addValueChangeListener(new FieldChangeListener(surname));
        form.addComponent(surname);
        fields.add(surname);

        final TextField username = new TextField("Username");
        username.addValueChangeListener(new FieldChangeListener(username));
        form.addComponent(username);
        fields.add(username);

        final TextField usernameAgain = new TextField("Username (again)");
        usernameAgain.addValueChangeListener(new FieldChangeListener(usernameAgain));
        form.addComponent(usernameAgain);
        fields.add(usernameAgain);

        final PasswordField password = new PasswordField("Password");
        password.addValueChangeListener(new FieldChangeListener(password));
        form.addComponent(password);
        fields.add(password);

        final PasswordField passwordAgain = new PasswordField("Password (again)");
        passwordAgain.addValueChangeListener(new FieldChangeListener(passwordAgain));
        form.addComponent(passwordAgain);
        fields.add(passwordAgain);

        //add buttons for submitting and cancelling
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button submit = new Button("Submit", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                // check all fields are valid. If not, highlight red
                boolean allFieldsValid = true;

                for (AbstractTextField field : fields) {

                    if ( field.getValue().trim().isEmpty() ) {
                        field.addStyleName("emptyField");
                        allFieldsValid = false;
                    }
                }

                if (! allFieldsValid) {
                    new Notification("All fields must be completed correctly - please fill highlighted fields",
                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                } else {
                    String usernameTrimmed = username.getValue().trim();
                    String passwordTrimmed = password.getValue().trim();

                    if (! usernameTrimmed.equals(usernameAgain.getValue().trim())) {
                        username.setValue("");
                        username.addStyleName("emptyField");
                        usernameAgain.setValue("");
                        usernameAgain.addStyleName("emptyField");
                        new Notification("Username fields must be the same",
                                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

                    } else if (! passwordTrimmed.equals(passwordAgain.getValue().trim())) {
                        password.setValue("");
                        password.addStyleName("emptyField");
                        passwordAgain.setValue("");
                        passwordAgain.addStyleName("emptyField");
                        new Notification("Password fields must be the same",
                                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

                    } else if (passwordTrimmed.length() < 8) {
                        new Notification("Password must be at least 8 characters - please choose a valid password",
                                Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

                    } else {

                        try {
                            if (Database.checkUserExists(usernameTrimmed)) {
                                new Notification("The username entered already exists - please reselect a username",
                                        Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());

                            } else {
                                if (Database.createUser(usernameTrimmed, forename.getValue().trim(),
                                        surname.getValue().trim(), passwordTrimmed)) {

                                    //clear all fields
                                    for (AbstractTextField field : fields) {
                                        field.setValue("");
                                    }
                                    Notification.show("New user successfully added");

                                } else {
                                    new Notification("Unable to create new user - please try again later",
                                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        Button clear = new Button("Clear", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                //clear all fields
                for (AbstractTextField tx : fields) {
                    tx.setValue("");
                    tx.removeStyleName("emptyField");
                }
            }
        });

        submit.addStyleName("buttons");
        clear.addStyleName("buttons");
        buttons.addComponent(submit);
        buttons.addComponent(clear);
        form.addComponent(buttons);

        root.addComponent(contentLayout);
    }

    private void clearExistingContent() {
        root.removeComponent(contentLayout);
        contentLayout.removeAllComponents();
    }

} // end of class