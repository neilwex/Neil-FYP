
package com.fyp;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by Neil on 18/02/2015.
 */

public class ModulesTabSheet extends TabSheet {

    private StreamResource templateResource;
    private StreamResource reportResource;
    private FileDownloader templateDownloader;
    private FileDownloader reportDownloader;
    private ResultSet r;

    private Window resultsWindow;
    private VerticalLayout resultsContent;
    private Container container;
    private Table table;

    private final String STUDENT_NUM = "Student Number";
    private final String CA = "CA";
    private final String EXAM = "Exam";
    private final String RESULT = "Result";
    private final String PERCENT = "Percentage";
    private final String AWARD = "Award";
    private final String GPA = "GPA Grade";
    private final String RANK = "Rank";

    public ModulesTabSheet(ResultSet moduleInfo) throws SQLException {

        this.setWidth("-1px");
        this.setHeight("-1px");
        r = moduleInfo;

        //if no modules yet, set up empty tab
        if (!r.isBeforeFirst() ) {

            VerticalLayout v = new VerticalLayout();
            v.setMargin(new MarginInfo(true, false, false, false));
            Label noModules = new Label("No registered modules yet.<br>", ContentMode.HTML);

            noModules.setCaption("Modules");
            v.addComponent(noModules);
            this.addTab(v);
            System.out.println("No modules");
        } else {
            while (moduleInfo.next()) {
                final String code = moduleInfo.getString("code");

                // setup grid layout for each tab
                GridLayout grid = new GridLayout(2, 9);
                grid.setMargin(new MarginInfo(true, false, false, false));
                grid.setSpacing(true);
                grid.setWidth(null);

                if (!moduleInfo.getBoolean("approved")) {
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

                    grid.addComponent(new Button("Download Template"), 0, 5);
                    templateResource = createResource(code, credits, ca, exam);
                    templateDownloader = new FileDownloader(templateResource);
                    templateDownloader.extend((AbstractComponent) grid.getComponent(0, 5));

                    grid.addComponent(new Button("View Results", new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            displayResultsWindow(code);
                        }
                    }), 1, 5);

                    grid.addComponent(new Button("Upload Results", new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            displayUploadWindow(code);
                        }
                    }), 0, 6);
                    grid.getComponent(0, 6).setWidth("100%");

                    grid.addComponent(new Button("Get Report"), 1, 6);
                    reportResource = createResource(code);
                    reportDownloader = new FileDownloader(reportResource);
                    reportDownloader.extend((AbstractComponent) grid.getComponent(1, 6));
                    //grid.getComponent(1,6).setWidth("131px");

                }

                grid.setCaption(code);
                this.addTab(grid);
            }
        }

    }


    protected StreamResource createResource(final String code) {
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
                        file = c.createModuleReport(num_students, modulesCredits, resultsData, moduleAverages);
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

    protected StreamResource createResource(final String code, final int credits, final int ca, final int exam ) {
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

    protected static InputStream getInputStream(File file) {
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
        summaryWindow.setClosable(true);
        summaryWindow.setResizable(false);
        summaryWindow.setContent(summaryContent);
        summaryWindow.center();
        summaryWindow.setImmediate(true);

        // add uploader
        CsvUploader receiver = new CsvUploader(code);
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


    private void displayResultsWindow(String code) {

        // Create a sub-window and set the content
        resultsWindow = new Window("View Results");
        resultsContent = new VerticalLayout();
        resultsContent.setMargin(true);
        resultsContent.setSpacing(true);
        resultsWindow.setWidth("750px");
        resultsWindow.setHeight("-1px");

        resultsWindow.setModal(true);
        resultsWindow.setResizable(false);
        resultsWindow.setContent(resultsContent);
        resultsWindow.center();
        resultsWindow.setImmediate(true);

        // Create an indexed container and fill it with columns for the table
        container = new IndexedContainer();

        // Define the names and data types of columns
        container.addContainerProperty(STUDENT_NUM, String.class, null);
        container.addContainerProperty(CA, Integer.class, null);
        container.addContainerProperty(EXAM, Integer.class, null);
        container.addContainerProperty(RESULT, Integer.class, null);
        container.addContainerProperty(PERCENT, Double.class, null);
        container.addContainerProperty(AWARD, String.class, null);
        container.addContainerProperty(GPA, String.class, null);
        container.addContainerProperty(RANK, Integer.class, null);

        try {
            createAndFillTable(code);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Open window in the UI
        getUI().addWindow(resultsWindow);
    }

    /**
     * Create the table, add the container to it and populate
     */
    private void createAndFillTable(String code) throws SQLException {

        table = new Table();
        table.setContainerDataSource(container);
        table.setWidth("655px");
        table.setSelectable(true);
        table.setImmediate(true);

        table.setColumnWidth(STUDENT_NUM, 130);
        table.setColumnWidth(CA, 55);
        table.setColumnWidth(EXAM, 65);
        table.setColumnWidth(RESULT, 70);
        table.setColumnWidth(PERCENT, 100);
        table.setColumnWidth(AWARD, 70);
        table.setColumnWidth(GPA, 95);
        table.setColumnWidth(RANK, 60);

        fillTable(code);

        resultsContent.addComponent(table);
        resultsContent.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
    }

    /**
     * Fills the contents of the container with results data from database, for use in table
     */
    private void fillTable(String code) throws SQLException {

        ResultSet results = Database.getModuleResults(code);
        int credits = Database.getCredits(code);

        // For rounding to 2 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        while (results.next()) {

            String studentNum = results.getString("student_num");
            double percentage = Double.parseDouble(df.format(results.getInt("total") * 5.0 / credits));

            // add details as a row to table
            container.addItem(studentNum);
            container.getContainerProperty(studentNum, STUDENT_NUM).setValue(studentNum);
            container.getContainerProperty(studentNum, CA).setValue(results.getInt("ca_mark"));
            container.getContainerProperty(studentNum, EXAM).setValue(results.getInt("final_exam_mark"));
            container.getContainerProperty(studentNum, RESULT).setValue(results.getInt("total"));
            container.getContainerProperty(studentNum, PERCENT).setValue(percentage);
            container.getContainerProperty(studentNum, AWARD).setValue(GradeCalculator.getAward(percentage));
            container.getContainerProperty(studentNum, GPA).setValue(GradeCalculator.getGPGrade(percentage));
            container.getContainerProperty(studentNum, RANK).setValue(results.getInt("rank"));
        }

    }

} //end of class