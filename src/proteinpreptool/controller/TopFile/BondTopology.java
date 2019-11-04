package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	BondTopology.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ bonds ] part of a GROAMCS topology object. extends
 *	AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.Int3;


public class BondTopology extends AtomInteraction {


    public BondTopology(){
        super();
    }

    public BondTopology(int atomAI, int atomAJ){
        super(atomAI, atomAJ, -1);
    }


    public BondTopology(int atomAI, int atomAJ, int funct, double charge0, double charge1) {
        super(atomAI, atomAJ, funct, charge0, charge1);
    }


    public BondTopology(Int3 data, int funct, double charge0, double charge1) {
        super(data.x, data.y, funct, charge0, charge1);
    }


}
