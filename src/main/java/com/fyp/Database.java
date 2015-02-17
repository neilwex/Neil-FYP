package com.fyp;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by Neil on 15/01/2015.
 */
public class Database {

    // JDBC driver name and database URL
    protected static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/results_db";

    // Database credentials
    protected static final String USER = "root";
    protected static final String PASS = "Wexford96-";

    protected static final int PASS_MARK = 40;
    protected static final int COMPENSATION_MARK = 30;

    private static ResultSet rs;
    private static String sql;

    private static Connection conn = null;
    private static PreparedStatement ps = null;

    protected static void setupConnection() throws SQLException {

        if (conn == null || !conn.isClosed()) {

            try {
                // Register JDBC driver
                Class.forName(JDBC_DRIVER);

                // Open a connection
                System.out.println("Connecting to database...");
                conn = DriverManager.getConnection(DB_URL, USER, PASS);

            } catch (SQLException se) {
                se.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    } //end connect

    protected static void closeConnection() throws SQLException {

        //Clean-up environment
        if (! rs.isClosed() ){
            rs.close();
        }
        if (! ps.isClosed() ) {
            ps.close();
        }
        if (! conn.isClosed() ) {
            ps.close();
        }

        System.out.println("Connection closed");
    }

    public static void getAllResults() throws SQLException {

        setupConnection();
        System.out.println("Calling method getAllResults...");
        sql = "SELECT * FROM results";
        ps = conn.prepareStatement(sql);
        rs = ps.executeQuery(sql);

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

    public static void getAverageGrade(String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getAverageGrade...");

        sql = "SELECT AVG(ca_mark + final_exam_mark) AS average FROM results WHERE module_code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        String average  = rs.getString("average");
        System.out.println("Overall Average: " + average);
    }

    public static void getMaxGrade (String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getMaxGrade...");
        sql = "SELECT MAX(ca_mark + final_exam_mark) AS max FROM results WHERE module_code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        String max  = rs.getString("max");
        System.out.println("Highest Grade Achieved: " + max);
    }

    public static void getMinGrade (String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getMinGrade...");

        sql = "SELECT MIN(ca_mark + final_exam_mark) AS min FROM results WHERE module_code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, "CS101");
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        String min  = rs.getString("min");
        System.out.println("Lowest Grade Achieved: " + min);
    }

    public static void getStdDev (String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getStdDev...");

        sql = "SELECT stddev(ca_mark + final_exam_mark) AS stddev FROM results WHERE module_code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        double stddev  = rs.getDouble("stddev");

        // Round deviation to 3 decimal points
        DecimalFormat df = new DecimalFormat("#.##");

        System.out.println("Standard Deviation: " + df.format(stddev));

    }

    public static void checkGrades(int student) throws SQLException {

        System.out.println("Getting student grades information...");

        sql = "SELECT SUM(credit_weighting) AS sum FROM modules WHERE code IN " +
                "(SELECT module_code FROM results WHERE student_num = ?)";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, student);
        rs = ps.executeQuery();
        rs.next();

        int credits = rs.getInt(1);
        if (credits == 60) {
            System.out.println("Student grades received for correct number of credits");
        } else if (credits < 60) {
            System.out.println("Insufficient grades received for student");
        } else {
            System.out.println("Student appears to have grades for more than 60 credits");
        }
        System.out.println("System has results for " + credits + " credits for student " + student);

        checkAllGradesPassed(student);
    }

    public static void checkAllGradesPassed (int student_num) throws SQLException {

        System.out.println("Checking grades...");
        sql = "SELECT module_code, ca_mark, final_exam_mark FROM results WHERE student_num = ?";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, student_num);
        rs = ps.executeQuery();

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
            checkPassByCompensation(student_num);
        } else {
            System.out.println("Student " + student_num + " has passed all modules");
        }
    }

    protected static void checkPassByCompensation(int student_num) throws SQLException {

        System.out.println("Checking grades...");
        sql = "SELECT SUM(credit_weighting) AS sum FROM modules WHERE code IN " +
                "(SELECT module_code FROM results WHERE student_num = ? AND ca_mark + results.final_exam_mark < ?)";

        ps = conn.prepareStatement(sql);
        ps.setInt(1, student_num);
        ps.setInt(2, PASS_MARK);
        rs = ps.executeQuery();
        rs.next();

        int creditsFailed = rs.getInt("sum");
        System.out.println("Student " + student_num + " has failed " + creditsFailed + " credits");
        if (creditsFailed > 10 ) {
            System.out.println("Student has failed more than 10 credits and is therefore" +
                    " ineligible to pass the year by compensation");
        } else {

            sql = "SELECT module_code, ca_mark, final_exam_mark FROM results " +
                     "WHERE student_num = ? AND ca_mark + final_exam_mark < ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, student_num);
            ps.setInt(2, PASS_MARK);
            rs = ps.executeQuery();

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

    public static void getGPA (int student_num) throws SQLException {

        System.out.println("Checking grades...\n");
        sql = "SELECT results.module_code, results.ca_mark + results.final_exam_mark AS total_res, modules.credit_weighting FROM results " +
                "INNER JOIN modules ON results.module_code =modules.code WHERE student_num = ?";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, student_num);
        rs = ps.executeQuery();

        double totalGPA = 0.0;
        String gpGrade;
        double gpaValue;
        GradeCalculator calc = new GradeCalculator();

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
                "(SELECT module_code FROM results WHERE student_num = ?)";

        ps = conn.prepareStatement(sql);
        ps.setInt(1, student_num);
        rs = ps.executeQuery();

        rs.next();
        int totalCredits = rs.getInt("sum");
        //System.out.println( "TotalGPA: " + totalGPA );
        //System.out.println( "Total Credits: " + totalCredits );

        int resultGPA = (int) (totalGPA / totalCredits);

        System.out.println("Overall GPA: " + calc.getGPValue(resultGPA) );
    }

    /**
     * Create new user in database with given credentials
     * @param newUser new username entered by user
     * @param newPassword new password entered by user
     * @return true if new user is successfully created, otherwise false
     * @throws SQLException, NoSuchAlgorithmException
     */
    public static boolean createUser(String newUser, String forename, String surname, String newPassword)
            throws SQLException, NoSuchAlgorithmException, UnsupportedEncodingException {

        setupConnection();
        System.out.println("Creating new user...");

        // Uses a secure Random not a simple Random
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        // 64 bit long salt
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        // create hash with newPassword and salt
        byte[] computedHash = createHash(newPassword, salt);

        // convert to Base64
        String base64ComputedHash = byteToBase64(computedHash);
        String base64Salt = byteToBase64(salt);

        // add new details to database
        ps = conn.prepareStatement("INSERT INTO users (userID, forename, surname, salt, hashPswd) VALUES (?,?,?,?,?)");
        ps.setString(1, newUser);
        ps.setString(2, forename);
        ps.setString(3, surname);
        ps.setString(4, base64Salt);
        ps.setString(5, base64ComputedHash);

        // executeUpdate returns 0 if unsuccessful
        return ps.executeUpdate() > 0;
    }

    /**
     * Creates a hash given a password and salt value
     * @param password  password provided by user
     * @param salt      salt value
     * @return byte[]   the password hash to be stored
     * @throws NoSuchAlgorithmException     If algorithm doesn't exist
     * @throws UnsupportedEncodingException
     */
    public static byte[] createHash(String password, byte[] salt)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(salt);
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        for (int i = 0; i < 1000; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }
        return hash;
    }

    /**
     * From a byte[] returns a base 64 representation
     * @param data byte[]
     * @return String
     * @throws java.io.IOException
     */
    public static String byteToBase64(byte[] data){
        BASE64Encoder endecoder = new BASE64Encoder();
        return endecoder.encode(data);
    }

    /**
     * From a base 64 representation, returns the corresponding byte[]
     * @param data String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(data);
    }

    /**
     * Attempt database login with given credentials
     * @param enteredUser username entered by user
     * @param enteredPassword password entered by user
     * @return true if login is successful, otherwise false
     * @throws SQLException
     */
    public static boolean attemptLogin(String enteredUser, String enteredPassword) throws SQLException {
        setupConnection();

        try {
            //VALIDATE USER INPUT

            // fetch enteredUser details (if existing) from database
            ps = conn.prepareStatement("SELECT hashPswd, salt FROM users WHERE userID = ?");
            ps.setString(1, enteredUser);
            rs = ps.executeQuery();

            String storedHash, storedSalt;
            if (! rs.next()) { // no user found with entered userID
                System.out.println("No user found with given userID");
                return false;
            } else { // user found with entered userID

                //get stored hash and salt values
                storedHash = rs.getString("hashPswd");
                storedSalt = rs.getString("salt");

                // DATABASE VALIDATION
                if (storedHash == null || storedSalt == null) {
                    throw new SQLException("Database inconsistent Salt or Digested Password altered");
                }
            }

            // convert to base64
            byte[] base64StoredHash = base64ToByte(storedHash);
            byte[] base64StoredSalt = base64ToByte(storedSalt);

            // create hash with enteredPassword and base64StoredSalt
            byte[] computedHash = createHash(enteredPassword, base64StoredSalt);

            System.out.println("Now comparing byte arrays");
            //compare computedHash with base64StoredHash
            return Arrays.equals(computedHash, base64StoredHash);

        } catch (IOException ex){
            throw new SQLException("Database inconsistant Salt or Digested Password altered");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ne) {
            ne.printStackTrace();
        }

        return false;
    }

    public static ResultSet getList (String query) throws SQLException {

        setupConnection();
        System.out.println("Calling method getList...");

        ps = conn.prepareStatement(query);
        return ps.executeQuery();
    }

    public static ResultSet getModuleStats(int accountID) throws SQLException {

        setupConnection();
        System.out.println("Calling method getModuleStats...");

        sql = "SELECT code, name, credit_weighting, ca_mark_percentage, final_exam_percentage, approved, COUNT(results.student_num) AS num_results" +
                " FROM modules LEFT JOIN results ON results.module_code = modules.code WHERE accountID = ? GROUP BY code";
        ps = conn.prepareStatement(sql);
        ps.setInt(1, accountID);
        return ps.executeQuery();
    }

    public static ResultSet getModuleStats(String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getModuleStats...");

        sql = "SELECT credit_weighting, ca_mark_percentage, final_exam_percentage" +
                " FROM modules WHERE code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        return ps.executeQuery();
    }

    public static ResultSet getModuleResults(String module) throws SQLException {
        setupConnection();
        System.out.println("Calling method getModuleResults...");

        sql = "SELECT * FROM ( " +
                "SELECT student_num, ca_mark, final_exam_mark, (ca_mark + final_exam_mark) AS total, rank FROM " +
                  "(SELECT ca_mark, final_exam_mark, student_num, " +
                    "@curRank := IF(@prevRank = (ca_mark + final_exam_mark), @curRank, @incRank) AS rank, " +
                    "@incRank := @incRank + 1, " +
                    "@prevRank := ca_mark + final_exam_mark " +
                  "FROM results p, ( SELECT @curRank :=0, @prevRank := NULL, @incRank := 1) r " +
                "WHERE module_code = ? " +
                "ORDER BY ca_mark + final_exam_mark DESC) s" +
              ") q ORDER BY student_num";

        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        return ps.executeQuery();
    }

    public static ResultSet getModuleAverages(String code) throws SQLException {

        setupConnection();
        System.out.println("Calling method getModuleResults...");

        sql = "SELECT AVG(ca_mark) AS ca, AVG(final_exam_mark) AS exam, AVG(ca_mark + final_exam_mark) AS total," +
                "STDDEV(ca_mark + final_exam_mark) AS stddev, MIN(ca_mark+final_exam_mark) AS min, MAX(ca_mark+final_exam_mark) AS max " +
                "FROM results WHERE module_code = ?";

        ps = conn.prepareStatement(sql);
        ps.setString(1, code);
        return ps.executeQuery();
    }


    public static int getNumStudents(String module) throws SQLException {

        setupConnection();
        System.out.println("Calling method getNumStudents...");

        sql = "SELECT COUNT(student_num) AS count FROM results WHERE module_code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, module);
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        return rs.getInt("count");
    }

    public static int getCredits(String code) throws SQLException {

        setupConnection();
        System.out.println("Calling method getCredits...");

        sql = "SELECT credit_weighting FROM modules WHERE code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, code);
        rs = ps.executeQuery();
        rs.next();

        //Retrieve by column name
        return rs.getInt(1);
    }

    public static boolean readCsvFile(String filename, String module){

        try {
            setupConnection();

            //put filename in correct format
            String editedFilename = filename.replace("\\", "\\\\");

            sql = "LOAD DATA LOCAL INFILE '" + editedFilename + "' INTO TABLE results FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES (student_num, ca_mark, final_exam_mark) SET module_code = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, module);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean addNewModule(String code, String title, String credits, String ca, String exam, int accountID) throws SQLException {

        setupConnection();
        System.out.println("Calling method addNewModule...");

        sql = "INSERT INTO modules VALUES (?,?,?,?,?,?,0);";
        ps = conn.prepareStatement(sql);
        ps.setString(1, code);
        ps.setString(2, title);
        ps.setInt(3, Integer.parseInt(credits));
        ps.setInt(4, Integer.parseInt(ca));
        ps.setInt(5, Integer.parseInt(exam));
        ps.setInt(6, accountID);

        // executeUpdate returns 0 if unsuccessful
        return ps.executeUpdate() > 0;
    }

    public static boolean checkModuleCode(String moduleCode) throws SQLException {

        setupConnection();
        System.out.println("Calling method addNewModule...");

        sql = "SELECT COUNT(code) AS count FROM modules WHERE code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, moduleCode);
        rs = ps.executeQuery();
        rs.next();

        // rs.getInt(1) returns the number of modules existing with code moduleCode
        return rs.getInt(1) > 0;
    }

    /*public static ResultSet getResults(String module) {
        return null;
    }*/

    public static boolean checkUserExists(String username) throws SQLException {

        setupConnection();
        System.out.println("Calling method checkUserExists...");

        sql = "SELECT COUNT(*) AS count FROM users WHERE userID = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        rs = ps.executeQuery();
        rs.next();

        // rs.getInt(1) will be greater than 0 if username already exists
        return rs.getInt(1) > 0;
    }

    public static ResultSet getPendingModules() throws SQLException {

        setupConnection();
        System.out.println("Calling method getPendingModules...");

        sql = "SELECT code, name, credit_weighting, ca_mark_percentage, final_exam_percentage," +
                "(CONCAT(users.forename, \" \", users.surname)) AS lecturer " +
                "FROM modules JOIN users ON modules.accountID = users.accountID  WHERE approved = FALSE;";
        ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }

    public static boolean approveModule(String code) throws SQLException {

        setupConnection();
        System.out.println("Calling method approveModule...");

        sql = "UPDATE modules SET approved = TRUE WHERE code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, code);

        // executeUpdate returns 0 if unsuccessful
        return ps.executeUpdate() > 0;
    }

    public static boolean deleteModule(String code) throws SQLException {

        setupConnection();
        System.out.println("Calling method deleteModule...");

        sql = "DELETE FROM modules WHERE code = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, code);

        // executeUpdate returns 0 if unsuccessful
        return ps.executeUpdate() > 0;
    }
} // end class