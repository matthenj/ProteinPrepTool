package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	AmberNetCDFTrajectory.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class allows interactions with an Amber NETCDF trajectory. Uses ucar
 *	netcdf library. Extends Trajectory.java, as all trajectories must.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ConsoleController;
import controller.ProteinPrepToolException;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AmberNetCDFTrajectory extends Trajectory {


    private ArrayList<Integer> framesPerFile;
    private boolean framesPerFileIdentical;


    public AmberNetCDFTrajectory(String folderPath) {
        super();
        framesPerFile = new ArrayList<>();


        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles.length < 1 && ConsoleController.verbose) {
            ConsoleController.log.addMessage("Folder is empty!");
        } else {


            ArrayList<String> files = new ArrayList<>();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i].getAbsolutePath());
                }
            }

            processPotentialNCFiles(files);


        }
    }


    public AmberNetCDFTrajectory(ArrayList<String> files) {
        super();
        framesPerFile = new ArrayList<>();


        processPotentialNCFiles(files);

    }

    private void processPotentialNCFiles(ArrayList<String> files) {
        for (String file : files) {
            NetcdfFile ncfile = null;
            try {
                ncfile = NetcdfFile.open(file);

                //if first file, read number of "atoms"
                List<Variable> vars = ncfile.getVariables();

                //spin through and fine the coordinates index.
                int desiredIndex = -1;
                for (int i = 0; i < vars.size(); i++) {
                    Variable var = vars.get(i);
                    String nameAndDimes = var.getNameAndDimensions();
                    if (nameAndDimes.toLowerCase().contains("coordinates")) {
                        desiredIndex = i;
                    }
                }

                if (desiredIndex < 0) {
                    throw new ProteinPrepToolException("Coordinates not found in nc file!");
                }

                Variable coordinates = vars.get(desiredIndex);
                //expect frames x atoms.
                Dimension frames = coordinates.getDimension(0);
                Dimension atoms = coordinates.getDimension(1);

                if (this.numberOfAtoms < 0) {
                    this.numberOfAtoms = atoms.getLength();
                } else if (this.numberOfAtoms != atoms.getLength()) {
                    throw new ProteinPrepToolException("Number of atoms in nc file is not consistant with other nc files.");
                }

                this.numberOfFrames += frames.getLength();
                framesPerFile.add(frames.getLength());
                this.numberOfFiles++;
                this.fileNames.add(file);
                framesPerFileIdentical = (numberOfFrames / fileNames.size()) == framesPerFile.get(0);


            } catch (IOException | ProteinPrepToolException ex) {
                if (ConsoleController.verbose) {
                    ConsoleController.log.addMessage("Ignoring : \"" + file + "\"");
                }

            } finally {
                if (ncfile != null) {
                    try {
                        ncfile.close();
                    } catch (IOException e) {
                        ConsoleController.log.addMessage("nc file closure failure. Curious.");
                    }
                }
            }
        }
    }


    public static void read() throws InvalidRangeException {
        String filename = "/home/njm/scratch/m0001.nc";
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);


            //gets each component of the netcdf file. We want "Cooridnates"
            List<Variable> vars = ncfile.getVariables();


            int desiredIndex = -1;

            if (ConsoleController.headless) {

                //assume we can find something called "Coordinates"

                for (int i = 0; i < vars.size(); i++) {
                    Variable var = vars.get(i);
                    String nameAndDimes = var.getNameAndDimensions();
                    if (nameAndDimes.toLowerCase().contains("coordinates")) {
                        desiredIndex = i;
                    }

                }


            } else {
                System.out.println("Select attribute containing cooridnate information");


            }


            Variable coordinates = vars.get(desiredIndex);
            //expect frames x atoms.
            Dimension frames = coordinates.getDimension(0);
            Dimension atoms = coordinates.getDimension(1);


            System.out.println(desiredIndex);
            System.out.println(frames.getLength());
            System.out.println(atoms.getLength());


            //which frame do you want?
            int[] origin = {1, 0, 0};

            //n x 3
            int[] dims = {1, atoms.getLength(), 3};
            Array traj = coordinates.read(origin, dims);


            //get x1y1z1...xnynzn.
            double[] traj_native = (double[]) traj.get1DJavaArray(double.class);

            for (int i = 0; i < traj_native.length; i++) {
                System.out.println(traj_native[i]);
            }


        } catch (IOException ioe) {
            System.out.println("trying to open " + filename);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("trying to close " + filename);
            }
        }
    }


    @Override
    public double[] getFrame(int frameId) throws ProteinPrepToolException {

        if (this.numberOfFrames < frameId) {
            throw new ProteinPrepToolException("Invalid frame ID! Max: " + frameId);
        }

        int fileID = -1;
        int readId = 0;
        if (framesPerFileIdentical) {
            int framesPerFile = this.framesPerFile.get(0);
            fileID = frameId / framesPerFile;
            readId = frameId % framesPerFile;
        } else {
            //else untested.
            int tFrameId = frameId;

            for (int i = 0; i < this.framesPerFile.size(); i++) {

                if ((tFrameId - this.framesPerFile.get(i)) < 0) {
                    fileID = i;
                    readId = tFrameId;
                } else {
                    tFrameId -= this.framesPerFile.get(i);
                }
            }
        }


        //relevant netcdf file.
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(this.fileNames.get(fileID));

            //gets each component of the netcdf file. We want "Cooridnates"
            List<Variable> vars = ncfile.getVariables();
            int desiredIndex = -1;

            //assume we can find something called "Coordinates"

            for (int i = 0; i < vars.size(); i++) {
                Variable var = vars.get(i);
                String nameAndDimes = var.getNameAndDimensions();
                if (nameAndDimes.toLowerCase().contains("coordinates")) {
                    desiredIndex = i;
                }

            }

            if (desiredIndex == -1) {
                throw new ProteinPrepToolException("Cannot find \"Coordinates\" in AMBER NetCDF File!");
            }


            Variable coordinates = vars.get(desiredIndex);
            //expect frames x atoms.
            Dimension frames = coordinates.getDimension(0);
            Dimension atoms = coordinates.getDimension(1);


            //which frame do you want?
            int[] origin = {readId, 0, 0};
            //n x 3
            int[] dims = {1, atoms.getLength(), 3};
            Array traj = coordinates.read(origin, dims);

            //get x1y1z1...xnynzn.
            double[] traj_native = (double[]) traj.get1DJavaArray(double.class);
            return traj_native;

        } catch (IOException | InvalidRangeException ioe) {
            System.out.println("trying to open " + this.fileNames.get(fileID));
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                System.out.println("trying to close " + this.fileNames.get(fileID));
            }
        }


        throw new ProteinPrepToolException("Frame " + frameId + " not found!");
    }


    public String toString() {
        return "Number of Atoms: " + numberOfAtoms + " | Number of Frames " + numberOfFrames + "  | Equal Files: " + ((framesPerFileIdentical) ? "Yes" : "No");
    }


}
