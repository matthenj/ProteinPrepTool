package controller.forcefields;

import controller.ConsoleController;
import controller.ProteinPrepToolException;

/*******************************************************************************
 *
 *	Filename   :	AtomType.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class models a single line of a GROMACS forcefield [ AtomTypes ] section.
 *
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class AtomType {

    //when an "Atomtypes" line from a ffnonbonded.itp file is "split," these indicies will map the parts of the
    //resulting string array into the required class value.
    private static final int NAME_INDEX = 0;
    private static final int ATOM_NUMBER_INDEX = 1;
    private static final int MASS_INDEX = 2;
    private static final int CHARGE_INDEX = 3;
    private static final int PYTPE_INDEX = 4;
    private static final int SIGMA_C6_INDEX = 5;
    private static final int EPS_C12_INDEX = 6;

    private int orgOrder;
    private String name;
    private int atomNumber;
    private double mass;
    private double charge;
    private String ptype;
    private double sigma_c6; //can contain sigma OR c6 value, depending on forcefield.
    private double eps_c12; //can contain eps OR c12, depending on forcefield;


    private AtomType() {

    }

    public AtomType(int orgorder, String name, int atomNumber, double mass, double charge, String ptype, double sigma_c6, double eps_c12) {

        this.orgOrder = orgorder;
        this.name = name;
        this.atomNumber = atomNumber;
        this.mass = mass;
        this.charge = charge;
        this.ptype = ptype;
        this.sigma_c6 = sigma_c6;
        this.eps_c12 = eps_c12;

    }

    /**
     * Method takes a line from a forcefield file, and if it fits the required format, creates an "AtomType" with it.
     * <p>
     * If the line is not compatible, a ProteinPrepToolException is thrown.
     *
     * Index should indicate the order of the line, as loaded from the file.
     *
     * @param forceFieldFileLine Line from forcefield
     * @throws ProteinPrepToolException If line is not describing an atom type.
     */
    public AtomType(int index, String forceFieldFileLine) throws ProteinPrepToolException {


        if (forceFieldFileLine.length() < 1) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Invalid line");
        }

        String cleanLine = forceFieldFileLine.toUpperCase().trim();

        if (cleanLine.startsWith(";")) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Comment line" + cleanLine);
        } else if (cleanLine.startsWith("[")) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Header line: " + cleanLine);
        }

        String[] parts = cleanLine.split("\\s+");

        //deal with line too short.
        if (parts.length < 7) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Line too short: " + cleanLine);
        }

        //line too long is harder, warn don't kill;
        if (parts.length > 7) {
            ConsoleController.log.addMessage("Forcefield line is longer than expected");
        }

        this.name = parts[NAME_INDEX];
        try {
            this.atomNumber = Integer.parseInt(parts[ATOM_NUMBER_INDEX]);
        } catch (NumberFormatException ex) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "at.num should be an integer: " + parts[ATOM_NUMBER_INDEX]);
        }

        try {
            this.mass = Double.parseDouble(parts[MASS_INDEX]);
        } catch (NumberFormatException ex) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "mass should be an numeric value: " + parts[MASS_INDEX]);
        }

        try {
            this.mass = Double.parseDouble(parts[CHARGE_INDEX]);
        } catch (NumberFormatException ex) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Charge should be an numeric value: " + parts[CHARGE_INDEX]);
        }


        this.ptype = parts[PYTPE_INDEX];


        try {
            this.mass = Double.parseDouble(parts[SIGMA_C6_INDEX]);
        } catch (NumberFormatException ex) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Column " + SIGMA_C6_INDEX + " should contain a numeric value");
        }

        try {
            this.mass = Double.parseDouble(parts[EPS_C12_INDEX]);
        } catch (NumberFormatException ex) {
            throw new ProteinPrepToolException("ATOMTYPES ERROR" , "Column " + EPS_C12_INDEX + " should contain a numeric value");
        }

        this.orgOrder = index;
    }


    public String getName() {
        return name;
    }

    public int getAtomNumber() {
        return atomNumber;
    }

    public double getMass() {
        return mass;
    }

    public double getCharge() {
        return charge;
    }

    public String getPtype() {
        return ptype;
    }

    public double getSigma_c6() {
        return sigma_c6;
    }

    public double getEps_c12() {
        return eps_c12;
    }


    /**
     * Returns string representation of a the single atom type.
     *
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String gap = "\t\t\t";

        sb.append(this.name).append(gap).append(this.atomNumber).append(gap).append(this.mass).append(gap)
                .append(this.charge).append(gap).append(this.ptype).append(gap).append(sigma_c6).append(gap)
                .append(eps_c12);

        return sb.toString();


    }

}
