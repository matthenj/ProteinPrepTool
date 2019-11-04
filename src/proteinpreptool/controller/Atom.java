package controller;

import java.text.DecimalFormat;

/*******************************************************************************
 *
 *	Filename   :	Atom.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class represents an ATOM or HETATM, as contained within a PDB  file.
 *	File contains character positions of each section of data, as well data
 *	representing any one loaded atom. Format as PDB can be  used to get the
 *	line as it would be in a PDB file.
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/




public class Atom {

    //PDB Files are split up based on character location.

    private final int RECORD_NAME_FIELD_START = 0;
    private final int RECORD_NAME_FIELD_LENGTH = 6;
    private final int ATOM_NUMBER_FIELD_START = 6;
    private final int ATOM_NUMBER_FIELD_LENGTH = 5;
    private final int ATOM_NAME_FIELD_START = 12;
    private final int ATOM_NAME_FIELD_LENGTH = 4;
    private final int ALT_LOC_POS = 16;
    private final int ATOM_RESIDUE_NAME_START = 17;
    private final int ATOM_RESIDUE_NAME_LENGTH = 3;
    private final int CHAIN_ID_POS = 21;
    private final int ATOM_RESIDUE_NUMBER_START = 22;
    private final int ATOM_RESIDUE_NUMBER_LENGTH = 4;
    private final int I_CODE_POS = 27;
    private final int X_START = 30;
    private final int X_LENGTH = 8;
    private final int Y_START = 38;
    private final int Y_LENGTH = 8;
    private final int Z_START = 46;
    private final int Z_LENGTH = 8;
    private final int OCCUPANCY_START = 54;
    private final int OCCUPANCY_LENGTH = 6;
    private final int TEMPFACTOR_START = 60;
    private final int TEMPFACTOR_LENGTH = 6;
    private final int GAP = 10;
    private final int ELEMENT_START = 76;
    private final int ELEMENT_LENGTH = 2;
    private final int CHARGE_START = 78;
    private final int CHARGE_LENGTH = 2;

    //mask for formatting output
    private final String mask = "%-" + RECORD_NAME_FIELD_LENGTH + "s%" + ATOM_NUMBER_FIELD_LENGTH + "d %-" + ATOM_NAME_FIELD_LENGTH +
            "s%c%" + ATOM_RESIDUE_NAME_LENGTH + "s%s%c%" + ATOM_RESIDUE_NUMBER_LENGTH + "d%c%3s%" + X_LENGTH + "s%" + Y_LENGTH + "s%"
            + Z_LENGTH + "s%" + OCCUPANCY_LENGTH + "s%" + TEMPFACTOR_LENGTH + "s%" + GAP + "s%" + ELEMENT_LENGTH + "s%"
            + CHARGE_LENGTH + "s";


    //Atom Information
    private String  recordName;
    private int     serialNumber;
    private String  atomName;
    private char    altLoc;
    private String  resName;
    private char    chainId;
    private int     resSeqNum;
    private char    iCode;
    private Double3 coordinates;
    private double  occupancy;
    private double  tempFactor;
    private String  element;
    private String  charge;

    //occupancy, tempfactor, element, charge
    private boolean[] informationMissing = {false, false, false, false};


    public Atom(){
        recordName = "";
        serialNumber = -1;
        atomName = "";
        altLoc = (char)-1;
        resName = "";
        chainId = (char)-1;
        resSeqNum = -1;
        iCode = (char)-1;
        coordinates = new Double3();
        occupancy = -1.0;
        tempFactor = -1.0;
        element = "";
        charge = "";
    }


    public Atom(Atom atm, double x, double y, double z){
        recordName = atm.recordName;
        serialNumber = atm.serialNumber;
        atomName = atm.atomName;
        altLoc = atm.altLoc;
        resName = atm.resName;
        chainId = atm.chainId;
        resSeqNum = atm.resSeqNum;
        iCode = atm.iCode;
        coordinates = new Double3(x, y, z);
        occupancy = atm.occupancy;
        tempFactor = atm.tempFactor;
        element= atm.element;
        charge = atm.charge;

    }

    public Atom(Atom atm){
        this(atm, atm.getXPosition(), atm.getYPosition(), atm.getZPosition());
    }

    /**
     * Method designed to read in a line from a PDB file. If it is a "ATOM" or "HETATM" line, the structure will be populated
     * with the information it contains. If it is not, an exception is thrown.
     * @param s - PDB File line
     */

    private void decomposeAndPopulateFromString(String s) throws ProteinPrepToolException {

        String fieldType = s.substring(RECORD_NAME_FIELD_START, RECORD_NAME_FIELD_START+RECORD_NAME_FIELD_LENGTH).toUpperCase().trim();

        if(!(fieldType.equals("ATOM") || fieldType.equals("HETATM"))){
            throw new ProteinPrepToolException("Non-atom PDB line");
        }

        recordName = fieldType;

        atomName = s.substring(ATOM_NAME_FIELD_START, ATOM_NAME_FIELD_START+ATOM_NAME_FIELD_LENGTH).toUpperCase().trim();
        resName = s.substring(ATOM_RESIDUE_NAME_START, ATOM_RESIDUE_NAME_START+ATOM_RESIDUE_NAME_LENGTH).trim();

        serialNumber = Integer.parseInt(s.substring(ATOM_NUMBER_FIELD_START, ATOM_NUMBER_FIELD_START+ATOM_NUMBER_FIELD_LENGTH).trim());
        resSeqNum = Integer.parseInt(s.substring(ATOM_RESIDUE_NUMBER_START, ATOM_RESIDUE_NUMBER_START+ATOM_RESIDUE_NUMBER_LENGTH).trim());


        //guard against missing information
        String  tmpString = (s.substring(OCCUPANCY_START, OCCUPANCY_START+OCCUPANCY_LENGTH).trim());
        if(tmpString.length() > 0) {
            occupancy = Double.parseDouble(tmpString);
            informationMissing[0] = false;
        } else {
            informationMissing[0] = true;
            occupancy = -10;
        }

        tmpString = s.substring(TEMPFACTOR_START, TEMPFACTOR_START+TEMPFACTOR_LENGTH).trim();
        if(tmpString.length() > 0){
            tempFactor = Double.parseDouble(tmpString);
            informationMissing[1] = false;
        } else {
            tempFactor = -10;
            informationMissing[1] = true;
        }

        coordinates.x = Double.parseDouble((s.substring(X_START, X_START+X_LENGTH).trim()));
        coordinates.y = Double.parseDouble((s.substring(Y_START, Y_START+Y_LENGTH).trim()));
        coordinates.z = Double.parseDouble((s.substring(Z_START, Z_START+Z_LENGTH).trim()));

        iCode = s.charAt(I_CODE_POS);
        altLoc = s.charAt(ALT_LOC_POS);
        chainId = s.charAt(CHAIN_ID_POS);

        s = s.trim();

        if(s.length() >= ELEMENT_START + ELEMENT_LENGTH){
            element = s.substring(ELEMENT_START, ELEMENT_START + ELEMENT_LENGTH).trim();
            informationMissing[2] = false;
        } else if(s.length() == ELEMENT_START + 1) {
            element = s.substring(ELEMENT_START, ELEMENT_START + 1);
            informationMissing[2] = false;
        } else{
            informationMissing[2] = true;
            element = "";
        }

        if(s.length() >= CHARGE_START + CHARGE_LENGTH){
            charge = s.substring(CHARGE_START, CHARGE_START + CHARGE_LENGTH);
            informationMissing[3] = false;
        } else {
            informationMissing[3] = true;
            charge = "";
        }


    }

    public Atom(String pdbFileLine) throws ProteinPrepToolException {
        coordinates = new Double3();
        decomposeAndPopulateFromString(pdbFileLine);
    }

    public void populateAtom(String pdbFileLine) throws ProteinPrepToolException {
        decomposeAndPopulateFromString(pdbFileLine);
    }

    private String formatStringForPDBFile(double value, int maxLength, int maxDecimals){

        //add one for DP
        double maxValueFullRes = Math.pow(10,(maxLength - (maxDecimals)))-1;

        DecimalFormat formatter  = (DecimalFormat)DecimalFormat.getInstance();
        formatter.setGroupingUsed(false);
        //can store at full res
        if(value < maxValueFullRes){
            formatter.setMaximumFractionDigits(maxDecimals);
            formatter.setMinimumFractionDigits(maxDecimals);
            formatter.setMinimumIntegerDigits(1);
            return formatter.format(value);
        } else {
            //loop off DPS (very unlikely in real world)
            while(maxDecimals >
            0){
                maxDecimals--;
                maxValueFullRes = Math.pow(10,(maxLength - (maxDecimals)))-1;
                if(value < maxValueFullRes) {
                    formatter.setMaximumFractionDigits(maxDecimals);
                    formatter.setMinimumFractionDigits(maxDecimals);
                    formatter.setMinimumIntegerDigits(1);
                    return formatter.format(value);
                }
            }


        }

        return "ERROR";
    }



    /**
     * Formats Atom information into PDB format.
     * @return String - PDB line relevant to this atom.
     */
    public String formatAsPDB(){
        String atomNameToPrint;
        //pad atom name if one character, as per PDB spec
        if(atomName.length() < 4){
            atomNameToPrint = " " + atomName;
        } else {
            atomNameToPrint = atomName;
        }

        //test fields that are often missing from PDB files; if they are, set field to space, else set to string.

        String occupancyString, tempFactorString, chargeString, elementString;

        if(informationMissing[0]){
            occupancyString = " ";
        } else {
            occupancyString = formatStringForPDBFile(occupancy, 6, 2);
        }


        if(informationMissing[1]){
            tempFactorString = " ";
        } else {
            tempFactorString = formatStringForPDBFile(tempFactor, 6, 2);
        }

        if(informationMissing[2]){
            elementString = " ";
        } else{
            elementString = element;
        }

        if(informationMissing[3]){
            chargeString = " ";
        } else {
            chargeString = charge + "";
        }




       String s = String.format(mask, recordName, serialNumber, atomNameToPrint, altLoc, resName, " ",
               chainId, resSeqNum, iCode, " ", formatStringForPDBFile(coordinates.x,8,3),
               formatStringForPDBFile(coordinates.y,8,3), formatStringForPDBFile(coordinates.z,
                       8,3), occupancyString, tempFactorString, " ", elementString, chargeString);
       return s;
    }



    public String getElement(){
        return element;
    }

    @Override
    public String toString(){


        return "";
    }


    public double getXPosition(){
        return this.coordinates.x;
    }

    public double getYPosition(){
        return this.coordinates.y;
    }

    public double getZPosition(){
        return this.coordinates.z;
    }


    public String getAtomName(){
        return this.atomName;
    }

    public String getResidueID(){
        return this.resName;
    }

}
