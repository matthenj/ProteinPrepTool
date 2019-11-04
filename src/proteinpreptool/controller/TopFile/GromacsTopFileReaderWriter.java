package controller.TopFile;

import controller.ConsoleController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*******************************************************************************
 *
 *	Filename   :	GromacsTopFileReaderWriter.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class can load a GROMACS .top file into a "TopologyFile" object, and
 *	also save a "TopologyFile" object into the GROMACS .top format, as
 *	required by HaptimolFlexiDock
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class GromacsTopFileReaderWriter extends TopFileLoader {

    private enum MODE {defaults, moleculetype, atoms, bonds, pairs, angles, dihedrals, system, molecules, UNKNOWN};

    private MODE mode;

    @Override
    public TopologyFile loadTopFile(String path) {
        TopologyFile tf = new TopologyFile();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {

            String line;
            while ((line = br.readLine()) != null) {
                //; == comment in GROMACS file
                if (line.startsWith(";")) continue;

                //each section defined with a tag.
                if (line.startsWith("[")) {
                    line.replace("[", "");
                    line.replace("]", "");
                    line = line.trim().toLowerCase();

                    try {
                        mode = MODE.valueOf(line);
                    } catch (IllegalArgumentException ex) {
                        ConsoleController.log.addMessage("Unknown GROMACS file tag: " + line);
                        mode = MODE.UNKNOWN;
                    }
                } else if (line.startsWith("#")) {
                    //includes are not opened, just stored for use later.
                    if (line.startsWith("#include")) {
                        tf.addIncludeStatement(line);
                    }
                } else if (mode != MODE.UNKNOWN) {
                    switch (mode) {
                        case defaults: {
                            String[] atomLine = line.trim().split("\\s+");
                            int nbfunc = Integer.parseInt(atomLine[0]);
                            int comRule = Integer.parseInt(atomLine[1]);
                            boolean genPairs = atomLine[2].contains("yes");
                            double lj = Double.parseDouble(atomLine[3]);
                            double qq = Double.parseDouble(atomLine[4]);
                            tf.setDefaults(new DefaultsTopology(nbfunc, comRule, genPairs, lj, qq));
                        }
                        break;
                        case moleculetype: {
                            String[] atomLine = line.trim().split("\\s+");
                            tf.setNameAndRXCL(atomLine[0], Integer.parseInt(atomLine[1]));
                        }
                        break;
                        case atoms: {
                            try {
                                String[] atomLine = line.trim().split("\\s+");
                                int nr = Integer.parseInt(atomLine[0]);
                                String type = atomLine[1];
                                int resn = Integer.parseInt(atomLine[2]);
                                String resId = atomLine[3];
                                String atom = atomLine[4];
                                int cgnr = Integer.parseInt(atomLine[5]);
                                double charge = Double.parseDouble(atomLine[6]);
                                double mass = Double.parseDouble(atomLine[7]);
                                AtomTopology at = new AtomTopology(nr, AtomTopology.getAtomType(type), resn, resId, atom, cgnr, charge, mass);
                                tf.addAtom(at);

                            } catch (NumberFormatException ex) {
                                ConsoleController.log.addMessage("Invalid line in topfile: " + line);
                            }
                        }
                        break;
                        case bonds:
                        case pairs:
                        case angles: {
                            String[] atomLine = line.trim().split("\\s+");

                            int ai = Integer.parseInt(atomLine[0]);
                            int aj = Integer.parseInt(atomLine[1]);
                            int func = Integer.parseInt(atomLine[atomLine.length - 3]);
                            double charge0 = Double.parseDouble(atomLine[atomLine.length - 2]);
                            double charge1 = Double.parseDouble(atomLine[atomLine.length - 1]);

                            if (mode == MODE.bonds) {
                                tf.addBond(new BondTopology(ai, aj, func, charge0, charge1));
                            } else if (mode == MODE.pairs) {
                                tf.addPair(new PairTopology(ai, aj, func, charge0, charge1));
                            } else if (mode == MODE.angles) {
                                int ak = Integer.parseInt(atomLine[2]);
                                tf.addAngle(new AngleTopology(ai, aj, ak, func, charge0, charge1));
                            }

                        }
                        break;
                        case dihedrals: {
                            //diheadrals can have more than 1 charge.
                            String[] atomLine = line.trim().split("\\s+");
                            int ai = Integer.parseInt(atomLine[0]);
                            int aj = Integer.parseInt(atomLine[1]);
                            int ak = Integer.parseInt(atomLine[2]);
                            int al = Integer.parseInt(atomLine[3]);
                            int func = Integer.parseInt(atomLine[4]);
                            double charge0 = Double.parseDouble(atomLine[5]);
                            double charge1 = Double.parseDouble(atomLine[6]);

                            if (atomLine.length == 8) {
                                double charge2 = Double.parseDouble(atomLine[7]);
                                tf.addDiheadral(new DiheadralTopology(ai, aj, ak, al, func, charge0, charge1, charge2));
                            } else {
                                tf.addDiheadral(new DiheadralTopology(ai, aj, ak, al, func, charge0, charge1));
                            }
                        }
                        break;
                        case system:
                            tf.addSystem(line);
                            break;
                        case molecules:
                            tf.addMolecules(line);
                            break;

                    }

                } else {
                    ConsoleController.log.addMessage("Unparsed line: " + line);
                }

            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        return tf;

    }

    public void saveTopFile(String path, TopologyFile tf) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));
            writer.write(tf.toString());

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
