package com.fyp;

/**
 * Created by Neil on 18/01/2015.
 */
public class GradeCalculator {
    static double [] lowerbound =  {76.67, 73.33, 70.00, 66.67, 63.33, 60.00, 56.67, 53.33, 50.00, 46.67, 43.33,
            40.00, 36.67, 33.33, 30.00, 26.67, 23.33, 20.00, 16.67, 13.33, 00.02, 00.00 };

    static double [] calcPoint =   {78.33, 75.00, 71.67, 68.33, 65.00, 61.67, 58.33, 55.00, 51.67, 48.33, 45.00,
            41.67, 38.33, 35.00, 31.67, 28.33, 25.00, 21.67, 18.33, 15.00, 11.67, 00.00 };

    static String[] grades =       { "A+",   "A",  "A-",  "B+",   "B",  "B-",  "C+",   "C",  "C-",  "D+",   "D",
            "D-",  "E+",   "E",  "E-",  "F+",   "F",  "F-",  "G+",   "G",  "G-",  "NG" };

    static double[] gpValues =     {  4.2,   4.0,   3.8,   3.6,   3.4,   3.2,   3.0,   2.8,   2.6,   2.4,   2.2,
            2.0,   1.6,   1.6,   1.6,   1.0,   1.0,   1.0,   0.4,   0.4,   0.4,   0.0 };

    static String[] awards = { "1H", "2H1", "2H2", "3H", "Pass", "Fail" };

    public static double getLowerBound(int percentage) {
        return calcPoint[getIndex(percentage)];
    }

    public static double getCalculationPoint(int percentage) {
        return calcPoint[getIndex(percentage)];
    }

    public static String getGPGrade(double percentage) {
        return grades[getIndex(percentage)];
    }

    public static double getGPValue(int percentage) {
        return gpValues[getIndex(percentage)];
    }

    public static int getIndex (int percentage) {
        int index;
        for (index = 0; index < lowerbound.length; index++) {
            if (percentage >= lowerbound[index]) {
                return index;
            }
        }
        return index;
    }

    public static int getIndex (double percentage) {
        int index;
        for (index = 0; index < lowerbound.length; index++) {
            if (percentage >= lowerbound[index]) {
                return index;
            }
        }
        return index;
    }

    public static String getAward (double percentage) {

        String award;
        if (percentage >= 70) {
            award = awards[0];
        } else if (percentage >= 60) {
            award = awards[1];
        } else if (percentage >= 50) {
            award = awards[2];
        } else if (percentage >= 45) {
            award = awards[3];
        } else if (percentage >= 40) {
            award = awards[4];
        }  else {
            award = awards[5];
        }

        return award;
    }

}