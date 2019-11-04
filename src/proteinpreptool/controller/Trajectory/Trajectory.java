package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	Trajectory.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Abstract class, outlines the required layout of a "trajectory" object.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ProteinPrepToolException;

import java.util.ArrayList;

public abstract class Trajectory {


    protected int numberOfAtoms;
    protected int numberOfFrames;
    protected int numberOfFiles;
    protected ArrayList<String> fileNames;


    public Trajectory(){
        fileNames = new ArrayList<String>();
        numberOfAtoms = -1;
        numberOfFrames = 0;
        numberOfFiles = 0;
    }

    public abstract double[] getFrame(int frameId) throws ProteinPrepToolException;


    public int getNumberOfFrames(){
        return numberOfFrames;
    }

    public int getNumberOfAtoms() {
        return numberOfAtoms;
    }
}
