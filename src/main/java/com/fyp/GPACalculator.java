package com.fyp;

/**
 * Created by Neil on 18/01/2015.
 */
public class GPACalculator {
    double [] lowerbound =  {76.67, 73.33, 70.00, 66.67, 63.33, 60.00, 56.67, 53.33, 50.00, 46.67, 43.33,
            40.00, 36.67, 33.33, 30.00, 26.67, 23.33, 20.00, 16.67, 13.33, 00.02, 00.00 };

    double [] calcPoint =   {78.33, 75.00, 71.67, 68.33, 65.00, 61.67, 58.33, 55.00, 51.67, 48.33, 45.00,
            41.67, 38.33, 35.00, 31.67, 28.33, 25.00, 21.67, 18.33, 15.00, 11.67, 00.00 };

    String[] grades =       { "A+",   "A",  "A-",  "B+",   "B",  "B-",  "C+",   "C",  "C-",  "D+",   "D",
            "D-",  "E+",   "E",  "E-",  "F+",   "F",  "F-",  "G+",   "G",  "G-",  "NG" };

    double[] gpValues =     {  4.2,   4.0,   3.8,   3.6,   3.4,   3.2,   3.0,   2.8,   2.6,   2.4,   2.2,
            2.0,   1.6,   1.6,   1.6,   1.0,   1.0,   1.0,   0.4,   0.4,   0.4,   0.0 };

    public double getLowerBound(int percentage) {
        return calcPoint[getIndex(percentage)];
    }

    public double getCalculationPoint(int percentage) {
        return calcPoint[getIndex(percentage)];
    }

    public String getGPGrade(int percentage) {
        return grades[getIndex(percentage)];
    }

    public double getGPValue(int percentage) {
        return gpValues[getIndex(percentage)];
    }

    public int getIndex (int percentage) {
        int index;
        for (index = 0; index < lowerbound.length; index++) {
            if (percentage >= lowerbound[index]) {
                return index;
            }
        }
        return index;
    }

       /* int index;
        if ( percentage >= 76.67 ) {
            index = 0;
        } else if ( percentage >= 73.33 ) {
            index = 1;
        } else if ( percentage >= 70.00 ) {
            index = 2;
        } else if ( percentage >= 66.67 ) {
            index = 3;
        } else if ( percentage >= 63.33 ) {
            index = 4;
        } else if ( percentage >= 60.00 ) {
            index = 5;
        } else if ( percentage >= 56.67 ) {
            index = 6;
        } else if ( percentage >= 53.33 ) {
            index = 7;
        } else if ( percentage >= 50.00 ) {
            index = 8;
        } else if ( percentage >= 46.67 ) {
            index = 9;
        } else if ( percentage >= 43.33 ) {
            index = 10;
        } else if ( percentage >= 40.00 ) {
            index = 11;
        } else if ( percentage >= 36.67 ) {
            index = 12;
        } else if ( percentage >= 33.33 ) {
            index = 13;
        } else if ( percentage >= 30.00 ) {
            index = 14;
        } else if ( percentage >= 26.67 ) {
            index = 15;
        } else if ( percentage >= 23.33 ) {
            index = 16;
        } else if ( percentage >= 20.00 ) {
            index = 17;
        } else if ( percentage >= 16.67 ) {
            index = 18;
        } else if ( percentage >= 13.33 ) {
            index = 19;
        } else if ( percentage >= 00.02 ) {
            index = 20;
        } else {
            index = 21;
        }

        return index;
    }*/

}