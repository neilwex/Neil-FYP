package com.fyp;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Neil on 25/01/2015.
 */
@SuppressWarnings("serial")
public class ArchivingBrowser extends VerticalLayout implements View, Tree.ExpandListener {

    private VaadinSession session;
    private VerticalLayout root;
    private HorizontalLayout sessionInfo;
    private Label userDetails;
    private Button logoutButton;
    private ArrayList<String> checked;
    private Tree.ItemStyleGenerator itemStyleGenerator;

    private String userGroup;
    private String currentUser;
    private String userEmail;
    public String workingDirectory;
    public String treeRoot;

    private Button popUp;
    private TextField budgetNumTextField;
    private CheckBox deleteAfterCheckbox;
    private Button submitButton;
    private Window fileBrowserWindow;
    private VerticalLayout fileBrowserContent;
    private Panel explorerPanel;
    private Tree tree;
    private Button selectFile;
    private String fileToArchive;
    private String budgetNumber;
    private Boolean deleteAfterArchiving;
    private Window confirmationWindow;
    private VerticalLayout confirmationContent;

    protected static final String ARCHIVE_BROWSER = "archiveBrowser";

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        workingDirectory = System.getProperty("user.dir");
        treeRoot = workingDirectory + "/groupShareMounts/" + userGroup + "/" + currentUser + "/" + userGroup;
        session = getSession();

        initLayout();
        initButtons();
        initTextListener();
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
        Page.getCurrent().setTitle("Archive files");

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
        root.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);
        sessionInfo.setWidth(null);
        sessionInfo.setMargin(new MarginInfo(true, true, false, false));
        root.addComponent(sessionInfo);
        //allHeaderInfo.addComponent(sessionInfo);
        root.setComponentAlignment(sessionInfo, Alignment.TOP_RIGHT);

        // get session info
        currentUser = session.getAttribute("user").toString();
        //userGroup = session.getAttribute("group").toString();
        //userEmail = session.getAttribute("email").toString();

        // display session information and logout button
        userDetails = new Label("Logged in: " + currentUser);
        sessionInfo.addComponent(userDetails);

        // logout button
        logoutButton = new Button("Logout");
        logoutButton.addStyleName(BaseTheme.BUTTON_LINK);
        logoutButton.addStyleName("buttons");
        sessionInfo.addComponent(logoutButton);
        logoutButton.addClickListener(new LogoutListener(session, getUI()));

        // stores which node is selected
        checked = new ArrayList<String>(1);

        // grid for selecting file/folder to archive
        GridLayout grid = new GridLayout(2, 5);
        grid.setSpacing(true);
        grid.setWidth(null);
        root.addComponent(grid);
        root.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

        // title for application
        Label heading = new Label("Group Archiving Application");
        heading.addStyleName("heading");
        heading.setWidth("100%");
        grid.addComponent(heading, 0, 0, 1, 0);

        // title for selected file label
        Label selectedFileTitle = new Label("Selected File:");
        grid.addComponent(selectedFileTitle);

        // button for opening file browser pop-up
        popUp = new Button("Please select file/folder");
        popUp.addStyleName(BaseTheme.BUTTON_LINK);
        popUp.addStyleName("buttons");
        grid.addComponent(popUp);

        // title for budget number label
        Label budgetNumTitle = new Label("Budget Number:");
        grid.addComponent(budgetNumTitle);

        // textfield for budget number
        budgetNumTextField = new TextField();
        budgetNumTextField.setInputPrompt("00000");
        budgetNumTextField.setRequired(true);
        budgetNumTextField.setRequiredError("A valid budget number must be provided");
        grid.addComponent(budgetNumTextField);

        Label deleteAfterLabel = new Label("Delete after:");
        grid.addComponent(deleteAfterLabel);

        // checkbox for deleting after archive option
        deleteAfterCheckbox = new CheckBox();
        grid.addComponent(deleteAfterCheckbox);

        // button for archiving selected file
        submitButton = new Button("Request Archiving");
        submitButton.addStyleName("buttons");
        submitButton.addStyleName("submit");
        grid.addComponent(submitButton, 1, 4);
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

                        tree.setImmediate(true);
                        float height = fileBrowserWindow.getHeight();
                        int newTreeHeight = (int) (height - 130);
                        tree.setHeight(newTreeHeight + "px");
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
                            fileToArchive = selected.replace(treeRoot, "");
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

        initFileBrowserPopUpButton();
        initSubmitButton();
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

        // create panel with tree component
        tree = new Tree();
        explorerPanel = new Panel(tree);
        tree.setHeight("470px");
        tree.addStyleName("checkbox");
        tree.setSelectable(false); // Only allow checkbox selections
        fileBrowserContent.addComponent(explorerPanel);

        //Submit button
        selectFile = new Button("Select");
        selectFile.addStyleName("buttons");
        fileBrowserContent.addComponent(selectFile);

        // Open pop-up in the UI
        getUI().addWindow(fileBrowserWindow);

        workingDirectory = System.getProperty("user.dir");
        treeRoot = workingDirectory + "/groupShareMounts/" + userGroup + "/" + currentUser + "/" + userGroup;

        initTree();

        // populate tree's root node
        populateNode(treeRoot, null);
    }

    /**
     * Set the listener for submit button
     */
    private void initSubmitButton() {

        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                deleteAfterArchiving = deleteAfterCheckbox.getValue();
                budgetNumber = budgetNumTextField.getValue().trim();

                if (checked.isEmpty()) {
                    Notification.show("No file selected - Please select a file to archive");

                } else if (budgetNumber.isEmpty()) {
                    budgetNumTextField.addStyleName("emptyField");
                    Notification.show("Please provide a valid budget number");

                } else  {
                    budgetNumTextField.removeStyleName("emptyField");
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
        summaryWindow.setWidth("630px");
        summaryWindow.setHeight("190px");
        summaryWindow.setModal(true);
        summaryWindow.setClosable(false);
        summaryWindow.setResizable(false);
        summaryWindow.setContent(summaryContent);
        summaryWindow.center();
        summaryWindow.setImmediate(true);

        Label confirmationInfo = new Label("You have requested archiving for the following:");
        summaryContent.addComponent(confirmationInfo);

        GridLayout confirmationGrid = new GridLayout(2, 2);
        confirmationGrid.setSpacing(true);
        confirmationGrid.setWidth(null);
        summaryContent.addComponent(confirmationGrid);
        summaryContent.setComponentAlignment(confirmationGrid, Alignment.MIDDLE_CENTER);

        // label containing title for selected file
        Label selectedFile = new Label("Selected file:");
        confirmationGrid.addComponent(selectedFile);

        // label containing name of file to be archived
        Label fileForArchiving = new Label(fileToArchive);
        confirmationGrid.addComponent(fileForArchiving);

        // label containing title for budget number
        Label budgetNumberTitle = new Label("Budget Number:");
        confirmationGrid.addComponent(budgetNumberTitle);

        // label containing budget number entered
        Label enteredBudgetNumber = new Label(budgetNumber);
        confirmationGrid.addComponent(enteredBudgetNumber);

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

                if (createAndSendEmail()) {
                    initConfirmationPopUp();
                }
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

    /**
     * Sets up the email details and creates an EmailTicket object for sending
     *
     * @return      true if the email is successfully sent; otherwise, false
     */
    private boolean createAndSendEmail() {

        // set up email ticket to be sent
        String from = userEmail;
        String[] to = new String[]{"example@test.com"}; //neil_77@gtest.embl.de"}; //"itsupport@embl.de"
        String[] cc = new String[]{};
        String subject = "TEST ARCHIVE TICKET";
        boolean copyToWebmaster = false;
        String webmaster = "";
        String emailContent = "Hi,\n\n" +
                "Please archive the following file:\n\n" +
                checked.get(0) +
                "\n\n" +
                "All files to be " + (deleteAfterArchiving ? "deleted" : "kept") + " after archiving.\n" +
                "Budget to be billed is " + budgetNumber + " (" + userGroup + " group)\n\n" +
                "Have a nice day";

        EmailTicket email = new EmailTicket(from, to, cc, subject, emailContent, copyToWebmaster, webmaster);

        if ( email.prepareAndSendEmail() ) {
            System.out.println("Email ticket sent");
            return true;

        } else {
            Notification.show("Problem with request. Please try again later.");
            return false;
        }

    }

    /**
     * Set the text listener for changing the class name for CSS
     */
    private void initTextListener() {
        budgetNumTextField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                // Get the entered text
                String enteredValue = event.getProperty().getValue().toString().trim();

                if (enteredValue.isEmpty()) {
                    budgetNumTextField.addStyleName("emptyField");
                } else {
                    budgetNumTextField.removeStyleName("emptyField");
                }
            }
        });

        budgetNumTextField.setImmediate(true);
    }

    /**
     * Set the click listener for the tree and add checkboxes to tree
     */
    private void initTree() {

        // set the appropriate CSS class name for tree nodes
        itemStyleGenerator = new Tree.ItemStyleGenerator() {
            @Override
            public String getStyle(Tree tree, Object itemId) {
                if (checked.contains(itemId.toString())) {
                    return "checked";
                } else {
                    return "unchecked";
                }
            }
        };
        tree.setItemStyleGenerator(itemStyleGenerator);

        // allow the user to "check" and "uncheck" tree nodes by clicking them
        tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {

                tree.setImmediate(true);

                // ID of the clicked item
                String itemID = event.getItemId().toString();

                //logic for single select
                if (checked.isEmpty() ) {
                    checked.add(itemID); // add selected node
                } else {

                    if (checked.contains(itemID)) {

                        checked.remove(itemID); // remove selected node
                    } else {
                        checked.set(0, itemID); // replace selected node
                    }
                }

                // style tree items (checkboxes)
                tree.markAsDirtyRecursive();
            }

        });

        // "this" handles tree's expand event
        tree.addExpandListener(this);
    }

    /*
     * Handle tree expand event, populate expanded node's children
     * with new files and directories.
     */
    public void nodeExpand(Tree.ExpandEvent event) {
        Item i = tree.getItem(event.getItemId());

        if (!tree.hasChildren(i)) {
            // populate tree's node which was expanded
            populateNode(event.getItemId().toString(), event.getItemId());
        }
    }

    /**
     * Populates tree with files.
     * New files are added to tree and file's parent and children properties are updated.
     *
     * @param filePath
     *            path whose contents are added to tree
     * @param parent
     *            for added nodes, if null then new nodes are added to root node
     */
    private void populateNode(String filePath, Object parent) {
        File file = new File(filePath);

        // if file is a directory with read and execute permissions
        if (file.isDirectory() && file.canRead() && file.canExecute()) {

            File[] dirFiles = file.listFiles();

            if (dirFiles == null) {

                Notification.show("Cannot display the contents of this directory (maybe permissions?)");
            } else {

                Arrays.sort(dirFiles);
                for (int i = 0; i < dirFiles.length; i++) {
                    File path = dirFiles[i];

                    tree.addItem(path);
                    // display just current directory (not absolute path)
                    tree.setItemCaption(path, path.getName());

                    // set parent if this file has one
                    if (parent != null) {
                        tree.setParent(path, parent);
                    }

                    // check if file is a directory and read access exists
                    if (dirFiles[i].isDirectory() && dirFiles[i].canRead()) {
                        // children exist
                        tree.setChildrenAllowed(path, true);
                    } else {
                        // children do not exist
                        tree.setChildrenAllowed(path, false);
                    }
                }
            }

        } else {
            Notification.show("Problem occurred!");
        }
    }

}