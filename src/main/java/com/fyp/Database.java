package com.fyp;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Neil on 15/01/2015.
 */
public class Database {

    // JDBC driver name and database URL
    protected static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    //jdbc:mysql://localhost:3306/results_db
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/results_db";

    //  Database credentials
    protected static final String USER = "root";
    protected static final String PASS = "Wexford96-";

    public static ResultSet rs;
    public static String sql;

    protected static final int PASS_MARK = 40;
    protected static final int COMPENSATION_MARK = 30;

    protected static Statement stmt;

    public static void main(String[] args) {
        Connection conn = null;
        stmt = null;

        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            addUser(stmt, "Neil", "test");

            // Testing methods I've created

            //getAllResults(stmt);
            //getAverageGrade(stmt);
            ////getAverageGrade(stmt, "CS101");
            //getMaxGrade(stmt, "CS101");

            //getStdDev(stmt, "CS101");
            //checkGrades(stmt);

            //getGPA(stmt,1010);

            //getMinGrade(stmt, "CS101");

            //STEP 6: Clean-up environment
            //rs.close();
            //stmt.close();
            //conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main

    public static void connect() throws SQLException {
        Connection conn = null;
        Statement stmt = null;

        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();

            // Testing methods I've created

            //getAllResults(stmt);
            //getAverageGrade(stmt);
            ////getAverageGrade(stmt, "CS101");
            //getMaxGrade(stmt, "CS101");

            //getStdDev(stmt, "CS101");
            //getGPA(stmt,1019);

            //getMinGrade(stmt, "CS101");


            //STEP 6: Clean-up environment
            //rs.close();
            //stmt.close();
            //conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            if(stmt!=null) {
                //stmt.close();
            }
            if(conn!=null) {
                //conn.close();
            }
        }//end try
        System.out.println("Goodbye!");

    } //end connect


    public static void getAllResults (Statement s) throws SQLException {
        System.out.println("Calling method getAllResults...");
        sql = "SELECT * FROM results";
        rs = s.executeQuery(sql);

        // extract data from result set
        while(rs.next()){
            //Retrieve by column name
            String student_num  = rs.getString("student_num");
            String module_code  = rs.getString("module_code");
            int ca_mark = rs.getInt("ca_mark");
            int final_exam_mark = rs.getInt("final_exam_mark");

            //Display values
            System.out.print("Student Number: " + student_num);
            System.out.print(", Module Code: " + module_code);
            System.out.print(", CS Mark: " + ca_mark);
            System.out.print(", Final Exam Mark: " + final_exam_mark);
            System.out.print(", Overall Grade: " + (ca_mark + final_exam_mark));
            System.out.println(", Pass/Fail?: " + (ca_mark + final_exam_mark >= PASS_MARK ? "Pass" : "Fail" ));
        }

    }

    public static void getAverageGrade (Statement s) throws SQLException {
        System.out.println("Calling method getAverageGrade...");
        sql = "SELECT DISTINCT module_code FROM results";
        rs = s.executeQuery(sql);
        ArrayList<String> modules = new ArrayList<String>();
        System.out.print("Please enter an individual module. The available modules are: ");
        while(rs.next()){
            modules.add(rs.getString("module_code"));
            System.out.print(rs.getString("module_code") + " ");
        }
        System.out.print("\n");
        Scanner scanner = new Scanner(System.in);
        String selected = scanner.next();

        while (! modules.contains(selected)) {
            System.out.println("Please re-enter a valid selection: ");
            selected = scanner.next();
        }

        sql = "SELECT AVG(ca_mark + final_exam_mark) AS average FROM results WHERE module_code = \"" + selected +"\"";
        rs = s.executeQuery(sql);
        rs.next();

        //Retrieve by column name
        String average  = rs.getString("average");
        System.out.println("Overall Average: " + average);

    }

    public static void getMaxGrade (Statement s, String module) throws SQLException {
        System.out.println("Calling method getMaxGrade...");
        sql = "SELECT MAX(ca_mark + final_exam_mark) AS max FROM results WHERE module_code = \"" + module +"\"";
        rs = s.executeQuery(sql);
        rs.next();

        //Retrieve by column name
        String max  = rs.getString("max");
        System.out.println("Highest Grade Achieved: " + max);
    }

    public static void getMinGrade (Statement s, String module) throws SQLException {
        System.out.println("Calling method getMinGrade...");
        sql = "SELECT MIN(ca_mark + final_exam_mark) AS min FROM results WHERE module_code = \"" + module +"\"";
        rs = s.executeQuery(sql);
        rs.next();

        //Retrieve by column name
        String min  = rs.getString("min");
        System.out.println("Lowest Grade Achieved: " + min);

    }

    public static void getStdDev (Statement s, String module) throws SQLException {
        System.out.println("Calling method getStdDev...");
        sql = "SELECT stddev(ca_mark + final_exam_mark) AS stddev FROM results WHERE module_code = \"" + module +"\"";
        rs = s.executeQuery(sql);
        rs.next();

        //Retrieve by column name
        double stddev  = rs.getDouble("stddev");

        // Round deviation to 3 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        System.out.println("Standard Deviation: " + df.format(stddev));

    }


    public static void checkGrades (Statement s) throws SQLException {

        Scanner scanner = new Scanner(System.in);
        int selected_student;
        boolean validSelection;
        do {
            System.out.println("Please enter the student number for whom data is required:");
            selected_student = scanner.nextInt();

            sql = "SELECT EXISTS (SELECT * FROM results WHERE student_num = " + selected_student + ");";
            rs = s.executeQuery(sql);
            rs.next();
            validSelection = rs.getString(1).equals("1");

        } while (! validSelection);

        System.out.println("Getting student grades information...");

        sql = "SELECT SUM(credit_weighting) AS sum FROM modules WHERE code IN " +
                "(SELECT module_code FROM results WHERE student_num = " + selected_student + ")";
        rs = s.executeQuery(sql);
        rs.next();
        int credits = rs.getInt(1);
        if (credits == 60) {
            System.out.println("Student grades received for correct number of credits");
        } else if (credits < 60) {
            System.out.println("Insufficient grades received for student");
        } else {
            System.out.println("Student appears to have grades for more than 60 credits");
        }
        System.out.println("System has results for " + credits + " credits for student " + selected_student);

        checkAllGradesPassed(s,selected_student);
    }

    public static void checkAllGradesPassed (Statement s, int student_num) throws SQLException {

        System.out.println("Checking grades...");
        sql = "SELECT module_code, ca_mark, final_exam_mark FROM results WHERE student_num = " + student_num + ";";
        rs = s.executeQuery(sql);
        boolean allPassed = true;
        // extract data from result set
        while(rs.next()){
            //Retrieve by column name
            String module_code  = rs.getString("module_code");
            int ca_mark = rs.getInt("ca_mark");
            int final_exam_mark = rs.getInt("final_exam_mark");

            //Display values
            System.out.print("Module Code: " + module_code);
            System.out.print(", CS Mark: " + ca_mark);
            System.out.print(", Final Exam Mark: " + final_exam_mark);
            System.out.print(", Overall Grade: " + (ca_mark + final_exam_mark));
            if ( ca_mark + final_exam_mark < PASS_MARK ) {
                allPassed = false;
            }
            System.out.println(", Pass/Fail?: " + (ca_mark + final_exam_mark >= PASS_MARK ? "Pass" : "Fail" ));

        }

        if (! allPassed) {
            System.out.println("Student " + student_num + " has not passed all modules");
            checkPassByCompensation(s,student_num);
        } else {
            System.out.println("Student " + student_num + " has passed all modules");
        }
    }

    private static void checkPassByCompensation(Statement s, int student_num) throws SQLException {
        System.out.println("Checking grades...");
        sql = "SELECT SUM(credit_weighting) AS sum FROM modules WHERE code IN " +
                "(SELECT module_code FROM results WHERE student_num = " + student_num + " AND ca_mark + results.final_exam_mark < " + PASS_MARK +")";

        rs = s.executeQuery(sql);
        rs.next();
        int creditsFailed = rs.getInt(1);
        System.out.println("Student " + student_num + " has failed " + creditsFailed + " credits");
        if (creditsFailed > 10 ) {
            System.out.println("Student has failed more than 10 credits and is therefore" +
                    " ineligible to pass the year by compensation");
        } else {

            sql = "SELECT module_code, ca_mark, final_exam_mark FROM results " +
                    "WHERE student_num = " + student_num + " AND ca_mark + final_exam_mark < " + PASS_MARK;
            rs = s.executeQuery(sql);

            boolean passByComp = true;
            // extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int ca_mark = rs.getInt("ca_mark");
                int final_exam_mark = rs.getInt("final_exam_mark");

                //Display values
                if ( ca_mark + final_exam_mark < COMPENSATION_MARK ) {
                    passByComp = false;
                    break;
                }
            }

            if ( passByComp ) {
                System.out.println("Student is eligible to pass the year by compensation");
            } else {
                System.out.println("Student is ineligible to pass the year by compensation due to receiving grade(s) below 30%");
            }

        }

    }

    public static void getGPA (Statement s, int student_num) throws SQLException {

        System.out.println("Checking grades...\n");
        sql = "SELECT results.module_code, results.ca_mark + results.final_exam_mark AS total_res, modules.credit_weighting FROM results " +
                "INNER JOIN modules ON results.module_code =modules.code WHERE student_num = " + student_num + ";";

        rs = s.executeQuery(sql);
        double totalGPA = 0.0;
        String gpGrade;
        double gpaValue;
        GPACalculator calc = new GPACalculator();

        // extract data from result set
        while (rs.next()) {
            //Retrieve by column name
            String module_code = rs.getString("module_code");
            int total_res = rs.getInt("total_res");
            int credits = rs.getInt("credit_weighting");

            //Display values
            System.out.print("Module Code: " + module_code);
            System.out.println(", Overall Grade: " + total_res);

            gpGrade = calc.getGPGrade(total_res);
            gpaValue = calc.getGPValue(total_res);

            System.out.print("Grade received: " + gpGrade);
            System.out.println(". GPA value: " + gpaValue +"\n");

            totalGPA += credits * calc.getCalculationPoint(total_res);
        }

        sql = "SELECT SUM(credit_weighting) AS sum FROM modules WHERE code IN " +
                "(SELECT module_code FROM results WHERE student_num = " + student_num + ");";

        rs = s.executeQuery(sql);
        rs.next();
        int totalCredits = rs.getInt("sum");
        //System.out.println( "TotalGPA: " + totalGPA );
        //System.out.println( "Total Credits: " + totalCredits );

        int resultGPA = (int) (totalGPA / totalCredits);

        System.out.println("Overall GPA: " + calc.getGPValue(resultGPA) );
    }

    public static void addUser (Statement s, String uname, String hpwd ) throws SQLException {
        System.out.println("Calling method addUser...");

        sql = "INSERT INTO users (userID, hashPswd) VALUES (' " + uname +"','" + hpwd + "');";
        //INSERT INTO users (userID, hashPswd) VALUES ('asd','asd');
        int outcome = s.executeUpdate(sql);

        System.out.println(outcome);
    }

} // end class
