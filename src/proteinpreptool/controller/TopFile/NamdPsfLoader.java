package controller.TopFile;

import controller.Atom;
import controller.ConsoleController;
import controller.Int2;
import controller.ProteinPrepToolException;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*******************************************************************************
 *
 *	Filename   :	NamdPsfLoader.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class that can load a protein topology from a NAMD PSF file.
 *
 *	Version History: 15/07/19 (initial version)
 *
 *******************************************************************************/


public class NamdPsfLoader extends TopFileLoader {

    private enum OPERATION_MODE {ATOM, BOND, ANGLE, DIHEADRAL, IMPROPER, NONE}
    private static final String[] VALID_SELECTIONS = new String[]{"NATOM",
            "NBOND", "NTHETA", "NPHI", "NIMPHI"};

    @Override
    public TopologyFile loadTopFile(String path) {

        TopologyFile tf = new TopologyFile();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {

            OPERATION_MODE opMode = OPERATION_MODE.NONE;

            String line;
            while ((line = br.readLine()) != null) {

                //End of section
                if(line.isEmpty()) {
                    opMode = OPERATION_MODE.NONE;
                } else if(line.startsWith("REMARKS")){
                    //REMARKS line :- don't care.
                    continue;
                } else if(opMode == OPERATION_MODE.NONE){
                    //investigate line to see if next line is start of section
                    for (int i = 0; i < VALID_SELECTIONS.length; i++) {
                        String str = VALID_SELECTIONS[i];
                        if (line.contains(str)) {
                            opMode = OPERATION_MODE.values()[i];
                            break;
                        }
                    }

                } else {

                    try {
                        switch (opMode) {
                            case ATOM:
                                handleAtomsLine(tf, line);
                                break;
                            case BOND:
                                handleBondsLine(tf, line);
                                break;
                            case ANGLE:
                                handleAnglesLine(tf, line);
                                break;
                            case DIHEADRAL:
                            case IMPROPER:
                                handleDiheadralsLine(tf, line);
                                break;
                        }
                    } catch(ProteinPrepToolException pex){
                        ConsoleController.log.addMessage(line);
                    }


                }



            }
        } catch (IOException e) {
            ConsoleController.log.addMessage("Failed to open topology file.");
        }

        generatePairs(tf);

        return tf;
    }

    //5 sections


    //NAMD .psf format is this:
    //AtomID SegmentName ResidueID ResidueName Atomname AomType Charge Mass


    //static finals relating to atoms section
    private static final int ATOMS_NUMBER_OF_FIELDS = 9;
    private static final int ATOMS_ATOM_ID_POS = 0;
    private static final int ATOMS_SEGMENT_NAME_POS = 1;
    private static final int ATOMS_RESIDUE_ID_POS = 2;
    private static final int ATOMS_RESIDUE_NAME_POS = 3;
    private static final int ATOMS_ATOM_NAME_POS = 4;
    private static final int ATOMS_ATOM_TYPE_POS = 5;
    private static final int ATOMS_CHARGE_POS = 6;
    private static final int ATOMS_MASS_POS = 7;
    private static final int ATOMS_UNUSED_ZERO = 8;

    /**
     * Method to break down "Atoms line" into a GROMACS compatible format.
     *
     * @param tf   :- Topology file to load into
     * @param line :- Line to break into an "atom topology"
     */
    private void handleAtomsLine(TopologyFile tf, String line) throws ProteinPrepToolException {


        String[] atomsLine = line.trim().split("\\s+");

        if (atomsLine.length != ATOMS_NUMBER_OF_FIELDS) {
            throw new ProteinPrepToolException("Not an \"ATOMS\" Line");
        }

        try {
            int atomId = Integer.parseInt(atomsLine[ATOMS_ATOM_ID_POS]);
            int resn = Integer.parseInt(atomsLine[ATOMS_RESIDUE_ID_POS]);
            double charge = Double.parseDouble(atomsLine[ATOMS_CHARGE_POS]);
            double mass = Double.parseDouble(atomsLine[ATOMS_MASS_POS]);
            AtomType type = AtomTopology.getAtomType(atomsLine[ATOMS_ATOM_TYPE_POS]);

            //Settomg cgnr to zero TODO: check that.
            tf.addAtom(new AtomTopology(atomId, type, resn, atomsLine[ATOMS_RESIDUE_NAME_POS],
                    atomsLine[ATOMS_ATOM_NAME_POS], 0, charge, mass));


        } catch (NumberFormatException nex) {
            throw new ProteinPrepToolException("Not an  \"ATOMS\" Line!");
        }
    }

    private void handleBondsLine(TopologyFile tf, String line) throws ProteinPrepToolException {
        //four bonds per line.
        String[] split = line.trim().split("\\s+");

        //minimum of two digits per line.
        if (split.length < 2 || (split.length % 2) != 0) {
            throw new ProteinPrepToolException("Not a \"BONDS\" Line");
        }
        try {
            for (int i = 0; i < split.length; i += 2) {
                int a = Integer.parseInt(split[i]);
                int b = Integer.parseInt(split[i + 1]);
                tf.addBond(new BondTopology(a, b));
            }
        } catch (NumberFormatException nex) {
            throw new ProteinPrepToolException("Not an  \"ATOMS\" Line!");
        }
    }

    private void handleAnglesLine(TopologyFile tf, String line) throws ProteinPrepToolException {

        //three angles per line.
        String[] split = line.trim().split("\\s+");

        //minimum of three digits per line.
        if (split.length < 3 || (split.length % 3) != 0) {
            throw new ProteinPrepToolException("Not a \"ANGLES\" Line");
        }
        try {
            for (int i = 0; i < split.length; i += 3) {
                int a = Integer.parseInt(split[i]);
                int b = Integer.parseInt(split[i + 1]);
                int c = Integer.parseInt(split[i + 2]);
                tf.addAngle(new AngleTopology(a, b, c));
            }
        } catch (NumberFormatException nex) {
            throw new ProteinPrepToolException("Not an  \"ANGLES\" Line!");
        }

    }

    //use for both diheadrals and impropers.
    private void handleDiheadralsLine(TopologyFile tf, String line) throws ProteinPrepToolException {
        String[] split = line.trim().split("\\s+");
        if (split.length < 4 || (split.length % 4) != 0) {
            throw new ProteinPrepToolException("Not a \"Diheadrals\" Line");
        }
        try {
            for (int i = 0; i < split.length; i += 4) {
                int a = Integer.parseInt(split[i]);
                int b = Integer.parseInt(split[i + 1]);
                int c = Integer.parseInt(split[i + 2]);
                int d = Integer.parseInt(split[i + 3]);
                tf.addDiheadral(new DiheadralTopology(a, b, c, d));
            }
        } catch (NumberFormatException nex) {
            throw new ProteinPrepToolException("Not an  \"Diheadrals\" Line!");
        }

    }


    private void generatePairs(TopologyFile tf){
        //unique list of atom pairs for non-bonded interactions.
        ArrayList<AtomPair> pairs = new ArrayList<>();

        for (int i = 0; i < tf.getNumberOfDiheadrals(); i++) {
            DiheadralTopology dt = tf.getDiheadral(i);
            pairs.add(new AtomPair(dt.getAtomAI(), dt.getAtomAL(), i));
        }

        //remove duplicates
        Stream<AtomPair> pairStream = pairs.stream();
        pairs = pairStream.distinct().collect(Collectors.toCollection(ArrayList::new));

        ArrayList<AtomPair> potentialPairs = new ArrayList<AtomPair>();
        for (int i = 0; i < tf.getNumberOfBonds(); i++) {
            BondTopology bond = tf.getBondTopology(i);
            potentialPairs.add(new AtomPair(bond.getAtomAI(), bond.getAtomAJ(), i));
        }

        int count = 0;
        for (int i = 0; i < tf.getNumberOfAngles(); i++) {
            AngleTopology angle  = tf.getAngleTopology(i);
            potentialPairs.add(new AtomPair(angle.getAtomAI(), angle.getAtomAJ(), count++));
            potentialPairs.add(new AtomPair(angle.getAtomAJ(), angle.getAtomAK(), count++));
            potentialPairs.add(new AtomPair(angle.getAtomAI(), angle.getAtomAK(), count++));
        }

        HashSet<AtomPair> hs = new HashSet<AtomPair>(pairs);
        hs.removeAll(potentialPairs);

        //reorder back into original order.
//        pairs.sort(new Comparator<AtomPair>() {
//            @Override
//            public int compare(AtomPair o1, AtomPair o2) {
//                return o1.z - o2.z;
//            }
//        });

        for (AtomPair it : hs) {
            tf.addPair(new PairTopology(it.x, it.y, -1));
        }

        tf.orderPairs();




    }





}
