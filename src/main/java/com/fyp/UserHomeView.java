package com.fyp;

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
import java.util.ArrayList;

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
    private ArrayList<String> checked;
    private Tree.ItemStyleGenerator itemStyleGenerator;
    private Table table;

    private Button popUp;
    private Button submitButton;
    private Window fileBrowserWindow;
    private VerticalLayout fileBrowserContent;
    private Button selectFile;
    private String fileToArchive;
    private String budgetNumber;
    private Boolean deleteAfterArchiving;
    private Window confirmationWindow;
    private VerticalLayout confirmationContent;

    private Button downloadTemplate;
    private Button uploadTemplate;
    private Button getReport;

    protected String currentTab;

    protected static final String USER_HOME = "userHome";
    private StreamResource myResource;
    private FileDownloader fileDownloader;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        session = getSession();

        try {
            initLayout();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        initButtons();
        //initTextListener();
    }

    /**
     * Sets up content for the UI
     */
    private void initLayout() throws SQLException {

        // root content
        root = new VerticalLayout();
        root.addStyleName("mainContent");
        root.setSizeFull();
        addComponent(root);
        //this.setHeight("200px");
        this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
        Page.getCurrent().setTitle("User Home");

        HorizontalLayout allHeaderInfo = new HorizontalLayout();
        allHeaderInfo.setSizeFull();
        //root.addComponent(allHeaderInfo);
        //allHeaderInfo.setMargin(new MarginInfo(true, true, false, false));

        Label testLabel = new Label("THIS IS HEADING");
        testLabel.setWidth(null);
        ///allHeaderInfo.addComponent(testLabel);
        //allHeaderInfo.setComponentAlignment(testLabel, Alignment.TOP_LEFT);

        // session information
        sessionInfo = new HorizontalLayout();
        sessionInfo.setSpacing(true);
        sessionInfo.setHeight("120px");
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);
        sessionInfo.setWidth(null);
        sessionInfo.setMargin(new MarginInfo(true, true, false, false));
        root.addComponent(sessionInfo);
        //allHeaderInfo.addComponent(sessionInfo);
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

        // stores which node is selected
        checked = new ArrayList<String>(1);

        Label newHeading = new Label("Welcome back, " + UserLogin.USER_FORENAME );
        newHeading.addStyleName("heading");
        newHeading.setWidth("100%");
        root.addComponent(newHeading);

        Label instructions = new Label("Please click on the tabs below to explore your modules.<br>" +
                "Alternatively, you can add new module information by clicking below.<br>" +
                "Please note: All newly added modules must be first approved by the administrators before they can be used" , ContentMode.HTML);

        instructions.addStyleName("instructions");
        instructions.setHeight("120px");
        //instructions.setWidth("100%");
        root.addComponent(instructions);
        //root.setComponentAlignment(newHeading, Alignment.TOP_CENTER);

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.addStyleName("hLayout");
        hLayout.setImmediate(false);
        hLayout.setWidth("-1px");
        hLayout.setHeight("-1px");
        hLayout.setMargin(false);
        hLayout.setSpacing(true);
        root.addComponent(hLayout);

        /*Database.setupConnection();

        String sql = "SELECT code FROM modules WHERE accountID = 2";
        ResultSet list = Database.getList(sql);
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("Modules", String.class, null);

        while (list.next()) {
            String item = list.getString(1);
            container.addItem(item);
            container.getContainerProperty(item, "Modules").setValue(item);
        }*/

        final TabSheet tabsheet = new TabSheet();
        hLayout.addComponent(tabsheet);
        final ResultSet moduleInfo = Database.getTabInfo(UserLogin.USER_ACC_NUM);

        while (moduleInfo.next()) {
            final String code = moduleInfo.getString("code");

            // setup grid layout for each tab
            GridLayout grid = new GridLayout(2, 6);
            grid.setMargin(new MarginInfo(true,false,false,false));
            grid.addStyleName("grid");
            grid.setSpacing(true);
            grid.setWidth(null);

            if ( !moduleInfo.getBoolean("approved") ) {
                //module not approved yet, so display warning message
                grid.addComponent(new Label("This module has not yet been approved by the administrator", ContentMode.TEXT));

            } else {

                // populate tab with module info
                grid.addComponent(new Label("Module:"));
                grid.addComponent(new Label(moduleInfo.getString("name")));
                grid.addComponent(new Label("Credits:"));
                grid.addComponent(new Label(moduleInfo.getString("credit_weighting")));
                grid.addComponent(new Label("C.A:"));
                grid.addComponent(new Label(moduleInfo.getString("ca_mark_percentage") + "%"));
                grid.addComponent(new Label("Final Exam:"));
                grid.addComponent(new Label(moduleInfo.getString("final_exam_percentage") + "%"));
                grid.addComponent(new Label("Results Submitted:"));
                grid.addComponent(new Label(moduleInfo.getString("num_results")));

                /*//add buttons with their respective click listeners
                grid.addComponent(new Button("Download Template", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        System.out.println("Download Template button clicked for " + code);

                        displaySummaryWindow(code);

                    }
                }), 1, 7, 1, 7);

                grid.addComponent(new Button("Upload Results", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        System.out.println("Upload Results button clicked for " + code);
                        //method for uploading results here
                    }
                }), 1, 8, 1, 8);

                grid.addComponent(new Button("Get Report", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        System.out.println("Get Report button clicked for " + code);
                        //method for generating report here
                    }
                }), 1,9,1,9);*/

                //grid.addComponent(heading, 0, 0, 1, 0);
                //new Label("Module: " + module + ", Credits: " + credits + ", CA: " + ca + ", Exam: " +exam, ContentMode.TEXT));
            }
            grid.setCaption(code);
            tabsheet.addTab(grid);
            //tabsheet.addTab(grid,  code);
        }

        currentTab = tabsheet.getTab(0).getCaption();

        tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                currentTab = event.getTabSheet().getSelectedTab().getCaption();
                System.out.println("currentTab updated");
                System.out.println(event.getTabSheet().getSelectedTab().getCaption());
            }
        });

        downloadTemplate = new Button("Download Template");
        uploadTemplate = new Button("Upload Results");
        getReport = new Button("Get Report");

        myResource = createResource();
        fileDownloader = new FileDownloader(myResource);
        fileDownloader.extend(downloadTemplate);

        root.addComponent(downloadTemplate);
        root.addComponent(uploadTemplate);
        root.addComponent(getReport);

        downloadTemplate.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                System.out.println(tabsheet.getSelectedTab().getCaption());
                //createResource("test");
            }
        });



/*
        confirmationButtons.addComponent(confirmButton);

        confirmButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {


                createResource(nameTextField.getValue());
                summaryWindow.setVisible(false);
                Notification.show("Please wait...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                summaryWindow.close();
            }
        });*/

        table = new Table("");

        /*

        //table.setContainerDataSource(container);
        table.setSelectable(true);
        table.setImmediate(true);
        table.setWidth("-1px");
        table.setPageLength(0);
*/
        //hLayout.addComponent(table);

        //userContent.setComponentAlignment(table, Alignment.TOP_LEFT);
        //hLayout.setComponentAlignment(table, Alignment.MIDDLE_LEFT);

        /*Panel panel = new Panel("Panel");
        panel.addStyleName("panel");
        panel.setImmediate(true);
        panel.setWidth("400px");
        panel.setHeight("300px");
        //hLayout.addComponent(panel);

        VerticalLayout v = new VerticalLayout();

        panel.setContent(new Label("Test"));*/
    }

    private StreamResource createResource() {
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                byte[] b;
                try {

                    //create spreadsheet document
                    jOpenDocumentCreateTest c = new jOpenDocumentCreateTest();
                    File file = c.createFile();

                    b = new byte[(int) file.length()];

                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileInputStream.read(b);

                    } catch (FileNotFoundException e) {
                        System.out.println("File Not Found.");
                        e.printStackTrace();
                        return null;
                    }
                    catch (IOException e) {
                        System.out.println("Error Reading The File.");
                        e.printStackTrace();
                        return null;
                    }

                    return new ByteArrayInputStream(b);
            }
        }, currentTab + ".ods");
    }

    /**
     * Set up the File browser pop-up and relevant listeners
     */
    private void initFileBrowserPopUpButton() {

        // end the session and redirect the user to login page
        popUp.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                initFileBrowserPopUp();

                // resize tree according to current window size
                fileBrowserWindow.addResizeListener(new Window.ResizeListener() {
                    @Override
                    public void windowResized(Window.ResizeEvent e) {


                    }
                });

                // close pop-up and get the name of selected file/folder
                selectFile.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {

                        // close pop up window
                        fileBrowserWindow.close();

                        fileToArchive = "No File Selected";
                        // if file/folder selected, get its name
                        if (!checked.isEmpty()) {
                            String selected = checked.get(0);

                            // only display relative file path for user
                            fileToArchive = selected.replace("", "");
                        }

                        // update the button with the selected file/folder name
                        popUp.setCaption(fileToArchive);
                    }
                });
            }
        });
    }

    /**
     * Initialise the buttons for the main page
     */
    private void initButtons() {

        //initFileBrowserPopUpButton();
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

        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                //deleteAfterArchiving = deleteAfterCheckbox.getValue();
                //budgetNumber = budgetNumTextField.getValue().trim();

                if (checked.isEmpty()) {
                    Notification.show("No file selected - Please select a file to archive");

                } else if (budgetNumber.isEmpty()) {
                    //budgetNumTextField.addStyleName("emptyField");
                    Notification.show("Please provide a valid budget number");

                } else  {
                    //budgetNumTextField.removeStyleName("emptyField");
                    displaySummaryWindow("");
                }
            }
        });
    }

    /**
     * Display summary window for user, containing details of their selection
     * Give submit or cancel options
     */
    private void displaySummaryWindow(final String code) {

        // Create a sub-window and set the content
        final Window summaryWindow = new Window("Please enter a name for your template file");
        VerticalLayout summaryContent = new VerticalLayout();
        summaryContent.setMargin(true);
        summaryContent.setSpacing(true);
        summaryWindow.setWidth("390px");
        summaryWindow.setHeight("-1px");

        summaryWindow.setModal(true);
        summaryWindow.setClosable(false);
        summaryWindow.setResizable(false);
        summaryWindow.setContent(summaryContent);
        summaryWindow.center();
        summaryWindow.setImmediate(true);

        final TextField nameTextField = new TextField();
        nameTextField.setInputPrompt("Filename");
        nameTextField.setWidth("209px");
        summaryContent.addComponent(nameTextField);

        final HorizontalLayout confirmationButtons = new HorizontalLayout();
        confirmationButtons.setSpacing(true);
        confirmationButtons.setWidth(null);
        summaryContent.addComponent(confirmationButtons);
        //summaryContent.setComponentAlignment(confirmationButtons, Alignment.BOTTOM_CENTER);

        // confirmation button
        Button confirmButton = new Button("Download");
        confirmButton.addStyleName("buttons");

        confirmationButtons.addComponent(confirmButton);

        confirmButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                summaryWindow.close();
            }
        });

        // cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.addStyleName("buttons");
        confirmationButtons.addComponent(cancelButton);
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
     * Pop-up to be displayed when user submits archive request
     */
    private void initConfirmationPopUp() {

        // Create a sub-window and set the content
        confirmationWindow = new Window("Request submitted");
        confirmationContent = new VerticalLayout();
        confirmationContent.setMargin(true);
        confirmationContent.setSpacing(true);
        confirmationWindow.setWidth("630px");
        confirmationWindow.setHeight("160px");
        confirmationWindow.setModal(true);
        confirmationWindow.setClosable(false);
        confirmationWindow.setResizable(false);
        confirmationWindow.setContent(confirmationContent);
        confirmationWindow.center();
        confirmationWindow.setImmediate(true);

        // display whether file will be kept/deleted after archiving
        String notification = "Your request has been sent to the IT Department who will notify you when archiving has been completed.<br>" +
                              "You have requested for your selected file to be "+ (deleteAfterArchiving ? "deleted" : "kept") + " after archiving";
        Label fileStatusText = new Label(notification, ContentMode.HTML);
        confirmationContent.addComponent(fileStatusText);

        Button popUpLogoutButton = new Button("Logout");
        popUpLogoutButton.addStyleName("buttons");
        confirmationContent.addComponent(popUpLogoutButton);
        confirmationContent.setComponentAlignment(popUpLogoutButton, Alignment.BOTTOM_CENTER);

        // add the listener for logging out
        popUpLogoutButton.addClickListener(new LogoutListener(session, getUI()));

        // Open pop-up in the UI
        getUI().addWindow(confirmationWindow);
    }

}