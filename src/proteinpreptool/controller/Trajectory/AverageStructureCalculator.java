package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	AverageStructureCalculator.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Determines the average coordinate position of each atom, taken across
 *	all frames added to it.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ProteinPrepToolException;

import java.util.Arrays;

public class AverageStructureCalculator {


    private double[] summedValues;
    private long numberAdded;
    private double[] cachedAverageStructure;
    private long numberAddedWhenCached;

    private AverageStructureCalculator(){


    }

    public  AverageStructureCalculator(int numberOfAtoms){
        summedValues = new double[numberOfAtoms * 3];
        numberAdded = 0;
        numberAddedWhenCached = 0;

        for (int i = 0; i < summedValues.length; i++){
            summedValues[i] = 0.0;
        }

    }


    public void addPose(double[] newPose) throws ProteinPrepToolException {

        if(summedValues.length != newPose.length){
            throw new ProteinPrepToolException("Structure size differs from expected.");
        }


        for (int i = 0; i < newPose.length; i++){
            summedValues[i] += newPose[i];
        }

        numberAdded++;

    }

    public double[] getAverageStructure(){

        if(numberAddedWhenCached == numberAdded){
            return Arrays.copyOf(cachedAverageStructure, cachedAverageStructure.length);
        }

        cachedAverageStructure = new double[summedValues.length];
        numberAddedWhenCached = numberAdded;

        for (int i = 0; i <cachedAverageStructure.length; i++){
            cachedAverageStructure[i] = summedValues[i] / numberAdded;
        }

        return Arrays.copyOf(cachedAverageStructure, cachedAverageStructure.length);

    }


}
