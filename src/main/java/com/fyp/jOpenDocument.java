package com.fyp;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Neil on 15/01/2015.
 */
public class jOpenDocument {

    File file;
    Sheet sheet;
    Map<String, Character> map;
    String disclaimer = "DISCLAIMER: The contents of this report are intended to be read-only. Please do not attempt to modify any data.";

    public File createOverallReport(int numMods) throws SQLException, IOException {

        ResultSet modules = Database.getList("SELECT DISTINCT(code) AS code FROM modules");
        ResultSet results = Database.getAllResults();

        //create new spreadsheet for report
        final File file = new File("files\\results.ods");
        SpreadSheet.create(numMods + 2,20,200).saveAs(file);

        //fill the first sheet with an overview of students results for every module
        sheet = SpreadSheet.createFromFile(file).getSheet(0);
        sheet.setName("Overview");
        fillOverviewReport(file, sheet, modules, results);

        modules = Database.getList("SELECT DISTINCT(code) AS code FROM modules");
        results = Database.getAllResults();

        //fill the second sheet with an colour coded table of students results for every module
        sheet = SpreadSheet.createFromFile(file).getSheet(1);
        sheet.setName("Colour Graph");
        fillColorCodedReport(file, sheet, modules, results);

        ResultSet numModules = Database.getList("SELECT DISTINCT(code) AS code FROM modules");
        //fill each sheet with a module
        int i = 2;
        while (numModules.next()) {
            sheet = SpreadSheet.createFromFile(file).getSheet(i);
            String code = numModules.getString("code");

            //get module data from database
            ResultSet moduleDetails = Database.getModuleDetails(code);
            ResultSet resultsData = Database.getModuleResults(code);
            ResultSet moduleAverages = Database.getModuleAverages(code);

            moduleDetails.next();

            fillModuleReport(file, sheet, moduleDetails, resultsData, moduleAverages);
            i++;
        }

        //return the file
        return file;
    }

    private void fillOverviewReport(File file, Sheet sheet, ResultSet numModules, ResultSet results) throws SQLException, IOException {

        sheet.getCellAt("A1").setValue("Report Overview");
        sheet.getCellAt("A2").setValue(disclaimer);
        sheet.getCellAt("A3").setValue("All marks are displayed as a percentage");
        sheet.getCellAt("A5").setValue("Student");
        char letter = 'B';

        // map for storing which column contains which module
        map = new HashMap<String, Character>();

        while (numModules.next()) {
            sheet.getCellAt(letter + "5").setValue(numModules.getString("code"));
            map.put(numModules.getString("code"), letter);
            letter++;
        }

        map.put("Credits", ++letter);
        map.put("Overall", ++letter);
        sheet.getCellAt(map.get("Credits") + Integer.toString(5)).setValue("Credits");
        sheet.getCellAt(map.get("Overall") + Integer.toString(5)).setValue("Overall");

        int rows = 6;
        String prev = "";
        String current;
        int credits =0;
        int creditsFailed = 0;
        boolean compensate = true;
        String outcome = "";

        // For rounding to 2 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        while (results.next()) {

            current = results.getString("student_num");
            if (! current.equals(prev)) {

                if (credits != 60) {
                    outcome = "Insufficient";
                } else if (creditsFailed == 0) {
                    outcome = "Pass";
                } else if (creditsFailed > 10) {
                    outcome = "Fail";
                } else if (compensate){
                    outcome = "Pass-by-Comp";
                } else {
                    outcome = "Fail";
                }

                sheet.getCellAt(map.get("Credits") + Integer.toString(rows)).setValue(credits);
                sheet.getCellAt(map.get("Overall") + Integer.toString(rows)).setValue(outcome);

                rows++;
                sheet.getCellAt('A' + Integer.toString(rows)).setValue(current);
                credits = 0;
                creditsFailed = 0;
                compensate = true;
            }

            //get corresponding column for module and add the result
            letter = map.get(results.getString("code"));
            double percentage = results.getInt("result") * 5.0 / results.getInt("credit_weighting");
            percentage = Double.parseDouble(df.format(percentage));

            // check if module passed, and if not, if student is elligible to compensate
            if (percentage < Database.PASS_MARK) {
                creditsFailed += results.getInt("credit_weighting");
                if (percentage < Database.COMPENSATION_MARK) {
                    compensate = false;
                }
            }

            sheet.getCellAt(letter + Integer.toString(rows)).setValue(percentage);

            credits += results.getInt("credit_weighting"); // maintain total credits per student

            prev = current;
        }

        /// fill last row's details
        sheet.getCellAt(map.get("Credits") + Integer.toString(rows)).setValue(credits);
        if (credits != 60) {
            outcome = "Insufficient";
        } else if (creditsFailed == 0) {
            outcome = "Pass";
        } else if (creditsFailed > 10) {
            outcome = "Fail";
        } else if (compensate){
            outcome = "Pass-by-Comp";
        } else {
            outcome = "Fail";
        }
        sheet.getCellAt(map.get("Overall") + Integer.toString(rows)).setValue(outcome);

        // reset first row
        sheet.getCellAt(map.get("Credits") + Integer.toString(6)).setValue("");
        sheet.getCellAt(map.get("Overall") + Integer.toString(6)).setValue("");

        sheet.getSpreadSheet().saveAs(file);
    }

    private void fillColorCodedReport (File file, Sheet sheet, ResultSet numModules, ResultSet results) throws SQLException, IOException {

        sheet.getCellAt("A1").setValue("Report Graph");
        sheet.getCellAt("A2").setValue(disclaimer);
        sheet.getCellAt("A5").setValue("Student");
        char letter = 'B';

        // map for storing which column contains which module
        map = new HashMap<String, Character>();

        while (numModules.next()) {
            sheet.getCellAt(letter + "5").setValue(numModules.getString("code"));
            map.put(numModules.getString("code"), letter);
            letter++;
        }

        map.put("Credits", ++letter);
        map.put("Overall", ++letter);

        int rows = 6;
        String prev = "";
        String current;

        // For rounding to 2 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        while (results.next()) {

            current = results.getString("student_num");
            if (! current.equals(prev)) {
                rows++;
                sheet.getCellAt('A' + Integer.toString(rows)).setValue(current);
            }

            //calculate the result
            letter = map.get(results.getString("code"));
            double percentage = results.getInt("result") * 5.0 / results.getInt("credit_weighting");
            percentage = Double.parseDouble(df.format(percentage));

            //add corresponding color to correct cell
            sheet.getCellAt(letter + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(percentage));

            prev = current;
        }

        rows += 2;
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(70));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(70));
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(60));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(60));
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(50));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(50));
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(45));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(45));
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(40));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(40));
        sheet.getCellAt('A' + Integer.toString(rows)).setBackgroundColor(GradeCalculator.getAwardColor(0));
        sheet.getCellAt('B' + Integer.toString(rows++)).setValue("= " + GradeCalculator.getAward(0));

        sheet.getSpreadSheet().saveAs(file);
    }

    public File createModuleReport(ResultSet moduleDetails, ResultSet rows, ResultSet moduleInfo) throws SQLException, IOException {

        moduleDetails.next();

        //create new spreadsheet for report
        final File file = new File("files\\results.ods");
        SpreadSheet.create(1,10,moduleDetails.getInt("count") + 20).saveAs(file);
        Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);

        return fillModuleReport(file, sheet, moduleDetails, rows, moduleInfo);
    }

    private File fillModuleReport(File file, Sheet sheet, ResultSet moduleDetails, ResultSet rows, ResultSet moduleAverages) throws SQLException, IOException {

        int num_students = moduleDetails.getInt("count");
        String lecturer = moduleDetails.getString("lecturer");
        String code = moduleDetails.getString("code");
        int credits = moduleDetails.getInt("credit_weighting");
        sheet.setName(code);

        sheet.getCellAt("A1").setValue("Module:");
        sheet.getCellAt("B1").setValue(code);
        sheet.getCellAt("A2").setValue("Lecturer");
        sheet.getCellAt("B2").setValue(lecturer);
        sheet.getCellAt("A3").setValue("# Students:");
        sheet.getCellAt("B3").setValue(num_students);
        sheet.getCellAt("A5").setValue(disclaimer);

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
        moduleAverages.next();
        double avg_percentage = Double.parseDouble(df.format(moduleAverages.getInt("total") * 5.0 / credits));

        sheet.getCellAt("A" + row).setValue("AVERAGES");
        sheet.getCellAt("B" + row).setValue(moduleAverages.getInt("ca"));
        sheet.getCellAt("C" + row).setValue(moduleAverages.getInt("exam"));
        sheet.getCellAt("D" + row).setValue(moduleAverages.getInt("total"));
        sheet.getCellAt("E" + row).setValue(avg_percentage);
        sheet.getCellAt("F" + row).setBackgroundColor(avg_percentage >= Database.PASS_MARK ? Color.GREEN : Color.RED);
        sheet.getCellAt("G" + row).setValue(GradeCalculator.getAward(avg_percentage));
        sheet.getCellAt("H" + row).setValue(GradeCalculator.getGPGrade(avg_percentage));

        row += 2;

        sheet.getCellAt("A" + row).setValue("Standard Dev:");
        sheet.getCellAt("B" + row++).setValue(Double.parseDouble(df.format(moduleAverages.getDouble("stddev"))));
        //row++;
        sheet.getCellAt("A" + row).setValue("Max. Mark:");
        sheet.getCellAt("B" + row++).setValue(moduleAverages.getDouble("max"));
        //row++;
        sheet.getCellAt("A" + row).setValue("Min. Mark:");
        sheet.getCellAt("B" + row).setValue(moduleAverages.getDouble("min"));

        sheet.getSpreadSheet().saveAs(file);
        return file;
    }

    public File createStudentReport(String student, ResultSet rows, String passInfo) throws IOException, SQLException {

        //create new spreadsheet for report
        final File file = new File("files\\results.ods");
        SpreadSheet.create(1,10,30).saveAs(file);
        Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);

        return fillStudentReport(file, sheet, student, rows, passInfo);
    }

    private File fillStudentReport(File file, Sheet sheet, String student, ResultSet rows, String passInfo) throws IOException, SQLException {

        sheet.getCellAt("A1").setValue("Student:");
        sheet.getCellAt("B1").setValue(student);
        sheet.getCellAt("A3").setValue(disclaimer);

        // setup column headers
        sheet.getCellAt("A5").setValue("Module");
        sheet.getCellAt("B5").setValue("Credits");
        sheet.getCellAt("C5").setValue("CA");
        sheet.getCellAt("D5").setValue("Final Exam");
        sheet.getCellAt("E5").setValue("Total");
        sheet.getCellAt("F5").setValue("Percentage");
        sheet.getCellAt("G5").setValue("Pass");
        sheet.getCellAt("H5").setValue("Award");
        sheet.getCellAt("I5").setValue("GPA Grade");

        DecimalFormat df = new DecimalFormat("#.##");
        int totalCredits = 0;
        int totalMarks = 0;

        int row = 6;
        while (rows.next()) { //add data by row

            int ca = rows.getInt("ca_mark");
            int exam = rows.getInt("final_exam_mark");
            int credits = rows.getInt("credit_weighting");

            // Round percentage to 2 decimal points
            double percentage = Double.parseDouble(df.format((ca + exam) * 5.0 / credits));

            sheet.getCellAt("A" + row).setValue(rows.getString("module_code"));
            sheet.getCellAt("B" + row).setValue(credits);
            sheet.getCellAt("C" + row).setValue(ca);
            sheet.getCellAt("D" + row).setValue(exam);
            sheet.getCellAt("E" + row).setValue(ca + exam);
            sheet.getCellAt("F" + row).setValue(percentage);
            sheet.getCellAt("G" + row).setBackgroundColor(percentage >= Database.PASS_MARK ? Color.GREEN : Color.RED);
            sheet.getCellAt("H" + row).setValue(GradeCalculator.getAward(percentage));
            sheet.getCellAt("I" + row).setValue(GradeCalculator.getGPGrade(percentage));

            totalCredits += credits;
            totalMarks += ca + exam;

            row++;
        }

        row++;
        double overallPercentage = totalMarks * 5.0 / totalCredits;

        sheet.getCellAt("A" + row).setValue("OVERALL");
        sheet.getCellAt("B" + row).setValue(totalCredits);
        sheet.getCellAt("F" + row).setValue(Double.parseDouble(df.format(overallPercentage)));
        sheet.getCellAt("H" + row).setValue(GradeCalculator.getAward(overallPercentage));
        sheet.getCellAt("I" + row).setValue(GradeCalculator.getGPGrade(overallPercentage));

        row += 2;

        String[] resultsNotes = passInfo.split("<br>");
        for (String info: resultsNotes) {
            sheet.getCellAt("A" + row).setValue(info);
            row++;
        }

        sheet.getSpreadSheet().saveAs(file);
        return file;
    }

    public File createStandardizedReport(int numModules, double OverallAV, double OverallSD) throws SQLException, IOException {

        //create new spreadsheet for report
        final File file = new File("files\\results.ods");
        SpreadSheet.create(numModules,10,300).saveAs(file);

        ResultSet modules = Database.getList("SELECT DISTINCT(code) AS code FROM modules");
        //fill each sheet with a module
        int i = 0;
        while (modules.next()) {
            sheet = SpreadSheet.createFromFile(file).getSheet(i);
            String code = modules.getString("code");

            //get module data from database
            ResultSet moduleDetails = Database.getModuleDetails(code);
            ResultSet resultsData = Database.getModuleResults(code);
            double av = Database.getModuleAverage(code);
            double sd = Database.getModuleStandardDev(code);
            moduleDetails.next();

            fillStandardizedReport(file, sheet, OverallAV, OverallSD, av, sd, moduleDetails, resultsData);
            i++;
        }

        return file;
    }

    private File fillStandardizedReport(File file, Sheet sheet, double OverallAV, double OverallSD, double av, double sd,
                                       ResultSet moduleDetails, ResultSet rows) throws SQLException, IOException {

        int num_students = moduleDetails.getInt("count");
        String lecturer = moduleDetails.getString("lecturer");
        String code = moduleDetails.getString("code");
        int credits = moduleDetails.getInt("credit_weighting");
        sheet.setName(code);

        int rawFirst, rawTwoOne, rawTwoTwo, rawThird, rawPass, rawFail;
        rawFirst = rawTwoOne = rawTwoTwo = rawThird = rawPass = rawFail = 0;
        int modFirst, modTwoOne, modTwoTwo, modThird, modPass, modFail;
        modFirst = modTwoOne = modTwoTwo = modThird = modPass = modFail = 0;

        sheet.getCellAt("A1").setValue("Module:");
        sheet.getCellAt("B1").setValue(code);
        sheet.getCellAt("A2").setValue("Lecturer");
        sheet.getCellAt("B2").setValue(lecturer);
        sheet.getCellAt("A3").setValue("# Students:");
        sheet.getCellAt("B3").setValue(num_students);
        sheet.getCellAt("A5").setValue(disclaimer);

        // setup column headers
        sheet.getCellAt("A7").setValue("Student");
        sheet.getCellAt("B7").setValue("Raw Mark (%)");
        sheet.getCellAt("C7").setValue("Modified Mark (%)");

        DecimalFormat df = new DecimalFormat("#.##");
        int row = 8;
        while (rows.next()) { //add data by row

            // Round percentage to 2 decimal points
            double raw = Double.parseDouble(df.format(rows.getInt("total") * 5.0 / credits));
            double standardized = Double.parseDouble(df.format( ((raw - av) * OverallSD / sd ) + OverallAV));

            sheet.getCellAt("A" + row).setValue(rows.getString("student_num"));
            sheet.getCellAt("B" + row).setValue(raw);
            sheet.getCellAt("C" + row).setValue(standardized);

            if (raw >= 70) {
                rawFirst++;
            } else if (raw >= 60) {
                rawTwoOne++;
            } else if (raw >= 50) {
                rawTwoTwo++;
            } else if (raw >= 45) {
                rawThird++;
            } else if (raw >= 40) {
                rawPass++;
            }  else {
                rawFail++;
            }

            if (standardized >= 70) {
                modFirst++;
            } else if (standardized >= 60) {
                modTwoOne++;
            } else if (standardized >= 50) {
                modTwoTwo++;
            } else if (standardized >= 45) {
                modThird++;
            } else if (standardized >= 40) {
                modPass++;
            }  else {
                modFail++;
            }

            row++;
        }

        // setup column headers
        sheet.getCellAt("F7").setValue("Awards");
        sheet.getCellAt("G7").setValue("# Raw");
        sheet.getCellAt("H7").setValue("# Modified");
        sheet.getCellAt("F8").setValue("1.1");
        sheet.getCellAt("G8").setValue(rawFirst);
        sheet.getCellAt("H8").setValue(modFirst);
        sheet.getCellAt("F9").setValue("2.1");
        sheet.getCellAt("G9").setValue(rawTwoOne);
        sheet.getCellAt("H9").setValue(modTwoOne);
        sheet.getCellAt("F10").setValue("2.2");
        sheet.getCellAt("G10").setValue(rawTwoTwo);
        sheet.getCellAt("H10").setValue(modTwoTwo);
        sheet.getCellAt("F11").setValue("3.1");
        sheet.getCellAt("G11").setValue(rawThird);
        sheet.getCellAt("H11").setValue(modThird);
        sheet.getCellAt("F12").setValue("Pass");
        sheet.getCellAt("G12").setValue(rawPass);
        sheet.getCellAt("H12").setValue(modPass);
        sheet.getCellAt("F13").setValue("Fail");
        sheet.getCellAt("G13").setValue(rawFail);
        sheet.getCellAt("H13").setValue(modFail);

        sheet.getSpreadSheet().saveAs(file);
        return file;
    }

}