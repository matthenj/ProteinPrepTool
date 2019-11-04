package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AngleTopology
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ angles ] part of a GROAMCS topology object.
 *	extends AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.Int4;

public class AngleTopology extends AtomInteraction{

    private int atomAK;

    public AngleTopology(){
        super();
        atomAK = -1;
    }

    public AngleTopology(int atomAI, int atomAJ, int atomAK, int funct, double charge0, double charge1) {
        super(atomAI, atomAJ, funct, charge0, charge1);
        this.atomAK = atomAK;
    }

    public AngleTopology(int atomAI, int atomAJ, int atomAK) {
        super(atomAI, atomAJ,-1);
        this.atomAK = atomAK;
    }

    public AngleTopology(Int4 atoms, int funct, double charge0, double charge1) {
        super(atoms.x, atoms.y, funct, charge0, charge1);
        this.atomAK = atoms.z;
    }


    public void setAtomAK(int atomk) {
        this.atomAK = atomk;
    }

    public int getAtomAK() {
        return atomAK;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        super.getStartOfTopFileLine(sb);
        sb.append(this.atomAK).append("\t");
        super.getEndOfTopFileLine(sb);

        return sb.toString();

    }




}
