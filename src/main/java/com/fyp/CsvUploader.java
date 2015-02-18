package com.fyp;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Neil on 18/02/2015.
 */

public class CsvUploader implements Upload.Receiver, Upload.SucceededListener {

    public File file;
    public String module;

    public CsvUploader(String module) {
        this.module = module;
    }

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
        boolean isFileCorrectFormat = CsvFile.isValidCsvFile(file, module);
        System.out.println("isFileCorrectFormat? " + isFileCorrectFormat);

        if (isFileCorrectFormat) {
            boolean readCsvFileSuccessfully = Database.readCsvFile(file.getAbsolutePath(), module);

            if (readCsvFileSuccessfully) {

                Notification.show("Selected file contents successfully added to the database");


            } else {
                new Notification("Unable to read file contents - please select a valid file",
                        Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
            }

        } else {
            new Notification("Selected file is not in correct format - please select a valid file",
                    Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
        }

    }

} //end of class