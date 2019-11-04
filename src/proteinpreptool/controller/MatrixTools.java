package controller;

import org.apache.commons.math3.linear.RealMatrix;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*******************************************************************************
 *
 *	Filename   :	MatrixTools.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class provides a couple of save to file methods for use with matrices. Any
 *	additional matrix oriented methods should be added to this class.
 *	Better to use libraries were possible.
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/


public class MatrixTools {

    public static void saveMatrixAsCSV(String path, RealMatrix matrix) throws ProteinPrepToolException {
        saveMatrixAsCSV(path, matrix.getData(), false);
    }

    public static void saveMatrixDiagonalAsCSV(String path, RealMatrix matrix) throws ProteinPrepToolException {
        saveMatrixAsCSV(path, matrix.getData(), true);
    }

    public static void saveMatrixDiagonalAsCSV(String path, double[][] matrix) throws ProteinPrepToolException {
        saveMatrixAsCSV(path, matrix, true);
    }

    public static void saveMatrixAsCSV(String path, double[][] matrix) throws ProteinPrepToolException {
        saveMatrixAsCSV(path, matrix, false);
    }

    private static void saveMatrixAsCSV(String path, double[][] matrix, boolean saveDiagonal)
            throws ProteinPrepToolException {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            if(saveDiagonal) {

                for (int i = 0; i < matrix.length; i++) {
                    if(matrix.length != matrix[i].length){
                        throw new ProteinPrepToolException("Must be a square matrix, in order to print diagonal.");
                    }
                }

                for (int i = 0; i < matrix.length; i++) {
                        writer.write(matrix[i][i] + System.lineSeparator());
                }
            }else {
                for (int rowI = 0; rowI < matrix.length; rowI++) {
                    for (int colI = 0; colI < matrix[rowI].length; colI++) {
                        writer.write(matrix[rowI][colI] + "");
                        if (colI + 1 < matrix[rowI].length)
                            writer.write(",");
                    }
                    writer.write(System.lineSeparator());
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
