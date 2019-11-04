
package controller;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*******************************************************************************
 *
 *	Filename   :	PDBReaderWriter.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class contains static methods that can be used to load and save PDBFiles.
 *	Only handles HETATM & ATOM lines. All others are ignored.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class PDBReaderWriter {


    public static PDBFile loadPdbFile(String path) {

        PDBFile file = new PDBFile();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {

            String line;

            int atoms = 0;
            int models = 0;

            while ((line = br.readLine()) != null) {

                if (line.startsWith("ATOM") || line.startsWith(("HETATM"))) {
                    if (models == 0) {
                        models++;
                    }

                    if (models > 1) {

                    } else {
                        atoms++;
                        try {
                            Atom atm = new Atom(line);
                            file.addAtom(atm);
                        } catch (ProteinPrepToolException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Don't care about later models.
                if (line.startsWith("MODEL")) {

                    models++;
                    if (models > 1) {
                        ConsoleController.log.addMessage("Multiple models found, model " + models
                                + " ignored.");
                    }
                }
            }

            ConsoleController.log.addMessage("Read " + atoms + " atoms and " + models + " models " +
                    "( " + (models - 1) + " models ignored.)");

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        return file;
    }


    public static void savePDBFile(String path, PDBFile file) {

        // If the file doesn't exists, create and write to it
        // If the file exists, truncate (remove all content) and write to it
        try (FileWriter writer = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            if (ConsoleController.verbose) {
                ConsoleController.log.addMessage("Saving PDB file to: " + path);
            }

            bw.write(file.generateStringForPrinting());

            if (ConsoleController.verbose) {
                ConsoleController.log.addMessage("Save complete.");
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

}
