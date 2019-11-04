package controller.pdbTopTrajFFVerification;

import controller.*;
import controller.TopFile.AtomTopology;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Character.isDigit;

/*******************************************************************************
 *
 *	Filename   :	PdbTopResNameComparator.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class is designed to check whether or not all of the residue names in
 *	the topology match those within the PDB file. Sometimes, naming
 *	conventions can get muddled, for example HB12 == 2HB1, so the
 *	application needs to be resiliant to that.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class PdbTopResNameComparator extends VerificationTest {

    private final String failMessage = "The PDB file and topology file contain different residues.";


    public PdbTopResNameComparator(PDBFile pdb, TopologyFile tp, Trajectory traj) {
        super(pdb, tp, traj);
    }

    @Override
    public boolean performTest() {

        int residue = 0;

        ArrayList<String> pdbList = pdbFile.getListOfResiudes();
        ArrayList<String> topList = topFile.getListOfResiudes();

        if (pdbList.size() != topList.size()) {
            return false;
        }

        //compare residue strings
        for (int i = 0; i < pdbList.size(); i++) {
            if (!(pdbList.get(i).trim().equalsIgnoreCase(topList.get(i).trim()))) {
                return false;
            }
        }


        //then, spin through each residue looking to see if they contain the same atoms.
        int missMatches = 0;
        try {
            for (int resNum = 0; resNum < pdbList.size(); resNum++) {
                missMatches += compareResidue(this.pdbFile.getResidue(resNum), topFile.getResidue(resNum));
            }
        } catch (ProteinPrepToolException e) {
            return false;
        }

        if (missMatches > 0) {
            ConsoleController.log.addMessage("PDBFile and Topology compared, " + missMatches +
                    " invalid pairs found");
            return false;
        } else {
            return true;
        }

    }


    protected static int compareResidue(ArrayList<Atom> pdb, ArrayList<AtomTopology> top) {

        //build a hashmap of atom
        HashMap<String, Integer> tMap = new HashMap<>();

        if (pdb.size() != top.size()) {
            return -1;
        }

        if (pdb.size() < 1) {
            return -2;
        }


        for (Atom atm : pdb) {
            String name = atm.getAtomName();
            name.trim();
            tMap.putIfAbsent(name, 0);
            tMap.put(name, tMap.get(name) + 1);
        }

        for (AtomTopology aTop : top) {
            String name = aTop.getAtom();
            name = name.trim();
            boolean found = false;
            if (!tMap.containsKey(name)) {
                if (name.length() == 4) {
                    boolean reordered = false;
                    if (Character.isDigit(name.charAt(0))) {
                        char a = name.charAt(0);
                        StringBuilder sb = new StringBuilder();
                        sb.append(name.substring(1, 4)).append(a);
                        name = sb.toString();
                        reordered = true;
                    } else if (Character.isDigit(name.charAt(3))) {
                        char a = name.charAt(3);
                        StringBuilder sb = new StringBuilder();
                        sb.append(a).append(name.substring(0, 3));
                        name = sb.toString();
                        reordered = true;
                    }

                    if (reordered && tMap.containsKey(name)) {
                        found = true;

                        //TODO: updated either topology or pdb file to make them the same. FLEXIDOCK is not resiliant
                        //TODO: to this.

                    }

                }
            } else {
                found = true;
            }

            if (found) {
                tMap.put(name, tMap.get(name) - 1);
            }
        }

        int mismatches = 0;

        Iterator it = tMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pair = (Map.Entry) it.next();
            if (pair.getValue() > 0)
                mismatches += (pair.getValue());
        }

        return (mismatches);
    }

    @Override
    public String getFailMessage() {
        return failMessage;
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
