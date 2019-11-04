package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AtomInteraction.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Abstract class. Provides framework for each type of atom interaction
 *	modelled in GROMACS top file: AtomTopology, BondTopology,
 *	DefaultsTopology, DiheadralTopology & PairTopology.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public abstract class AtomInteraction {

    private int atomAI;
    private int atomAJ;
    private int funct;
    private double charge0;
    private double charge1;
    protected boolean chargesSet;

    public AtomInteraction(){
        atomAI = -1;
        atomAJ = -1;
        funct = -1;
        charge0 = 0.0;
        charge1 = 0.0;
        chargesSet = false;
    }

    public AtomInteraction(int atomAI, int atomAJ, int funct, double charge0, double charge1) {
        this.atomAI = atomAI;
        this.atomAJ = atomAJ;
        this.funct = funct;
        this.charge0 = charge0;
        this.charge1 = charge1;
        chargesSet = true;
    }

    public AtomInteraction(int atomAI, int atomAJ, int funct) {
        this.atomAI = atomAI;
        this.atomAJ = atomAJ;
        this.funct = funct;
        chargesSet = false;
    }


    public int getAtomAI() {
        return atomAI;
    }

    public void setAtomAI(int atomAI) {
        this.atomAI = atomAI;
    }

    public int getAtomAJ() {
        return atomAJ;
    }

    public void setAtomAJ(int atomAJ) {
        this.atomAJ = atomAJ;
    }

    public int getFunct() {
        return funct;
    }

    public void setFunct(int funct) {
        this.funct = funct;
    }

    public double getCharge0() {
        return charge0;
    }

    public void setCharge0(double charge0) {
        this.charge0 = charge0;
    }

    public double getCharge1() {
        return charge1;
    }

    public void setCharge1(double charge1) {
        this.charge1 = charge1;
    }

    public void getStartOfTopFileLine(StringBuilder sb){
        sb.append("\t").append(this.atomAI).append("\t").append(this.atomAJ).append("\t");

    }

    public void getEndOfTopFileLine(StringBuilder sb){
        if(funct >= 0)
            sb.append(this.funct).append("\t");

        if(chargesSet)
            sb.append(this.charge0).append("\t").append(this.charge1).append("\t");

    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        getStartOfTopFileLine(sb);
        getEndOfTopFileLine(sb);
        return sb.toString();


    }


}
