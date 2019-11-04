package controller.Trajectory;

import controller.ProteinPrepToolException;

/*******************************************************************************
 *
 *	Filename   :	ProteinTools.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class holds static methods relating to the manipulation of Proteins.
 *	Currently holds one method, mean squared deviation. Subsequent methods
 *	should be added here.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class ProteinTools {


    public static double MeanSquaredDeviation(double[] structureA, double[] structureB) throws ProteinPrepToolException {

        if(structureA.length != structureB.length){
            throw new ProteinPrepToolException("Structures comprise different number of Atoms");
        }

        double total = 0;

        for(int i = 0; i < structureA.length; i++){
            total += ((structureA[i] - structureB[i]) * (structureA[i] - structureB[i]));
        }

        double atoms = structureA.length;

        return (1.0/atoms)*total;



    }

}
