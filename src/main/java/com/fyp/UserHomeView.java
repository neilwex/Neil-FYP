package com.fyp;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Neil on 25/01/2015.
 */
@SuppressWarnings("serial")
public class UserHomeView extends VerticalLayout implements View {

    private VaadinSession session;
    private VerticalLayout root;
    private HorizontalLayout sessionInfo;
    private Label userDetails;
    private Button logoutButton;

    private Window fileBrowserWindow;
    private VerticalLayout fileBrowserContent;
    private Button selectFile;
    protected Window addNewModuleWindow;

    private TabSheet tabsheet;
    protected String currentTab;
    private HorizontalLayout buttons;

    protected static final String USER_HOME = "userHome";
    private StreamResource templateResource;
    private StreamResource reportResource;
    private FileDownloader templateDownloader;
    private FileDownloader reportDownloader;

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
        sessionInfo = new HorizontalLayout();
        sessionInfo.setSpacing(true);
        sessionInfo.setHeight("120px");
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);
        sessionInfo.setWidth(null);
        sessionInfo.setMargin(new MarginInfo(true, true, false, false));
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);

        // display session information and logout button
        userDetails = new Label("Logged in: " + UserLogin.USER_NAME);
        sessionInfo.addComponent(userDetails);

        // logout button
        logoutButton = new Button("Logout");
        //logoutButton.addStyleName(BaseTheme.BUTTON_LINK);
        logoutButton.addStyleName("buttons");
        //logoutButton.setSizeUndefined();
        sessionInfo.addComponent(logoutButton);
        logoutButton.addClickListener(new LogoutListener(session, getUI()));

        Label newHeading = new Label("Welcome back, " + UserLogin.USER_FORENAME );
        newHeading.addStyleName("heading");
        newHeading.setWidth("100%");
        root.addComponent(newHeading);

        Label instructions = new Label("Please click on the tabs below to explore your modules.<br>" +
                "Alternatively, you can add new module information by clicking below.<br>" +
                "Please note: All newly added modules must be first approved by the administrators before they can be used" , ContentMode.HTML);

        instructions.addStyleName("instructions");
        instructions.setHeight("100px");
        root.addComponent(instructions);

        Button addModule = new Button("Add Module", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                displayAddModuleWindow();
            }
        });
        root.addComponent(addModule);

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addStyleName("hLayout");
        hLayout.setImmediate(false);
        hLayout.setWidth("-1px");
        hLayout.setHeight("-1px");
        hLayout.setMargin(false);
        hLayout.setSpacing(true);
        root.addComponent(hLayout);

        tabsheet = new TabSheet();
        hLayout.addComponent(tabsheet);
        final ResultSet moduleInfo = Database.getModuleStats(UserLogin.USER_ACC_NUM);

        while (moduleInfo.next()) {
            final String code = moduleInfo.getString("code");

            // setup grid layout for each tab
            GridLayout grid = new GridLayout(2, 8);
            grid.setMargin(new MarginInfo(true,false,false,false));
            grid.addStyleName("grid");
            grid.setSpacing(true);
            grid.setWidth(null);

            if ( !moduleInfo.getBoolean("approved") ) {
                //module not approved yet, so display warning message
                grid.addComponent(new Label("This module has not yet been approved by the administrator", ContentMode.TEXT));

            } else {

                int credits = moduleInfo.getInt("credit_weighting");
                int ca = moduleInfo.getInt("ca_mark_percentage");
                int exam = moduleInfo.getInt("final_exam_percentage");

                // populate tab with module info
                grid.addComponent(new Label("Module:"));
                grid.addComponent(new Label(moduleInfo.getString("name")));
                grid.addComponent(new Label("Credits:"));
                grid.addComponent(new Label(Integer.toString(credits)));
                grid.addComponent(new Label("C.A:"));
                grid.addComponent(new Label(ca + "%"));
                grid.addComponent(new Label("Final Exam:"));
                grid.addComponent(new Label(exam + "%"));
                grid.addComponent(new Label("Results Submitted:"));
                grid.addComponent(new Label(moduleInfo.getString("num_results")));

                grid.addComponent(new Button("Download Template"),0,5);
                templateResource = createResource(code, credits, ca, exam);
                templateDownloader = new FileDownloader(templateResource);
                templateDownloader.extend((AbstractComponent) grid.getComponent(0, 5));

                grid.addComponent(new Button("Upload Results", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        displayUploadWindow(code);
                    }
                }),0,6);

                grid.addComponent(new Button("Get Report"),0,7);
                reportResource = createResource(code);
                reportDownloader = new FileDownloader(reportResource);
                reportDownloader.extend((AbstractComponent) grid.getComponent(0, 7));
            }

            grid.setCaption(code);
            tabsheet.addTab(grid);
        }

        currentTab = tabsheet.getTab(0).getCaption();

        buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                currentTab = event.getTabSheet().getSelectedTab().getCaption();
                System.out.println("currentTab updated to: " + currentTab);
            }
        });

        currentTab = tabsheet.getTab(0).getCaption();
    }

    private StreamResource createResource(final String code) {
        String filename = code + "Report.ods";
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                File file = null;
                try {

                    //get module data from database
                    int num_students = Database.getNumStudents(code);
                    int modulesCredits = Database.getCredits(code);
                    ResultSet resultsData = Database.getModuleResults(code);
                    ResultSet moduleAverages = Database.getModuleAverages(code);

                    try {
                        jOpenDocumentCreateTest c = new jOpenDocumentCreateTest();

                        // create spreadsheet report for module
                        file = c.createReport(num_students, modulesCredits, resultsData, moduleAverages);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return getInputStream(file);
            }
        }, filename);
    }

    private StreamResource createResource(final String code, final int credits, final int ca, final int exam ) {
        String filename = code + "Template.csv";
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                File file = null;

                try {
                    file = CsvFile.createCsvFile(credits, ca, exam);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return getInputStream(file);
            }
        }, filename);
    }

    private InputStream getInputStream(File file) {
        byte[] b;

        try {
            b = new byte[(int) file.length()];

            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);

        } catch (FileNotFoundException e) {
                System.out.println("File Not Found.");
                e.printStackTrace();
                return null;
        } catch (IOException e) {
                System.out.println("Error Reading The File.");
                e.printStackTrace();
                return null;
        }

        return new ByteArrayInputStream(b);
    }


    /**
     * Initialise the buttons for the main page
     */
    private void initButtons() {

        //initSubmitButton();
    }

    /**
     * Set up the pop-up for the file browser
     */
    private void initFileBrowserPopUp() {

        // Create a sub-window and set the content
        fileBrowserWindow = new Window("Select File/Folder to archive");
        fileBrowserContent = new VerticalLayout();
        fileBrowserContent.setMargin(true);
        fileBrowserContent.setSpacing(true);
        fileBrowserWindow.setWidth("650px");
        fileBrowserWindow.setHeight("600px");
        fileBrowserWindow.setModal(true);
        fileBrowserWindow.setContent(fileBrowserContent);
        fileBrowserWindow.center();
        fileBrowserWindow.setImmediate(true);

        //Submit button
        selectFile = new Button("Select");
        selectFile.addStyleName("buttons");
        fileBrowserContent.addComponent(selectFile);

        // Open pop-up in the UI
        getUI().addWindow(fileBrowserWindow);
    }

    /**
     * Set the listener for submit button
     */
    private void initSubmitButton() {

    }

    /**
     * Display an upload window for user, allowing them to select a file for upload
     */
    private void displayUploadWindow(final String code) {

        // Create a sub-window and set the content
        final Window summaryWindow = new Window("Upload Results");
        VerticalLayout summaryContent = new VerticalLayout();
        summaryContent.setMargin(true);
        summaryContent.setSpacing(true);
        summaryWindow.setWidth("-1px");
        summaryWindow.setHeight("-1px");

        summaryWindow.setModal(true);
        summaryWindow.setClosable(false);
        summaryWindow.setResizable(false);
        summaryWindow.setContent(summaryContent);
        summaryWindow.center();
        summaryWindow.setImmediate(true);

        // add uploader
        CsvUploader receiver = new CsvUploader();
        Upload upload = new Upload("This is caption", receiver);
        upload.addSucceededListener(receiver);
        summaryContent.addComponent(upload);

        // cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.addStyleName("buttons");
        summaryContent.addComponent(cancelButton);
        summaryContent.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                summaryWindow.close();
            }
        });

        // Open pop-up in the UI
        getUI().addWindow(summaryWindow);
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
        addNewModuleWindow.setClosable(false);
        addNewModuleWindow.setResizable(false);
        addNewModuleWindow.center();
        addNewModuleWindow.setImmediate(true);

        /// Create form for user data
        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        addNewModuleWindow.setContent(form);

        final TextField moduleCode = new TextField("Module Code");
        form.addComponent(moduleCode);
        final TextField moduleTitle = new TextField("Module Title");
        form.addComponent(moduleTitle);

        ComboBox creditCombo = new ComboBox("Credit Weighting");
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

                try {
                    int newValue;

                    if (ca.getValue() == null || ca.getValue().trim().isEmpty() ) {
                        newValue = 0 ;
                    } else {
                        newValue = (Integer) ca.getConvertedValue();
                    }
                    int examValue = 100 - newValue;
                    if (examValue > 100 || examValue < 0) {
                        ca.addStyleName("emptyField");
                        Notification.show("Invalid range: Please enter a number 0-100 ");
                    } else {
                        ca.removeStyleName("emptyField");
                        exam.setValue(Integer.toString(examValue));
                    }
                } catch (Converter.ConversionException e) {
                    Notification.show("Invalid format: Please enter a number 0-100 ");
                }
            }
        });

        //listener for updating ca figure when exam is changed
        exam.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                try {

                    int newValue;
                    if (exam.getValue() == null || exam.getValue().trim().isEmpty() ) {
                        newValue = 0 ;
                    } else {
                        newValue = (Integer) exam.getConvertedValue();
                    }

                    int caValue = 100 - newValue;
                    if (caValue > 100 || caValue < 0) {
                        exam.addStyleName("emptyField");
                        Notification.show("Invalid range: Please enter a number 0-100 ");
                    } else {
                        exam.removeStyleName("emptyField");
                        ca.setValue(Integer.toString(caValue));
                    }
                } catch (Converter.ConversionException e) {
                    Notification.show("Invalid format: Please enter a number 0-100 ");
                }
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button submit = new Button("Submit", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                String trimmedModuleCode = moduleCode.getValue().trim();
                String trimmedModuleTitle = moduleTitle.getValue().trim();

                if (trimmedModuleCode.isEmpty() || trimmedModuleTitle.isEmpty()) {
                    Notification.show("Form incomplete - please fill all fields");

                    // NOW NEED TO CHECK EXAM AND CA INPUT - must be int in correct range
                } else {
                    ///displayUploadWindow("");
                }
            }
        });

        Button cancel = new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                addNewModuleWindow.close();
            }
        });

        buttons.addComponent(submit);
        buttons.addComponent(cancel);
        form.addComponent(buttons);

        // display whether file will be kept/deleted after archiving
        String notification = "Your request has been sent to the IT Department who will notify you when archiving has been completed.<br>" +
                              "You have requested for your selected file to be ... after archiving";
        Label fileStatusText = new Label(notification, ContentMode.HTML);

        // Open pop-up in the UI
        getUI().addWindow(addNewModuleWindow);
    }

    protected class CsvUploader implements Upload.Receiver, Upload.SucceededListener {

        public File file;

        public OutputStream receiveUpload(String filename,
                                          String mimeType) {
            // Create upload stream
            FileOutputStream fos = null; // Stream to write to
            try {

                // Open the file for writing.
                file = new File("files\\" + filename);
                fos = new FileOutputStream(file);
            } catch (final java.io.FileNotFoundException e) {
                new Notification("Could not open file<br/>",
                        e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                return null;
            }

            return fos; // Return the output stream to write to
        }

        public void uploadSucceeded(Upload.SucceededEvent event) {

            //check file format of uploaded document
            boolean isFileCorrectFormat = CsvFile.isValidCsvFile(file, currentTab);
            System.out.println("isFileCorrectFormat? " + isFileCorrectFormat);

            if (isFileCorrectFormat) {
                boolean readCsvFileSuccessfully = Database.readCsvFile(file.getAbsolutePath(), currentTab);

                if (readCsvFileSuccessfully) {

                    Notification.show("Selected file contents successfully added to the database");
                } else {
                    //Notification.show("Selected file is not in correct format - please select a valid file");
                    new Notification("Unable to read file contents - please select a valid file",
                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                }

            } else {
                new Notification("Selected file is not in correct format - please select a valid file",
                        Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
                //Notification.show("Selected file is not in correct format - please select a valid file");
            }

        }
    };

}