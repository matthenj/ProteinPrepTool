package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	DefaultsTopology.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ defaults ] part of a GROAMCS topology object.
 *	extends AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class DefaultsTopology {

    private int nbFunction;
    private int combRule;
    private boolean genPairs;
    private double fudgeLJ;
    private double fudgeQQ;


    public DefaultsTopology(){
        nbFunction = 1;
        combRule = 1;
        genPairs = false;
        fudgeLJ = 1.0;
        fudgeQQ = 1.0;
    }

    public DefaultsTopology(int nbFunction, int combRule, boolean genPairs, double fudgeLJ, double fudgeQQ) {
        this.nbFunction = nbFunction;
        this.combRule = combRule;
        this.genPairs = genPairs;
        this.fudgeLJ = fudgeLJ;
        this.fudgeQQ = fudgeQQ;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(nbFunction).append("\t").append(combRule).append("\t").append((genPairs?"yes":"no"))
                .append("\t").append(fudgeLJ).append("\t").append(fudgeQQ);

        return sb.toString();

    }
}
