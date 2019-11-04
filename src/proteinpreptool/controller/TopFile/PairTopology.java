package controller.TopFile;

import javafx.util.Pair;

/*******************************************************************************
 *
 *	Filename   :	PairTopology.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ pair ] part of a GROAMCS topology object. extends
 *	AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class PairTopology extends AtomInteraction implements Comparable<PairTopology> {


    public PairTopology() {
        super();
    }

    public PairTopology(int atomAI, int atomAJ, int funct, double charge0, double charge1) {
        super(atomAI, atomAJ, funct, charge0, charge1);
    }

    public PairTopology(int atomAI, int atomAJ, int funct) {
        super(atomAI, atomAJ, funct);
    }

    @Override
    public int compareTo(PairTopology pairTopology) {

        if(this.getAtomAI() == pairTopology.getAtomAI()){
            return this.getAtomAJ() - pairTopology.getAtomAJ();
        } else {
            return this.getAtomAI() - pairTopology.getAtomAI();
        }
    }
}
