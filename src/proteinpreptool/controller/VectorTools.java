package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*******************************************************************************
 *
 *	Filename   :	VectorTools.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class provides some utilities for working with vectors. any additional
 *	vector tools required should be added here.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class VectorTools {


    private static class ElementMultiplier implements Runnable{

        double[] a, b, r;
        int start, end;

        public ElementMultiplier(){
            a = null;
            b = null;
            r = null;
            start = 0;
            end = 0;
        }

        public ElementMultiplier(double[] vectorA, double[] vectorB, double[] result, int start, int end) throws ProteinPrepToolException {
            if(vectorA == null || vectorB == null || result == null || start < 0 || start > vectorA.length || end < 0 || end < start || end > vectorA.length
             ||vectorA.length != vectorB.length || vectorB.length != result.length){
                throw new ProteinPrepToolException("Invalid multiplication config.");
            }

            this.a = vectorA;
            this.b = vectorB;
            this.r = result;
            this.start = start;
            this.end = end;
        }


        public void initELMult(double[] vectorA, double[] vectorB, double[] result, int start, int end) throws ProteinPrepToolException {
            if(vectorA == null || vectorB == null || result == null || start < 0 || start > vectorA.length || end < 0 || end < start || end > vectorA.length
                    ||vectorA.length != vectorB.length || vectorB.length != result.length){
                throw new ProteinPrepToolException("Invalid multiplication config.");
            }

            this.a = vectorA;
            this.b = vectorB;
            this.r = result;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {

            for (int i = start; i < end; i++){
                r[i] = a[i] * b[i];
            }

        }
    }


    private int processors;
    private int numThreads;
    private Thread[] threads;
    private ElementMultiplier[] elMults;

    public VectorTools(){
        processors = Runtime.getRuntime().availableProcessors();
        numThreads = (processors > ConsoleController.threadLimit) ? ConsoleController.threadLimit : processors;

        if(numThreads > 0){
             threads = new Thread[numThreads];
             elMults = new ElementMultiplier[numThreads];
            for(int i = 0; i < numThreads; i++){
                elMults[i] = new ElementMultiplier();
                threads[i] = new Thread(elMults[i]);
            }
        } else {
            ConsoleController.log.addMessage("Running in single threaded mode!");
        }
    }

    public double[] elementWiseVectorScalerPower(double[] vector, double power){


        double[] rtn = new double[vector.length];
        for(int i = 0; i < vector.length; i++){
            rtn[i] = Math.pow(vector[i], power);
        }

        return rtn;

    }

    public double[] elementWiseVectorVectorAddition(double[] vectorA, double[] vectorB) throws ProteinPrepToolException {


        if(vectorA.length != vectorB.length){
            throw new ProteinPrepToolException("Vector addition requries two vectors of identical length");
        }

        double[] rtn = new double[vectorA.length];
        for(int i = 0; i < vectorA.length; i++){
            rtn[i] = vectorA[i] + vectorB[i];
        }

        return rtn;

    }

    public static void saveVectorAsCSV(String path, double[] vector) throws ProteinPrepToolException {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            for (int row = 0; row < vector.length; row++) {
                writer.write(vector[row] + "");
                if (row + 1 < vector.length) {
                    writer.write(",");
                    writer.write(System.lineSeparator());
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Perform element wise multiplication between two vectors. Vectors must be the same length.
     * @param vectorA double vector
     * @param vectorB double vector
     * @return double[]
     * @throws ProteinPrepToolException :- thrown on dimension errors.
     */
    public double[] elementWiseVectorVectorMultiplier(double[] vectorA, double[] vectorB) throws ProteinPrepToolException {

        if(vectorA.length != vectorB.length) {
            throw new ProteinPrepToolException("Vectors are different lengths! Cannot multiply.");
        }

        //Questionable as to whether this is faster than single core. further investigation needed.
        int numThreads = (processors > ConsoleController.threadLimit) ? ConsoleController.threadLimit : processors;
        double[] rtn = new double[vectorA.length];

        if(numThreads == 0) {

            for (int i = 0; i < vectorA.length; i++) {
                rtn[i] = vectorA[i] * vectorB[i];
            }
        } else {
            int workPerThread =vectorA.length/numThreads;
            int finalThreadBonusWork = vectorA.length % numThreads;


            for(int i = 0; i < numThreads; i++){
                int start = workPerThread * i;
                int end = workPerThread * (i + 1);
                end = (i == numThreads-1) ? end + finalThreadBonusWork : end;

                elMults[i].initELMult(vectorA,vectorB,rtn,start,end);
                threads[i].run();
            }

            for (Thread t : threads){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        return rtn;
    }


    /**
     * Sums an array.
     * @param sum array to sum
     * @return total value of array.
     */
    public double sum(double[] sum){
        double total = 0;
        for(double d : sum){
            total+=d;
        }

        return total;
    }


    /**
     * Method that takes two arrays, an array of values, and a vector 3N in length representing a 3 x n matrix,
     * performs an element wise multiplication, and returns a 3 x n two dimensional array.
     * @param val vector of elements to multiply by/
     * @param x1y1z12 3N vector representing 2d array
     * @return double[][] 3 x N resulting matrix.
     * @throws ProteinPrepToolException thrown on dimension errors.
     */

    public double[][] convertTo3dAndElementWiseMult(double[] val, double[] x1y1z12) throws ProteinPrepToolException {

        if(val.length != x1y1z12.length/3){
            throw new ProteinPrepToolException("Invalid array sizes. Arguement 2 must be three times the length of Arg1");
        }

        double[][] rtn = new double[3][val.length];
        for (int i = 0; i < val.length; i++){

            rtn[0][i] = val[i] * x1y1z12[i * 3 + 0];
            rtn[1][i] = val[i] * x1y1z12[i * 3 + 1];
            rtn[2][i] = val[i] * x1y1z12[i * 3 + 2];
        }


        return rtn;

    }















}
