package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	DiheadralTopology.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ diheadrals ] part of a GROAMCS topology object.
 *	extends AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class DiheadralTopology extends AtomInteraction{

    private int atomk;
    private int atomL;
    private double charge2;
    private boolean charge2Set;


    public DiheadralTopology() {
        super();
        this.atomk = -1;
        this.atomL = -1;
        this.charge2 = 0.0;
        this.charge2Set = false;
    }

    public DiheadralTopology(int atomAI, int atomAJ, int atomk, int atomL, int funct, double charge0, double charge1, double charge2) {
        super(atomAI, atomAJ, funct, charge0, charge1);
        this.atomk = atomk;
        this.atomL = atomL;
        this.charge2 = charge2;
        this.charge2Set = true;
    }


    public DiheadralTopology(int atomAI, int atomAJ, int atomk, int atomL) {
        super(atomAI, atomAJ, -1);
        this.atomk = atomk;
        this.atomL = atomL;
        this.chargesSet = false;
    }


    public DiheadralTopology(int atomAI, int atomAJ, int atomk, int atomL, int funct, double charge0, double charge1) {
        super(atomAI, atomAJ, funct, charge0, charge1);
        this.atomk = atomk;
        this.atomL = atomL;
        this.charge2 = -1;
        this.charge2Set = false;
    }

    public int getAtomAk() {
        return atomk;
    }

    public void setAtomAK(int atomk) {
        this.atomk = atomk;
    }

    public int getAtomAL() {
        return atomL;
    }

    public void setAtomAL(int atomL) {
        this.atomL = atomL;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        super.getStartOfTopFileLine(sb);
        sb.append(atomk).append("\t").append(atomL).append("\t");
        super.getEndOfTopFileLine(sb);

        if(charge2Set){
            sb.append(charge2);
        }

        return sb.toString();
    }

}
