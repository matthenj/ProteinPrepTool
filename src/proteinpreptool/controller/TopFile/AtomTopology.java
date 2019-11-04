package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AtomTopology.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class describes the [ atoms ] part of a GROAMCS topology object. extends
 *	AtomInteraction abstract class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class AtomTopology {

    private static HashMap<String, AtomType> uniqueAtomTypes = new HashMap<>();

    public static AtomType getAtomType(String type) {
        if (uniqueAtomTypes.containsKey(type)) {
            return uniqueAtomTypes.get(type);
        } else {
            AtomType atm = new AtomType(type);
            uniqueAtomTypes.put(type, atm);
            return atm;
        }
    }

    public static AtomType getAtomTypeIfPresent(String type) {
        if (uniqueAtomTypes.containsKey(type)) {
            return uniqueAtomTypes.get(type);
        } else {
            return null;
        }
    }

    private int number;
    private AtomType type;
    private int resnr;
    private String residueID;
    private String atom;
    private int chargeNumber;
    private double charge;
    private double mass;

    public AtomTopology() {
        this.number = -1;
        type = null;
        resnr = -1;
        residueID = "";
        atom = "";
        chargeNumber = -1;
        charge = 0.0;
        this.mass = -1;
    }

    public AtomTopology(int number, AtomType type, int resnr, String residueID, String atom, int chargeNumber, double charge, double mass) {
        this.number = number;
        this.type = type;
        this.resnr = resnr;
        this.residueID = residueID.trim();
        this.atom = atom;
        this.chargeNumber = chargeNumber;
        this.charge = charge;
        this.mass = mass;
    }

    public AtomTopology(AtomTopology atm) {
        this(atm.number, atm.type, atm.resnr, atm.residueID, atm.atom, atm.chargeNumber, atm.charge, atm.mass);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public AtomType getType() {
        return type;
    }

    public void setType(AtomType type) {
        this.type = type;
    }

    public int getResnr() {
        return resnr;
    }

    public void setResnr(int resnr) {
        this.resnr = resnr;
    }

    public String getResidueID() {
        return residueID;
    }

    public void setResidueID(String residueID) {
        this.residueID = residueID;
    }

    public String getAtom() {
        return atom;
    }

    public void setAtom(String atom) {
        this.atom = atom;
    }

    public int getChargeNumber() {
        return chargeNumber;
    }

    public void setChargeNumber(int chargeNumber) {
        this.chargeNumber = chargeNumber;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMinimumFractionDigits(3);
        df.setMinimumFractionDigits(4);

        String strNumber = String.format("%6d", this.number);
        String strAtmType = String.format("%10s", this.type.toString().trim());
        String strResidID = String.format("%6s", this.residueID);
        String strAtom = String.format("%6s", this.atom);
        String strchargNu = String.format("%6d", this.chargeNumber);
        String strCharge = String.format("%10f", this.charge);
        String strMass = String.format("%10f", this.mass);
        String strResnr = String.format("%6d", this.resnr);

        sb.append(strNumber).append(" ").append(strAtmType).append(" ").append(strResnr).append(" ")
                .append(strResidID).append(" ").append(strAtom).append(" ").append(strchargNu).append(" ").append((strCharge)).append(" ").append((strMass));

        return sb.toString();
    }

}
