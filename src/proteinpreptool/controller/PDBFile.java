package controller;

import java.util.ArrayList;
import java.util.Arrays;

/*******************************************************************************
 *
 *	Filename   :	PDBFile.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class representing a basic PDBFile. Only ATOM and HETATM lines are
 *	modelled here, as those are the only lines required by Haptimol
 *	FlexiDock
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class PDBFile {

    private ArrayList<Atom> atoms;
    private double[] coordsAsVector;
    private ArrayList<String> cachedResidueList;
    private ArrayList<Int2> residueStartPoints;
    private int numberOfHydrogenAtoms;

    public PDBFile() {
        atoms = new ArrayList();
        cachedResidueList = null;
        residueStartPoints = null;
        numberOfHydrogenAtoms = 0;
        coordsAsVector = null;
    }

    public PDBFile(PDBFile other, double[] coords) throws ProteinPrepToolException {

        atoms = new ArrayList<Atom>();
        for (int i = 0; i < other.getNumberOfAtoms(); i++) {
            atoms.add(new Atom(other.getAtom(i), coords[i * 3], coords[i * 3 + 1], coords[i * 3 + 2]));
        }
        this.numberOfHydrogenAtoms = other.numberOfHydrogenAtoms;
        this.coordsAsVector = Arrays.copyOf(coords, coords.length);

    }


    public void addAtom(Atom atm) {
        atoms.add(atm);
        numberOfHydrogenAtoms = atm.getElement().equalsIgnoreCase("H") ? numberOfHydrogenAtoms + 1 : numberOfHydrogenAtoms;
    }


    public Atom getAtom(int i) throws ProteinPrepToolException {

        if (i >= atoms.size()) {
            throw new ProteinPrepToolException("Atom not found");
        }

        if (i < 0) {
            throw new ProteinPrepToolException("Negative index not allowed.");
        }

        return atoms.get(i);
    }

    public int getNumberOfAtoms() {
        return atoms.size();
    }

    public int getNumberOfHydrogenAtoms() {
        return numberOfHydrogenAtoms;
    }

    /**
     * Returns a string representation of this PDBFile, as it would be in a file, ready for writing.
     * @return String containing PDB file.
     */
    public String generateStringForPrinting() {

        StringBuilder sb = new StringBuilder();
        sb.append("REMARK\t").append("Generated with ProteinPrepTool").append(System.lineSeparator());
        sb.append("MODEL\t1").append(System.lineSeparator());
        for (Atom acv : atoms) {
            sb.append(acv.formatAsPDB()).append(System.lineSeparator());
        }
        sb.append("ENDMDL").append(System.lineSeparator());
        sb.append("END");

        return sb.toString();

    }

    /**
     * Gets a copy of the atom coordinates from PDB file in a single vector, structured (x1,y1,z1,x2,y2,z2....x(n-1)y(n-1)z(n-1) where
     * n is the number of atoms.
     *
     * @return vector as described.
     */
    public double[] getCoordsAsVector() {

        //generate coordinates if requested.
        if (this.coordsAsVector == null) {
            coordsAsVector = new double[this.getNumberOfAtoms() * 3];

            int writeIndex = 0;
            for (Atom atm : atoms) {
                coordsAsVector[writeIndex++] = atm.getXPosition();
                coordsAsVector[writeIndex++] = atm.getYPosition();
                coordsAsVector[writeIndex++] = atm.getZPosition();
            }
        }
        return coordsAsVector.clone();
    }

    private void prepareCachedResidueInfo() {

        this.cachedResidueList = new ArrayList<>();
        this.residueStartPoints = new ArrayList<>();

        String currentResidue = "";
        int previousId = -1;

        for (int i = 0; i < atoms.size(); i++) {
            Atom atm = atoms.get(i);
            if (!(currentResidue.equalsIgnoreCase(atm.getResidueID()))) {
                currentResidue = atm.getResidueID();
                cachedResidueList.add(currentResidue);
                if (previousId >= 0) {
                    this.residueStartPoints.get(previousId).y = i;
                }
                previousId++;
                this.residueStartPoints.add(new Int2(i, 0));
            }
        }
    }

    /**
     * Gets a list of residues in this PDB file.
     * @return ArrayList of Strings. Each string describes a residue type.
     */
    public ArrayList<String> getListOfResiudes() {

        if (cachedResidueList == null) {
            prepareCachedResidueInfo();
        }

        return this.cachedResidueList;
    }


    /**
     * Returns list of atoms in residue i.
     * @param i - residue to retrieve. (Count from 0).
     * @return ArrayList<Atom> - atoms in residue i
     * @throws  ProteinPrepToolException - if 'i' is beyond the bounds of the array.
     */
    public ArrayList<Atom> getResidue(int i) throws ProteinPrepToolException {

        if (this.residueStartPoints == null) {
            prepareCachedResidueInfo();
        }

        if(i >= residueStartPoints.size() || i < 0){
            throw new ProteinPrepToolException("Invalid residue ID");
        }

        Int2 resStartPoint = this.residueStartPoints.get(i);
        ArrayList<Atom> tmp = new ArrayList<>();

        for (int id = resStartPoint.x; id < resStartPoint.y; id++) {
            tmp.add(new Atom(this.atoms.get(id)));
        }

        return tmp;

    }


}
