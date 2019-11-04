package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AmberPrmTopFileReader
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class loads an AMBER PRMTOP file into ProteinPrepTool. Some adaptation from
 * 	PARMEDs implementation
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


//# Gromacs uses "funct" flags in its parameter files to indicate what kind of
//# functional form is used for each of its different parameter types. This is
//# taken from the topdirs.c source code file along with a table in the Gromacs
//# user manual. The table below summarizes my findings, for reference:
//
// # Bonds
// # -----
// #  1 - F_BONDS : simple harmonic potential
// #  2 - F_G96BONDS : fourth-power potential
// #  3 - F_MORSE : morse potential
// #  4 - F_CUBICBONDS : cubic potential
// #  5 - F_CONNBONDS : not even implemented in GROMACS
// #  6 - F_HARMONIC : seems to be the same as (1) ??
// #  7 - F_FENEBONDS : finietely-extensible-nonlinear-elastic (FENE) potential
// #  8 - F_TABBONDS : bond function from tabulated function
// #  9 - F_TABBONDSNC : bond function from tabulated function (no exclusions)
// # 10 - F_RESTRBONDS : restraint bonds
//
// # Angles
// # ------
// #  1 - F_ANGLES : simple harmonic potential
// #  2 - F_G96ANGLES : cosine-based angle potential
// #  3 - F_CROSS_BOND_BONDS : bond-bond cross term potential
// #  4 - F_CROSS_BOND_ANGLES : bond-angle cross term potential
// #  5 - F_UREY_BRADLEY : Urey-Bradley angle-bond potential
// #  6 - F_QUARTIC_ANGLES : 4th-order polynomial potential
// #  7 - F_TABANGLES : angle function from tabulated function
// #  8 - F_LINEAR_ANGLES : angle function from tabulated function
// #  9 - F_RESTRANGLES : restricted bending potential
//
// # Dihedrals
// # ---------
// #  1 - F_PDIHS : periodic proper torsion potential [ k(1+cos(n*phi-phase)) ]
// #  2 - F_IDIHS : harmonic improper torsion potential
// #  3 - F_RBDIHS : Ryckaert-Bellemans torsion potential
// #  4 - F_PIDIHS : periodic harmonic improper torsion potential (same as 1)
// #  5 - F_FOURDIHS : Fourier dihedral torsion potential
// #  8 - F_TABDIHS : dihedral potential from tabulated function
// #  9 - F_PDIHS : Same as 1, but can be multi-term
// # 10 - F_RESTRDIHS : Restricted torsion potential
// # 11 - F_CBTDIHS : combined bending-torsion potential

import controller.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmberPrmTopFileReader extends TopFileLoader {

    private static final double AMBER_CHARGE_MULTIPLIER = 1 / 18.2223;
    private int activeWriteIndex;
    private int[] pointers;
    private boolean createAtoms;
    private boolean createResidues;
    private int fieldWidth;
    private int fieldsPerLine;
    private boolean formatSet;
    private ArrayList<AmberTopAtom> atoms;

    private ArrayList<AmberTopResidue> residues;

    private ArrayList<Double> bondForceConstant;
    private ArrayList<Double> bondEquilValue;
    private ArrayList<Double> angleForceConstant;
    private ArrayList<Double> angleEquilValue;
    private ArrayList<Double> diheadralForceConstant;
    private ArrayList<Double> diheadralPeriodicity;
    private ArrayList<Double> diheadralPhase;
    private ArrayList<Double> ljACoeff;
    private ArrayList<Double> ljBCoeff;
    private ArrayList<AmberTopBond> bondsIncHydrogen;
    private ArrayList<AmberTopBond> bondsExcHydrogen;
    private ArrayList<AmberTopAngle> anglesIncHydrogen;
    private ArrayList<AmberTopAngle> anglesExcHydrogen;
    private ArrayList<AmberTopDiheadralGroups> diheadralsIncHydrogen;
    private ArrayList<AmberTopDiheadralGroups> diheadralsExcHydrogen;
    private ArrayList<Integer> atomsPerMolecule;
    private ArrayList<Integer> nonBondedParmIndex;
    private ArrayList<AmberTopPair> pairs;
    private String radiusSet;
    private String title;

    public AmberPrmTopFileReader() {
        pointers = new int[32];
        activeWriteIndex = 0;
        fieldWidth = 0;
        fieldsPerLine = 0;
        formatSet = false;
        title = "";
        radiusSet = "";
        this.atoms = new ArrayList<>();
        this.residues = new ArrayList<>();
        bondForceConstant = new ArrayList<>();
        bondEquilValue = new ArrayList<>();
        angleForceConstant = new ArrayList<>();
        angleEquilValue = new ArrayList<>();
        diheadralForceConstant = new ArrayList<>();
        diheadralPeriodicity = new ArrayList<>();
        diheadralPhase = new ArrayList<>();
        ljACoeff = new ArrayList<>();
        ljBCoeff = new ArrayList<>();
        bondsIncHydrogen = new ArrayList<>();
        bondsExcHydrogen = new ArrayList<>();
        anglesIncHydrogen = new ArrayList<>();
        anglesExcHydrogen = new ArrayList<>();
        diheadralsIncHydrogen = new ArrayList<>();
        diheadralsExcHydrogen = new ArrayList<>();
        atomsPerMolecule = new ArrayList<>();
        nonBondedParmIndex = new ArrayList<>();
        pairs = new ArrayList<AmberTopPair>();
        createAtoms = false;
        createResidues = false;
    }

    private MODE getMode(String flag) {
        for (MODE opMode : MODE.values()) {
            if (opMode.toString().equals(flag)) {
                return opMode;
            }
        }
        return MODE.UNKNOWN;

    }

    private void processPointers(String line) {
        String[] split = line.trim().split("\\s+");
        for (int i = 0; i < split.length; i++) {
            try {
                pointers[activeWriteIndex] = Integer.parseInt(split[i]);
                activeWriteIndex++;
            } catch (NumberFormatException nfe) {
                ConsoleController.log.addMessage("Parse failed: " + split[i]);
            }
        }
        atoms.ensureCapacity(pointers[POINTERS_INDEX.NATOM.index]);

    }

    private void processAtoms(String line) {
        if (line.length() % 4 == 0) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                String atomName = line.substring(i, i + fieldWidth);
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setAtom(atomName);// = atomName;
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown atom line:" + line);
        }
    }

    private void processCharge(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setCharge(Double.parseDouble(line.substring(i, i + fieldWidth).trim()) * AMBER_CHARGE_MULTIPLIER);
                activeWriteIndex++;
            }
        } else {
            //resort to whitespace.
            ConsoleController.log.addMessage("Format unset, using whitespace fallback (process charge)");
            String[] charges = line.trim().split("\\s+");
            for (String str : charges) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setCharge(Double.parseDouble(str));
                activeWriteIndex++;
            }
        }

    }

    private void processAtomNumber(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setNumber(Integer.parseInt(line.substring(i, i + fieldWidth).trim()));
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processMass(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setMass(Double.parseDouble(line.substring(i, i + fieldWidth).trim()));
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processAmberAtomType(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).atomTopology.setType(AtomTopology.getAtomType(line.substring(i, i + fieldWidth)));
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processAmberAtomRadii(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).radius = Double.parseDouble(line.substring(i, i + fieldWidth).trim());
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processLjIndex(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createAtoms) {
                    atoms.add(new AmberTopAtom());
                }
                atoms.get(activeWriteIndex).ljIndex = Integer.parseInt(line.substring(i, i + fieldWidth).trim());
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processNonBondedParmIndex(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                this.nonBondedParmIndex.add(Integer.parseInt(line.substring(i, i + fieldWidth).trim()));
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processResidueLabel(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createResidues) {
                    this.residues.add(new AmberTopResidue());
                }
                this.residues.get(activeWriteIndex).label = line.substring(i, i + fieldWidth);
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processResidueFirstAtom(String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                if (createResidues) {
                    this.residues.add(new AmberTopResidue());
                }
                this.residues.get(activeWriteIndex).firstAtom = Integer.parseInt(line.substring(i, i + fieldWidth).trim());
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processBondInformation(ArrayList<AmberTopBond> a, String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                //determine if new or existing bond - 3 values per bond
                if (activeWriteIndex % 3 == 0) {
                    a.add(new AmberTopBond());
                }
                //get correct "int 3" to set.
                int lookup = activeWriteIndex / 3;
                //determine value
                int value = Integer.parseInt(line.substring(i, i + fieldWidth).trim());
                switch (activeWriteIndex % 3) {
                    case 0:
                        a.get(lookup).bt.setAtomAI(value / 3 + 1);
                        break;
                    case 1:
                        a.get(lookup).bt.setAtomAJ(value / 3 + 1);
                        break;
                    case 2:
                        a.get(lookup).index = value;
                        break;
                }
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processAngleInformation(ArrayList<AmberTopAngle> a, String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                //determine if new or existing bond - 4 values per angle
                if (activeWriteIndex % 4 == 0) {
                    a.add(new AmberTopAngle());
                }
                //get correct "int 4" to set.
                int lookup = activeWriteIndex / 4;
                //determine value
                int value = Integer.parseInt(line.substring(i, i + fieldWidth).trim());
                switch (activeWriteIndex % 4) {
                    case 0:
                        a.get(lookup).at.setAtomAI(value / 3 + 1);
                        break;
                    case 1:
                        a.get(lookup).at.setAtomAJ(value / 3 + 1);
                        break;
                    case 2:
                        a.get(lookup).at.setAtomAK(value / 3 + 1);
                        break;
                    case 3:
                        a.get(lookup).index = value;
                        break;
                }
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void processDiheadralInformation(ArrayList<AmberTopDiheadralGroups> a, String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                //determine if new or existing bond - 4 values per angle
                if (activeWriteIndex % 5 == 0) {
                    a.add(new AmberTopDiheadralGroups());
                }
                int lookup = activeWriteIndex / 5;
                //determine value
                int value = Integer.parseInt(line.substring(i, i + fieldWidth).trim());
                switch (activeWriteIndex % 5) {
                    case 0:
                        a.get(lookup).dt.setAtomAI(Math.abs(value / 3) + 1);
                        break;
                    case 1:
                        a.get(lookup).dt.setAtomAJ(Math.abs(value) / 3 + 1);
                        break;
                    case 2:
                        a.get(lookup).dt.setAtomAK(Math.abs(value) / 3 + 1);
                        break;
                    case 3:
                        a.get(lookup).dt.setAtomAL(Math.abs(value) / 3 + 1);
                        break;
                    case 4:
                        a.get(lookup).i = value;
                        break;
                }
                activeWriteIndex++;
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void placeLineDataInArrayListDouble(ArrayList<Double> a, String line) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                a.add(Double.parseDouble(line.substring(i, i + fieldWidth).trim()));
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void placeLineDataInArrayListDouble(ArrayList<Double> a, String line, double scaleMultiplier) {
        if (formatSet) {
            for (int i = 0; i < line.length(); i += fieldWidth) {
                a.add((Double.parseDouble(line.substring(i, i + fieldWidth).trim()) * scaleMultiplier));
            }
        } else {
            ConsoleController.log.addMessage("Unknown format, ignoring section: " + line);
        }
    }

    private void addResidueInformationToAtoms() {
        int atomId = 0;
        for (int i = 0; i < residues.size(); i++) {
            AmberTopResidue res = residues.get(i);
            int startIndex = res.firstAtom - 1;
            int finIndex = (i + 1 < residues.size()) ? residues.get(i + 1).firstAtom - 1 : pointers[POINTERS_INDEX.NATOM.index];
            for (; startIndex < finIndex; startIndex++) {
                atoms.get(startIndex).atomTopology.setResidueID(res.label);
                atoms.get(startIndex).atomTopology.setResnr(i + 1);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void generatePairs() {
        //unique list of atom pairs for non-bonded interactions. nrexcl comes in to play here (to do)
        ArrayList<AtomPair> pairs = new ArrayList<AtomPair>();
        //loop through diheadrals, generate list of unique pairs (atoms 1 & 4)
        for (int i = 0; i < this.diheadralsExcHydrogen.size(); i++) {
            pairs.add(new AtomPair(this.diheadralsExcHydrogen.get(i).dt.getAtomAI(), this.diheadralsExcHydrogen.get(i).dt.getAtomAL(), i));
        }
        for (int i = 0; i < this.diheadralsIncHydrogen.size(); i++) {
            pairs.add(new AtomPair(this.diheadralsIncHydrogen.get(i).dt.getAtomAI(), this.diheadralsIncHydrogen.get(i).dt.getAtomAL(), i + this.diheadralsExcHydrogen.size()));
        }
        Stream<AtomPair> pairStream = pairs.stream();
        pairs = pairStream.distinct().collect(Collectors.toCollection(ArrayList::new));
        //spin through, and identify pairs that have no angle or bond between them.
        ArrayList<AtomPair> potentialPairs = new ArrayList<AtomPair>();
        for (AmberTopBond bond : bondsExcHydrogen) {
            potentialPairs.add(new AtomPair(bond.bt.getAtomAI(), bond.bt.getAtomAJ(), bond.index));
        }
        for (AmberTopBond bond : bondsIncHydrogen) {
            potentialPairs.add(new AtomPair(bond.bt.getAtomAI(), bond.bt.getAtomAJ(), bond.index));
        }
        int count = 0;
        for (AmberTopAngle angle : anglesExcHydrogen) {
            potentialPairs.add(new AtomPair(angle.at.getAtomAI(), angle.at.getAtomAJ(), count++));
            potentialPairs.add(new AtomPair(angle.at.getAtomAJ(), angle.at.getAtomAK(), count++));
            potentialPairs.add(new AtomPair(angle.at.getAtomAI(), angle.at.getAtomAK(), count++));
        }
        for (AmberTopAngle angle : anglesIncHydrogen) {
            potentialPairs.add(new AtomPair(angle.at.getAtomAI(), angle.at.getAtomAJ(), count++));
            potentialPairs.add(new AtomPair(angle.at.getAtomAJ(), angle.at.getAtomAK(), count++));
            potentialPairs.add(new AtomPair(angle.at.getAtomAI(), angle.at.getAtomAK(), count++));
        }
        HashSet<AtomPair> hs = new HashSet<AtomPair>(pairs);
        hs.removeAll(potentialPairs);
        for (AtomPair it : hs) {
            this.pairs.add(new AmberTopPair(it));
        }
        //reorder back into original order.
        pairs.sort(new Comparator<AtomPair>() {
            @Override
            public int compare(AtomPair o1, AtomPair o2) {
                return o1.z - o2.z;
            }
        });

    }

    @Override
    public TopologyFile loadTopFile(String path) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            int count = 0;
            MODE currentMode = MODE.UNKNOWN;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("%")) {
                    if (line.startsWith("%FLAG")) {
                        //   System.out.println(line.split("\\s+")[1]+"(" + count++ + ")" );
                        currentMode = getMode(line.substring(5).trim());
                        activeWriteIndex = 0;
                        formatSet = false;
                        createAtoms = false;
                        createResidues = false;
                    } else if (line.startsWith("%FORMAT") && !formatSet) {
                        String data = line.substring(8, line.trim().length() - 1);
                        String[] tmp = data.split("[a-z]|[A-Z]");
                        fieldWidth = (int) Double.parseDouble(tmp[1]);
                        fieldsPerLine = Integer.parseInt(tmp[0]);
                        formatSet = true;
                    }
                    continue;
                }
                switch (currentMode) {
                    case TITLE:
                        if (title.length() == 0) title = line;
                        break;
                    case POINTERS:
                        processPointers(line);
                        break;
                    case ATOM_NAME:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processAtoms(line);
                        break;
                    case CHARGE:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processCharge(line);
                        break;
                    case ATOMIC_NUMBER:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processAtomNumber(line);
                        break;
                    case MASS:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processMass(line);
                        break;
                    case ATOM_TYPE_INDEX:
                        processLjIndex(line);
                        break;
                    case NUMBER_EXCLUDED_ATOMS:
                        break;
                    case NONBONDED_PARM_INDEX:
                        processNonBondedParmIndex(line);
                        break;
                    case RESIDUE_LABEL:
                        if (residues.size() == 0) createResidues = true;
                        processResidueLabel(line);
                        break;
                    case RESIDUE_POINTER:
                        if (residues.size() == 0) createResidues = true;
                        processResidueFirstAtom(line);
                        break;
                    case BOND_FORCE_CONSTANT:
                        placeLineDataInArrayListDouble(bondForceConstant, line);
                        break;
                    case BOND_EQUIL_VALUE:
                        //convert from Angstroms to Nanometers
                        placeLineDataInArrayListDouble(bondEquilValue, line, 0.1);
                        break;
                    case ANGLE_FORCE_CONSTANT:
                        placeLineDataInArrayListDouble(angleForceConstant, line);
                        break;
                    case ANGLE_EQUIL_VALUE:
                        placeLineDataInArrayListDouble(angleEquilValue, line);
                        break;
                    case DIHEDRAL_FORCE_CONSTANT:
                        placeLineDataInArrayListDouble(diheadralForceConstant, line);
                        break;
                    case DIHEDRAL_PERIODICITY:
                        placeLineDataInArrayListDouble(diheadralPeriodicity, line);
                        break;
                    case DIHEDRAL_PHASE:
                        placeLineDataInArrayListDouble(diheadralPhase, line);
                        break;
                    case SCEE_SCALE_FACTOR:
                        break;
                    case SCNB_SCALE_FACTOR:
                        break;
                    case LENNARD_JONES_ACOEF:
                        placeLineDataInArrayListDouble(ljACoeff, line);
                        break;
                    case LENNARD_JONES_BCOEF:
                        placeLineDataInArrayListDouble(ljBCoeff, line);
                        break;
                    case BONDS_INC_HYDROGEN:
                        processBondInformation(bondsIncHydrogen, line);
                        break;
                    case BONDS_WITHOUT_HYDROGEN:
                        processBondInformation(bondsExcHydrogen, line);
                        break;
                    case ANGLES_INC_HYDROGEN:
                        processAngleInformation(anglesIncHydrogen, line);
                        break;
                    case ANGLES_WITHOUT_HYDROGEN:
                        processAngleInformation(anglesExcHydrogen, line);
                        break;
                    case DIHEDRALS_INC_HYDROGEN:
                        processDiheadralInformation(diheadralsIncHydrogen, line);
                        break;
                    case DIHEDRALS_WITHOUT_HYDROGEN:
                        processDiheadralInformation(diheadralsExcHydrogen, line);
                        break;
                    case EXCLUDED_ATOMS_LIST:
                        break;
                    case HBOND_ACOEF:
                        break;
                    case HBOND_BCOEF:
                        break;
                    case HBCUT:
                        break;
                    case AMBER_ATOM_TYPE:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processAmberAtomType(line);
                        break;
                    case TREE_CHAIN_CLASSIFICATION:
                        break;
                    case JOIN_ARRAY:
                        //Unused
                        break;
                    case IROTAT:
                        //Unused
                        break;
                    case BOX_DIMENSIONS:
                        //unused
                        break;
                    case CAP_INFO:
                        //unsued
                        break;
                    case CAP_INFO2:
                        //unsued
                        break;
                    case RADIUS_SET:
                        if (this.radiusSet.length() == 0) this.radiusSet = line;
                        break;
                    case RADII:
                        if (atoms.size() == 0) {
                            createAtoms = true;
                        }
                        processAmberAtomRadii(line);
                        break;
                    case SCREEN:
                        break;
                    case IPOL:
                        break;
                    case SOLTY:
                    default:
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        addResidueInformationToAtoms();
        generatePairs();
        TopologyFile tf = new TopologyFile();
        for (int i = 0; i < atoms.size(); i++) {
            AmberTopAtom acv = atoms.get(i);
            acv.atomTopology.setNumber(i + 1);
            acv.atomTopology.setChargeNumber(i + 1);
            tf.addAtom(acv.atomTopology);
        }
        for (AmberTopBond amberTopBond : bondsExcHydrogen) {
            tf.addBond(amberTopBond.bt);
        }
        for (AmberTopBond amberTopBond : bondsIncHydrogen) {
            tf.addBond(amberTopBond.bt);
        }
        for (AmberTopAngle amberTopAngle : anglesExcHydrogen) {
            tf.addAngle(amberTopAngle.at);
        }
        for (AmberTopAngle amberTopAngle : anglesIncHydrogen) {
            tf.addAngle(amberTopAngle.at);
        }
        for (AmberTopPair pair : pairs) {
            tf.addPair(pair.pt);
        }
        for (AmberTopDiheadralGroups amberTopDiheadralGroups : diheadralsExcHydrogen) {
            tf.addDiheadral(amberTopDiheadralGroups.dt);
        }
        for (AmberTopDiheadralGroups amberTopDiheadralGroups : diheadralsIncHydrogen) {
            tf.addDiheadral(amberTopDiheadralGroups.dt);
        }
        return tf;

    }

    private enum MODE {
        TITLE, POINTERS, ATOM_NAME, CHARGE, ATOMIC_NUMBER, MASS, ATOM_TYPE_INDEX, NUMBER_EXCLUDED_ATOMS, NONBONDED_PARM_INDEX, RESIDUE_LABEL, RESIDUE_POINTER, BOND_FORCE_CONSTANT, BOND_EQUIL_VALUE, ANGLE_FORCE_CONSTANT, ANGLE_EQUIL_VALUE, DIHEDRAL_FORCE_CONSTANT, DIHEDRAL_PERIODICITY, DIHEDRAL_PHASE, SCEE_SCALE_FACTOR, SCNB_SCALE_FACTOR, SOLTY, LENNARD_JONES_ACOEF, LENNARD_JONES_BCOEF, BONDS_INC_HYDROGEN, BONDS_WITHOUT_HYDROGEN, ANGLES_INC_HYDROGEN, ANGLES_WITHOUT_HYDROGEN, DIHEDRALS_INC_HYDROGEN, DIHEDRALS_WITHOUT_HYDROGEN, EXCLUDED_ATOMS_LIST, HBOND_ACOEF, HBOND_BCOEF, HBCUT, AMBER_ATOM_TYPE, TREE_CHAIN_CLASSIFICATION, JOIN_ARRAY, IROTAT, RADIUS_SET, RADII, SCREEN, IPOL, BOX_DIMENSIONS, CAP_INFO, CAP_INFO2, UNKNOWN
    }

    //infomration contained int the "pointers" part of the prmtop is stored in an array, which can be referenced with this enum.
    private enum POINTERS_INDEX {
        NATOM(0), NTYPES(1), NBONH(2), MBONA(3), NTHETH(4), MTHETA(5), NPHIH(6), MPHIA(7), NHPARM(8), NPARM(9), NNB(10), NRES(11), NBONA(12), NTHETA(13), NPHIA(14), NUMBND(15), NUMANG(16), NPTRA(17), NATYP(18), NPHB(19), IFPERT(20), NBPER(21), NGPER(22), NDPER(23), MBPER(24), MGPER(25), MDPER(26), IFBOX(27), NMXRS(28), IFCAP(29), NUMEXTRA(30), NCOPY(31);

        public int index;

        POINTERS_INDEX(int count) {
            this.index = count;
        }

        private int getIndex() {
            return index;
        }
    }

    //wrapper classes; store the relevent top information, and any additional info present in the amber file.
    private class AmberTopAtom {
        AtomTopology atomTopology;

        //extra fields in amber file.
        double radius;
        double polarization; //not prsent
        int ljIndex; //not present
        int atomTypeIndex; //not present

        AmberTopAtom() {
            atomTopology = new AtomTopology();
            ljIndex = 0;
            atomTypeIndex = 0;
            radius = -1.0;
            polarization = 0.0;
        }
    }

    private class AmberTopBond {
        BondTopology bt;
        int index;

        AmberTopBond() {
            bt = new BondTopology();
            index = -1;
        }

    }

    private class AmberTopAngle {
        AngleTopology at;
        int index;

        AmberTopAngle() {
            at = new AngleTopology();
            index = -1;
        }
    }

    private class AmberTopPair {
        PairTopology pt;
        int index;

        AmberTopPair() {
            pt = new PairTopology();
            index = 0;
        }

        AmberTopPair(AtomPair ap) {
            pt = new PairTopology(ap.x, ap.y, 1);
            this.index = ap.z;
        }

    }

    private class AmberTopResidue {
        String label;
        int firstAtom;

        AmberTopResidue() {
            label = "";
            firstAtom = -1;
        }
    }

    private class AmberTopDiheadralGroups {
        DiheadralTopology dt;
        int i;

        AmberTopDiheadralGroups() {
            dt = new DiheadralTopology();
            i = 0;
        }
    }

    private class AmberTopHBondCoef {
        double a, b;
    }
}