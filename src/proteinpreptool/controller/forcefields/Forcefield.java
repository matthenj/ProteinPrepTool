package controller.forcefields;

import controller.ProteinPrepToolException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

/*******************************************************************************
 *
 *	Filename   :	Forcefield.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class models a GROAMCS forcefield. All GROMACS forcefields have the same
 *	layout.
 *
 *  Inital use is to identify if a topology file contains an atom
 *	type that is not contained within the forcefield, and highlight that to
 *	the user. Later versions may try to infer the correct atom type from the
 *	top files (in the case of PRMTOP).
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class Forcefield {

    private String fileName;
    private File filePath;
    private HashMap<String, AtomType> atomtypes;

    public Forcefield(File filePath) {

        this.filePath = filePath;
        this.fileName = filePath.getName();
        this.atomtypes = new HashMap<>();

    }

    public String getFileName() {
        return fileName;
    }

    public void loadFile() {
        try (BufferedReader br = Files.newBufferedReader(filePath.toPath())) {

            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {

                try {
                    //Scroll through forcefield, loading each atom line at a time.
                    AtomType atomType = new AtomType(index, line);
                    this.atomtypes.putIfAbsent(atomType.getName().toUpperCase(), atomType);
                    index++;
                } catch (ProteinPrepToolException pex) {
                    //System.err.println(pex.getFriendlyMessage());
                }
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public boolean contains(String key) {
        return atomtypes.containsKey(key.trim().toUpperCase());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (AtomType t : this.atomtypes.values()) {
            sb.append(t.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public String[] getAvailableAtomTypes() {
        String[] knownAtoms = new String[this.atomtypes.size()];
        int i = 0;
        for (String s : atomtypes.keySet()) {
            knownAtoms[i++] = s;
        }
        return knownAtoms;
    }
}
