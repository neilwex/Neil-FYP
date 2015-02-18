package com.fyp;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * Created by Neil on 15/01/2015.
 */
public class jOpenDocumentCreateTest {

    File file;
    Sheet sheet;

    public jOpenDocumentCreateTest() throws IOException {

    }

    public File createFile(String code, int credits, int ca, int exam) throws IOException {

        String[] columns = new String[] { "Student", "CA (" + (ca * credits / 5) + ")", "Exam (" + (exam * credits / 5) + ")"};

        TableModel model = new DefaultTableModel(null, columns);

        // Save the data to an ODS file and open it.
        final File file = new File("results.ods");
        SpreadSheet.createEmpty(model).saveAs(file);

        Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);
        ///sheet.getCellAt("A1").setBackgroundColor(Color.RED);
        sheet.getSpreadSheet().saveAs(file);
        System.out.println("New spreadsheet created");
        return file;
        //OOUtils.open(file);

    }

    public File readFile(File uploadedFile, int ca, int exam) throws IOException {

        //File file = new File("uploads\\" + uploadedFile);
        Sheet sheet = SpreadSheet.createFromFile(uploadedFile).getSheet(0);

        sheet.getCellAt("A1").setBackgroundColor(Color.YELLOW);
        sheet.getSpreadSheet().saveAs(file);
        System.out.println("New spreadsheet created");

        return file;
    }


    public File createReport(int num_students, int credits, ResultSet rows, ResultSet moduleInfo) throws SQLException, IOException {

        //create new spreadsheet for report
        final File file = new File("files\\results.ods");
        SpreadSheet.create(1,10,num_students + 20).saveAs(file);
        Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);

        return fillReport(file, sheet, num_students, credits, rows, moduleInfo );
    }

    private File fillReport(File file, Sheet sheet, int num_students, int credits, ResultSet rows, ResultSet moduleInfo) throws SQLException, IOException {

        sheet.getCellAt("A1").setValue("Module:");
        sheet.getCellAt("B1").setValue("CODE HERE");
        sheet.getCellAt("A2").setValue("Lecturer");
        sheet.getCellAt("B2").setValue("LECTURER HERE");
        sheet.getCellAt("A3").setValue("# Students:");
        sheet.getCellAt("B3").setValue(num_students);
        sheet.getCellAt("A5").setValue("DISCLAIMER: the contents of this report are intended to be read-only. Please do not attempt to modify any data.");

        // setup column headers
        sheet.getCellAt("A7").setValue("Student");
        sheet.getCellAt("B7").setValue("CA");
        sheet.getCellAt("C7").setValue("Final Exam");
        sheet.getCellAt("D7").setValue("Total");
        sheet.getCellAt("E7").setValue("Percentage");
        sheet.getCellAt("F7").setValue("Pass");
        sheet.getCellAt("G7").setValue("Award");
        sheet.getCellAt("H7").setValue("GPA Grade");
        sheet.getCellAt("I7").setValue("Class Rank");

        DecimalFormat df = new DecimalFormat("#.##");
        int row = 8;
        while (rows.next()) { //add data by row

            // Round percentage to 2 decimal points
            double percentage = Double.parseDouble(df.format(rows.getInt("total") * 5.0 / credits));

            sheet.getCellAt("A" + row).setValue(rows.getString("student_num"));
            sheet.getCellAt("B" + row).setValue(rows.getInt("ca_mark"));
            sheet.getCellAt("C" + row).setValue(rows.getInt("final_exam_mark"));
            sheet.getCellAt("D" + row).setValue(rows.getInt("total"));
            sheet.getCellAt("E" + row).setValue(percentage);
            sheet.getCellAt("F" + row).setBackgroundColor(percentage >= Database.PASS_MARK ? Color.GREEN : Color.RED);
            sheet.getCellAt("G" + row).setValue(GradeCalculator.getAward(percentage));
            sheet.getCellAt("H" + row).setValue(GradeCalculator.getGPGrade(percentage));
            sheet.getCellAt("I" + row).setValue(rows.getInt("rank"));

            row++;
        }

        row += 2;
        moduleInfo.next();
        double avg_percentage = Double.parseDouble(df.format(moduleInfo.getInt("total") * 5.0 / credits));

        sheet.getCellAt("A" + row).setValue("AVERAGES");
        sheet.getCellAt("B" + row).setValue(moduleInfo.getInt("ca"));
        sheet.getCellAt("C" + row).setValue(moduleInfo.getInt("exam"));
        sheet.getCellAt("D" + row).setValue(moduleInfo.getInt("total"));
        sheet.getCellAt("E" + row).setValue(avg_percentage);
        sheet.getCellAt("F" + row).setBackgroundColor(avg_percentage >= Database.PASS_MARK ? Color.GREEN : Color.RED);
        sheet.getCellAt("G" + row).setValue(GradeCalculator.getAward(avg_percentage));
        sheet.getCellAt("H" + row).setValue(GradeCalculator.getGPGrade(avg_percentage));

        row += 2;

        sheet.getCellAt("A" + row).setValue("Standard Deviation:");
        sheet.getCellAt("B" + row).setValue(Double.parseDouble(df.format(moduleInfo.getDouble("stddev"))));
        row++;
        sheet.getCellAt("A" + row).setValue("Maximum Mark:");
        sheet.getCellAt("B" + row).setValue(moduleInfo.getDouble("max"));
        row++;
        sheet.getCellAt("A" + row).setValue("Minimum Mark:");
        sheet.getCellAt("B" + row).setValue(moduleInfo.getDouble("min"));

        sheet.getSpreadSheet().saveAs(file);
        System.out.println("New report created");
        return file;
    }
}
