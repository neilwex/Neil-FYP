package com.fyp;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.tepi.filtertable.FilterTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by Neil on 22/01/2015.
 */
@SuppressWarnings("serial")
public class RetrievingBrowser extends VerticalLayout implements View {

    protected static final String RETRIEVAL_BROWSER = "retrievalBrowser";
    protected static final String FILENAME = "Filename";
    protected static final String DATE = "Date";

    private VaadinSession session;
    private VerticalLayout root;
    private HorizontalLayout sessionInfo;
    private String userGroup;
    private String currentUser;
    private String userEmail;
    private Label userDetails;
    private Button logoutButton;

    private VerticalLayout retrievalContent;
    private Button submitButton;
    private Button clearButton;
    private FilterTable table;
    private Set rowId;
    private String[] resultArray;
    private String selectedFiles;
    private Label currentlySelected;
    private String filesToBeRetrieved;
    private Container container;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        session = getSession();
        initLayout();
        initButtons();
        initTableListener();
    }

    /**
     * Sets up content for the UI
     */
    private void initLayout() {

        // root content
        root = new VerticalLayout();
        root.addStyleName("mainContent");
        root.setSizeFull();
        addComponent(root);
        this.setHeight("200px");
        this.setComponentAlignment(root, Alignment.MIDDLE_CENTER);
        Page.getCurrent().setTitle("Retrieve files");

        // session information
        sessionInfo = new HorizontalLayout();
        sessionInfo.setSpacing(true);
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);
        sessionInfo.setWidth(null);
        sessionInfo.setMargin(new MarginInfo(true, true, false, false));

        // get session info
        currentUser = session.getAttribute("user").toString();
        userGroup = session.getAttribute("group").toString();
        userEmail = session.getAttribute("email").toString();

        // display session information and logout button
        userDetails = new Label("Logged in: " + currentUser + " (" + userGroup + ")");
        sessionInfo.addComponent(userDetails);

        // logout button
        logoutButton = new Button("Logout");
        logoutButton.addStyleName(BaseTheme.BUTTON_LINK);
        logoutButton.addStyleName("buttons");
        sessionInfo.addComponent(logoutButton);
        logoutButton.addClickListener( new LogoutListener(session, getUI()) );

        retrievalContent = new VerticalLayout();
        retrievalContent.setSpacing(true);
        retrievalContent.addStyleName("retrievalContent");
        root.addComponent(retrievalContent);

        Label instructions = new Label("Below is a listing of all files archived for your group (" + userGroup + ")<br>" +
                "Please select the files you wish to retrieve. You may select multiple files." , ContentMode.HTML);
        retrievalContent.addComponent(instructions);

        createContainer();
        createAndFillTable();

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        retrievalContent.addComponent(buttons);

        submitButton = new Button("Request Retrieval");
        submitButton.addStyleName("retrievalButtons");
        submitButton.addStyleName("buttons");
        buttons.addComponent(submitButton);

        clearButton = new Button("Clear all");
        clearButton.addStyleName("retrievalButtons");
        clearButton.addStyleName("buttons");
        buttons.addComponent(clearButton);

        HorizontalLayout selectFiles = new HorizontalLayout();
        selectFiles.setSpacing(true);
        retrievalContent.addComponent(selectFiles);

        // Shows feedback from selection.
        final Label selected = new Label("Selected:");
        selectFiles.addComponent(selected);
        currentlySelected = new Label("No files selected", ContentMode.HTML);
        selectFiles.addComponent(currentlySelected);

    }

    /**
     * Create an indexed container and fill it with columns for the table
     */
    private void createContainer() {
        // Create a container
        container = new IndexedContainer();

        // Define the names and data types of columns
        container.addContainerProperty(DATE, Date.class, null);
        container.addContainerProperty(FILENAME, String.class, null);
    }

    /**
     * Create the table, add the container to it and populate
     */
    private void createAndFillTable() {

        table = new FilterTable();
        table.setContainerDataSource(container);
        table.setWidth("100%");
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setImmediate(true);
        table.setMultiSelectMode(MultiSelectMode.SIMPLE);
        table.setFilterBarVisible(true);
        table.setColumnWidth(DATE, 240);

        fillTable();

        retrievalContent.addComponent(table);
        retrievalContent.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
    }

    /**
     * Executes SSH command for retrieving all previously archived files for a group
     * Gets the filename and date from this info and adds it to the container
     */
    private void fillTable() {

        // execute SSH command for retrieving all previously archived files
        final String ssh = "/usr/bin/ssh";
        final String host = "tsm";
        final String cmd = "dsmadmc -se=a1 -id=archgui -password=archgui -dataonly=YES -comma " +
                            "\"select * from archives where FILESPACE_NAME like '%" + userGroup + "%'\"";
        String[] retrieveCommand = {ssh, host, cmd};

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(retrieveCommand);
            //proc.waitFor();

        } catch (IOException e) {

            System.out.println("Problem with executing SSH command");
            e.printStackTrace();
        } /*catch (InterruptedException e) {

            e.printStackTrace();
        }*/

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        //System.out.println("\nHere is the standard output of the command (if any):\n");
        String s;
        try {
            while ((s = stdInput.readLine()) != null) {
                if ( s.contains("FILE")) {
                    //System.out.println(s);
                    String[] parts = s.split(",");

                    if (parts.length == 11) { // correct number of arguments

                        String fileName = parts[1] + parts[4] + parts[5];
                        //System.out.println("Filename: " + fileName);

                        String dateString = parts[7];
                        //System.out.println("Date String: " + dateString);

                        // convert date string to date object
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // add details as a row to table
                        container.addItem(fileName);
                        container.getContainerProperty(fileName, DATE).setValue(date);
                        container.getContainerProperty(fileName, FILENAME).setValue(fileName);
                    }
                }

            }
            System.out.println("Table populated");

        } catch (IOException e) {
            System.out.println("Problem with output loop");
            e.printStackTrace();
        }

        // read any errors from the attempted command
        //System.out.println("Here is the standard error of the command (if any):\n");
        try {
            while ((s = stdError.readLine()) != null) {
               //System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println("Problem with error loop");
            e.printStackTrace();
        }

    }

    /**
     * Initialises the buttons' listeners for the page
     */
    private void initButtons() {

        initSubmitButton();
    }

    /**
     * Listener for the submit button
     */
    private void initSubmitButton() {

        submitButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                // if no files selected, notify user
                if (resultArray == null) {
                    Notification.show("No file selected - Please select a file to retrieve");
                } else if (resultArray.length == 0) {
                    Notification.show("No file selected - Please select a file to retrieve");
                } else {
                    // valid selection made
                    displaySummaryWindow();
                }
            }
        });
    }

    /**
     * Display summary window for user, containing details of their selection
     * Give submit or cancel options
     */
    private void displaySummaryWindow() {

        // Create a sub-window and set the content
        final Window summaryWindow = new Window("Confirm");
        VerticalLayout summaryContent = new VerticalLayout();
        summaryContent.setMargin(true);
        summaryContent.setSpacing(true);
        summaryWindow.setWidth("650px");
        int height = 160 + (18 * (resultArray.length - 1)); //dynamic size for pop-up window
        summaryWindow.setHeight(height + "px");
        summaryWindow.setModal(true);
        summaryWindow.setClosable(false);
        summaryWindow.setResizable(false);
        summaryWindow.setContent(summaryContent);
        summaryWindow.center();
        summaryWindow.setImmediate(true);

        Label summaryInfo = new Label("You have requested retrieval of the following files:");
        summaryContent.addComponent(summaryInfo);

        //display list of selected files
        Label selectedFilesLabel = new Label(selectedFiles, ContentMode.HTML);
        selectedFilesLabel.setWidth(null);
        summaryContent.addComponent(selectedFilesLabel);
        summaryContent.setComponentAlignment(selectedFilesLabel, Alignment.MIDDLE_CENTER);

        final HorizontalLayout confirmationButtons = new HorizontalLayout();
        confirmationButtons.setSpacing(true);
        confirmationButtons.setWidth(null);
        summaryContent.addComponent(confirmationButtons);
        summaryContent.setComponentAlignment(confirmationButtons, Alignment.BOTTOM_CENTER);

        // confirmation button
        Button confirmButton = new Button("Confirm");
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
     * Table listener that keeps record of currently selected rows in table
     * Updates label below table with currently selected files
     */
    private void initTableListener() {

        // Handle selection change for tree
        table.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                // get selected files from table
                rowId = (Set<String>) table.getValue(); // get the selected rows' ids
                resultArray = new String[rowId.size()];
                rowId.toArray(resultArray);
                selectedFiles = "";
                filesToBeRetrieved = "";

                if (resultArray.length > 0) {
                    for (String s : resultArray) {
                        selectedFiles += s + "<br>";
                        filesToBeRetrieved += s + "\n";
                    }
                } else {
                    selectedFiles = "No files selected";
                }

                //update label
                currentlySelected.setValue(selectedFiles);
            }
        });
    }

    /**
     * Window displayed after retrieve request submitted
     * Informs user of their request and allows logout
     */
    private void initConfirmationPopUp() {

        // Create a sub-window and set the content
        Window confirmationWindow = new Window("Request submitted");
        VerticalLayout confirmationContent = new VerticalLayout();
        confirmationContent.setMargin(true);
        confirmationContent.setSpacing(true);
        confirmationWindow.setWidth("610px");
        confirmationWindow.setHeight("130px");
        confirmationWindow.setModal(true);
        confirmationWindow.setClosable(false);
        confirmationWindow.setResizable(false);
        confirmationWindow.setContent(confirmationContent);
        confirmationWindow.center();
        confirmationWindow.setImmediate(true);

        Label confirmationText = new Label("Your request has been sent to the IT Department who will notify you when retrieval has been completed.");
        confirmationContent.addComponent(confirmationText);

        Button logoutButton = new Button("Logout");
        logoutButton.addStyleName("buttons");
        logoutButton.setWidth(null);
        confirmationContent.addComponent(logoutButton);
        confirmationContent.setComponentAlignment(logoutButton, Alignment.BOTTOM_CENTER);
        logoutButton.addClickListener(new LogoutListener(session, getUI()));

        // Open pop-up in the UI
        getUI().addWindow(confirmationWindow);
    }

}