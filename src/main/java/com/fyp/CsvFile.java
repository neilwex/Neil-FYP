package com.fyp;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Neil on 09/02/2015.
 */
public class CsvFile {

    protected static File createCsvFile(int credits, int ca, int exam) throws IOException {
        File file = new File("files\\results.csv");

        try {
            FileWriter writer = new FileWriter(file);
            writer.append("Student");
            writer.append(',');
            writer.append("CA (" + (ca * credits / 5) + ")");
            writer.append(',');
            writer.append("Exam (" + (exam * credits / 5) + ")");

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return file;
    }

    protected static boolean isValidCsvFile(File fileToRead, String module) {

        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try { // this handles incorrect formats being uploaded

            ResultSet moduleDetails = Database.getModuleStats(module);
            moduleDetails.next();

            int credits = moduleDetails.getInt("credit_weighting");
            int ca_percent = moduleDetails.getInt("ca_mark_percentage");
            int exam_percent = moduleDetails.getInt("final_exam_percentage");

            boolean correctContent = true;
            br = new BufferedReader(new FileReader(fileToRead));
            br.readLine(); // skip first line THIS NEEDS TO BE CHANGED LATER TO CHECK HEADERS ARE CORRECT
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] result = line.split(cvsSplitBy);
                if (result.length == 3 && ! result[0].trim().isEmpty() && ! result[1].trim().isEmpty() && ! result[2].trim().isEmpty()) {

                    try {
                        // check results are in numeric form
                        double ca = Double.parseDouble(result[1]);
                        double exam = Double.parseDouble(result[2]);

                        //check results are in correct range with module CA/Exam/Credits info
                        if (ca < 0 || ca > (ca_percent * credits / 5) || exam < 0 || exam > (exam_percent * credits / 5)) {
                            correctContent = false;
                            break;
                        }
                    } catch (NumberFormatException nfe) {
                        System.out.println("NumberFormatException encountered for ca/exam data");
                        nfe.printStackTrace();
                        correctContent = false;
                        break;
                    }
                } else {
                    correctContent = false;
                    break;
                }

            }

            if (! correctContent) {
                System.out.println("File contents not in correct format");
                return false;
            }

            System.out.println("File contents are in correct format");

            //do sql update with information - have sql statement return boolean in case update fails

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {e.printStackTrace();}
            }
        }

        return true;
    }

}