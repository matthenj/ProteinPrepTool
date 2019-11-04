package controller;

import controller.TopFile.*;
import controller.pdbTopTrajFFVerification.ErrorRecoverer;
import controller.pdbTopTrajFFVerification.ProteinVerifier;
import controller.pdbTopTrajFFVerification.ProteinVerifierFactory;
import controller.pdbTopTrajFFVerification.TestReport;
import controller.Trajectory.*;
import controller.Trajectory.matrix3p.DenseMatrix;
import controller.Trajectory.matrix3p.Vector;
import controller.Trajectory.matrix3p.eigenvalues.Eigenvalues;
import controller.forcefields.Forcefield;
import org.apache.commons.math3.linear.RealMatrix;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

import static controller.ConsoleController.*;
import static java.lang.Thread.sleep;

/*******************************************************************************
 *
 *	Filename   :	HaptimolPreProcessor.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class performs all the processing required to prepare a PDB/TOP/TRAJ for
 *	use with Haptimol FlexiDock.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class HaptimolPreProcessor implements Runnable {


    private String pdbPath;
    private String topPath;
    private String trajectoryFolder;
    private String nvsSavePath;
    private String topSavePath;
    private String eigenValueSavePath;
    private String eigenVectorSavePath;
    private String forceFieldPath;
    private String ffName;

    private boolean testAgainstForcefield;

    private ArrayList<String> trajectoryFrames;
    private int numberOfEigs;

    private ConsoleController.TOP_FORMAT topFormat;
    private ConsoleController.TRAJ_FORMAT trajFormat;

    private int progress;

    private int smallestRmsdIndex;
    private double smallestMSD;
    private double[] nearestViableStructure;


    public HaptimolPreProcessor() {
        pdbPath = null;
        topPath = null;
        trajectoryFolder = null;
        nvsSavePath = null;
        topSavePath = null;
        eigenValueSavePath = null;
        eigenVectorSavePath = null;
        forceFieldPath = null;
        numberOfEigs = -1;
        progress = 0;

    }

    private void progress(int percent) {
        if (percent > 0) {
            progress = percent;
        }
        ConsoleController.progressWindow.updateProcess(progress);
    }


    //Method tests, each path to see if it's acceptable. if yes, assingmenet takes place, else failure.
    public void initialise(String pdbPath, String topPath, String trajectoryFolder, String avStructurePath,
                           String topSavePath, String eigenValueSavePath, String eigenVectorSavePath, String numberOfEigs, ConsoleController.TOP_FORMAT topf,
                           ConsoleController.TRAJ_FORMAT trajf, String ffName, String forceFieldPath) throws ProteinPrepToolException {

        StringBuilder err = new StringBuilder();
        int errored = 0;

        String[] trajFolder;

        if (forceFieldPath == null) {
            testAgainstForcefield = false;
        } else {
            testAgainstForcefield = true;
        }

        try {

            try {
                examinePath(pdbPath);
            } catch (ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }

            try {
                examinePath(topPath);
            } catch (
                    ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }


            trajFolder = trajectoryFolder.split(",");

            for (int i = 0; i < trajFolder.length; i++) {
                if (trajFolder[i].isEmpty()) {
                    continue;
                }
                try {
                    examinePath(trajFolder[i].trim(), true);
                } catch (
                        ProteinPrepToolException e) {
                    err.append(e.getFriendlyMessage());
                    errored++;
                }

            }


            try {
                examinePath(avStructurePath, false, false, false);
            } catch (
                    ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }

            try {
                examinePath(topSavePath, false, false, false);
            } catch (
                    ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }

            try {
                examinePath(eigenValueSavePath, false, false, false);
            } catch (
                    ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }

            try {
                examinePath(eigenVectorSavePath, false, false, false);
            } catch (
                    ProteinPrepToolException e) {
                err.append(e.getFriendlyMessage());
                errored++;
            }

            if (testAgainstForcefield) {
                try {
                    examinePath(forceFieldPath, false, true, false);
                } catch (
                        ProteinPrepToolException e) {
                    err.append(e.getFriendlyMessage());
                    errored++;
                }
            }

            boolean valid = checkRootDirectoryExists(avStructurePath);

            if (!valid) {
                err.append(avStructurePath).append(": parent directory not found.");
                errored++;
            }


            valid = checkRootDirectoryExists(topSavePath);
            if (!valid) {
                err.append(avStructurePath).append(": parent directory not found.");
                errored++;
            }
            valid = checkRootDirectoryExists(eigenValueSavePath);
            if (!valid) {
                err.append(avStructurePath).append(": parent directory not found.");
                errored++;
            }

            valid = checkRootDirectoryExists(eigenVectorSavePath);
            if (!valid) {
                err.append(avStructurePath).append(": parent directory not found.");
                errored++;
            }

            try {
                int numberOfEigsN = Integer.parseInt(numberOfEigs);
                if (numberOfEigsN < 1) {
                    throw new ProteinPrepToolException("NegativeEigs");
                }
            } catch (NumberFormatException exp) {
                err.append(numberOfEigs).append(" is an invalid number of Eigenvalues");
                errored++;
            } catch (ProteinPrepToolException pex) {
                err.append(numberOfEigs).append(" is an invalid number of Eigenvalues");
                errored++;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ProteinPrepToolException("Issues found. Please retry...", "Please complete the form", true);
        }
        if (errored > 0) {
            throw new ProteinPrepToolException("Issues found. Please retry...", err.toString(), true);
        }


        this.pdbPath = pdbPath;
        this.topPath = topPath;
        this.nvsSavePath = avStructurePath;
        this.topSavePath = topSavePath;
        this.eigenValueSavePath = eigenValueSavePath;
        this.eigenVectorSavePath = eigenVectorSavePath;
        this.numberOfEigs = Integer.parseInt(numberOfEigs);
        this.topFormat = topf;
        this.trajFormat = trajf;
        this.forceFieldPath = forceFieldPath;
        this.ffName = ffName;

        trajectoryFrames = new ArrayList<>();
        for (String s : trajFolder) {
            trajectoryFrames.add(s.trim());
        }

    }


    private boolean checkRootDirectoryExists(String path) {

        if (path.length() == 0)
            return false;

        File f = new File(path);
        if (f.isDirectory()) {
            return false;
        }

        File fp = new File(f.getParent());

        if (fp.exists())
            return true;
        else
            return false;

    }

    private void examinePath(String path) throws ProteinPrepToolException {
        examinePath(path, false, true, false);
    }

    private void examinePath(String path, boolean canBeDir) throws ProteinPrepToolException {
        examinePath(path, canBeDir, true, false);
    }


    private void examinePath(String path, boolean canBeDirectory, boolean mustExist, boolean emptyDirectoryAllowed) throws ProteinPrepToolException {

        boolean errorFound = false;
        boolean fatalFound = false;
        StringBuilder err = new StringBuilder();
        StringBuilder fatal = new StringBuilder();

        File f = new File(path);

        if (f.exists() && !mustExist) {
            errorFound = true;
            err.append(path).append(": ").append("Save file already exists. Please input a new filename.").append(System.lineSeparator());
        } else if (!f.exists() && mustExist) {
            fatalFound = true;
            fatal.append(path).append(": ").append("Not found. Please select valid file.").append(System.lineSeparator());
        }

        if (f.isDirectory() && !canBeDirectory) {
            fatalFound = true;
            fatal.append(path).append(": ").append("Is a directory. Please select valid file.").append(System.lineSeparator());
        }

        if (f.isDirectory() && (f.listFiles().length == 0 && !emptyDirectoryAllowed)) {
            fatalFound = true;
            fatal.append(path).append(": ").append("Is an empty directory. Please select valid folder.").append(System.lineSeparator());
        }

        if (fatalFound) {
            throw new ProteinPrepToolException(fatal.toString(), fatal.toString(), true);
        }

        if (errorFound) {
            throw new ProteinPrepToolException(err.toString(), err.toString(), false);
        }


    }


    public PDBFile loadPDBFile() {
        //Load the reference structure.
        PDBReaderWriter pdbReadWrite = new PDBReaderWriter();
        PDBFile pdb = pdbReadWrite.loadPdbFile(pdbPath);

        return pdb;
    }

    public void savePDBFile(PDBFile base, double[] coordinates) throws ProteinPrepToolException {
        PDBFile closestToAverage = new PDBFile(base, coordinates);
        PDBReaderWriter pdbReadWrite = new PDBReaderWriter();
        pdbReadWrite.savePDBFile(this.nvsSavePath, closestToAverage);
    }


    public TopologyFile loadTopologyFile(String forcefieldName) throws ProteinPrepToolException {
        TopFileLoader tfl;
        TopologyFile top;

        switch (topFormat) {
            case TOP:
                tfl = new GromacsTopFileReaderWriter();
                break;
            case PRMTOP:
                tfl = new AmberPrmTopFileReader();
                break;
            case PSF:
                tfl = new NamdPsfLoader();
                break;
            default:
                throw new ProteinPrepToolException("UNKNOWN TOP FILE FORMAT");
        }

        top = tfl.loadTopFile(topPath);
        top.setFFName(forcefieldName);
        return top;
    }

    public Forcefield loadForcefield() {
        return new Forcefield(new File(forceFieldPath));
    }

    public double[] determineAverageStructure(PDBFile pdb, TopologyFile top, Trajectory traj) throws ProteinPrepToolException {
        if (verbose) {
            log.addMessage("Determining average structure... ");
        }
        MassWeightedSuperImposition mwsi = new MassWeightedSuperImposition(pdb.getCoordsAsVector(), top);
        AverageStructureCalculator asc = new AverageStructureCalculator(pdb.getNumberOfAtoms());

        for (int i = 0; i < traj.getNumberOfFrames(); i++) {
            double[] superimposed = mwsi.superposition(traj.getFrame(i));
            asc.addPose(superimposed);
        }

        this.progress(30);

        return asc.getAverageStructure();
    }

    public int getClosestToAverageStructure(Trajectory traj, double[] avStructure) throws ProteinPrepToolException {
        double smallestRMSD = ProteinTools.MeanSquaredDeviation(avStructure,traj.getFrame(0));
        int smallestRMSDIndex = 0;

        for (int i = 0; i < 10; i++) {
            System.out.print(avStructure[i] + ", ");
        }

        System.out.println();
        for (int i = 1; i < traj.getNumberOfFrames(); i++) {

            double d = ProteinTools.MeanSquaredDeviation(avStructure, traj.getFrame(i));

            if(i >= 0 &&  i < 10){
                log.addMessage("i: " + i + "msd: " + d);
            }

            if (smallestRMSD > d) {
                smallestRMSD = d;
                smallestRMSDIndex = i;
            }
        }

        log.addMessage("Smallest RMSD Index is: " + smallestRMSDIndex);

        return smallestRMSDIndex;
    }

    public RealMatrix findCovarianceMatrix(TopologyFile top, Trajectory traj, int frameSize, double[] averageStructure) throws ProteinPrepToolException {

        TrajectoryStorageBuffer tsb = new TrajectoryStorageBuffer(frameSize);
        MassWeightedSuperImposition mwsi = new MassWeightedSuperImposition(averageStructure, top);
        AverageStructureCalculator asc = new AverageStructureCalculator(traj.getNumberOfAtoms());

        double smallestRMSD = ProteinTools.MeanSquaredDeviation(averageStructure,mwsi.superposition(traj.getFrame(0)));
        int smallestRmsdID = 0;

        //refit to average structure.
        for (int i = 0; i < traj.getNumberOfFrames(); i++) {
            double[] superimposed = mwsi.superposition(traj.getFrame(i));
            tsb.addFrame(superimposed);
            asc.addPose(superimposed);
            double tmp = ProteinTools.MeanSquaredDeviation(averageStructure,superimposed);

            if(tmp < smallestRMSD){
                smallestRMSD = tmp;
                smallestRmsdID = i;
                this.nearestViableStructure = superimposed;
            }


        }

        tsb.subtractFromTrajectory(asc.getAverageStructure());

        if (verbose) {
            log.addMessage("Creating covariance matrix..");
        }

        this.progress(45);


        //calculate cov. matrix
        CovarianceCalculator cov = new CovarianceCalculator(tsb);

        //clear memory
        mwsi.freeMem();
        asc = new AverageStructureCalculator(0);

        RealMatrix covmat = cov.calculateCovariance();




        this.smallestMSD = smallestRMSD;
        this.smallestRmsdIndex = smallestRmsdID;




        tsb.cleanup();

        return covmat;
    }

    public Eigenvalues calculateEigenvalues(RealMatrix covarianceMatrix) {
        DenseMatrix mat = new DenseMatrix(covarianceMatrix.getData());
        Eigenvalues ev = Eigenvalues.of(mat);
        ev.largest(numberOfEigs);
        ev.run();


        return ev;

    }


    public void sortAndSaveEigenvalues(int numberOfAtoms, Eigenvalues ev) throws ProteinPrepToolException {
        double[] correctOrder = new double[numberOfEigs];
        for (int i = 0; i < numberOfEigs; i++) {
            correctOrder[i] = ev.value[(numberOfEigs - 1 - i)];
        }

        VectorTools.saveVectorAsCSV(eigenValueSavePath, correctOrder);
        //free that memory.
        correctOrder = new double[0];

        Vector[] evec = ev.vector;

        this.progress(93);

        double[][] testMat = new double[numberOfAtoms * 3][numberOfEigs];

        for (int i = 0; i < numberOfEigs; i++) {
            Vector v = evec[i];
            for (int j = 0; j < v.size(); j++) {
                testMat[j][numberOfEigs - 1 - i] = v.get(j);
            }
        }
        MatrixTools.saveMatrixAsCSV(eigenVectorSavePath, testMat);
    }


    private void fitAndCalculateRMSD(double[] avstruct, TopologyFile top, double[] closestToAverageStructure){
        try {
            MassWeightedSuperImposition mwsi = new MassWeightedSuperImposition(avstruct, top);
            double[] fitted = mwsi.superposition(closestToAverageStructure);
            double d = ProteinTools.MeanSquaredDeviation(avstruct, fitted);
            double rmsd = Math.sqrt(d);
            String rmsdStr = String.format("%.3f", rmsd);

            log.addMessage("RMSD of the closest to average structure is: " + rmsdStr );

        } catch (ProteinPrepToolException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        ConsoleController.computeThreadRunning = true;

        PDBFile pdb = null;
        TopologyFile top = null;
        int numberOfAtoms = 0;
        int frameSize = 0;
        Trajectory traj = null;
        ErrorRecoverer err = null;
        double exeStartTime = System.nanoTime();
        try {



            pdb = loadPDBFile();
            this.progress(5);

            //Load topology.
            top = loadTopologyFile(ffName);

            //check TOP and PDB align.
            this.progress(8);

            //Estimate memory consumption.
            //If heap is too small, quit (unless overridden).
            numberOfAtoms = pdb.getNumberOfAtoms();
            frameSize = checkMemoryAndConfigureMemory(numberOfAtoms, numberOfEigs);

            if (TEST_MODE && verbose) {
                log.addMessage("Frame size: " + frameSize);
            }

            //load trajectory and calculate average structure.

            switch (trajFormat) {
                case NETCDF:
                    traj = new AmberNetCDFTrajectory(trajectoryFrames);
                    break;
                default:
                    throw new ProteinPrepToolException("Unidentified Trajectory Format.");
            }

            this.progress(10);

            if (verbose) {
                log.addMessage("Testing files for compatibility...");
            }


            Forcefield ff = null;

            if (testAgainstForcefield) {
                ff = loadForcefield();
                ff.loadFile();

            }


            TestReport tr = verifyFileCompatibility(pdb, top, traj, ff);

            if (tr.anyFailures()) {

                if (headless) {
                    //headless, not much we can do:- Exit.
                    throw new ProteinPrepToolException(tr.printFailMessages());
                } else if (tr.hasFatal()) {
                    throw new ProteinPrepToolException(tr.printFatalMessages());
                } else {

                    //Handle those errors...
                    if (ConsoleController.threadLimit > 1) {
                        ConsoleController.threadLimit--;
                    }

                    err = new ErrorRecoverer(tr);
                    err.handle();

                }


            }

        } catch (ProteinPrepToolException ex) {
            //Dialogue message highlighting error
            //ifstop, post and return
            JOptionPane.showMessageDialog(null, ex.getFriendlyMessage());
            ConsoleController.postMessage(ConsoleController.MESSAGE.COMPUTE_STOP_FAILED);
            return;

            //else continue
        }

        //else continue with computation
        try {


            this.progress(20);


            double[] avStructure = this.determineAverageStructure(pdb, top, traj);

            if (verbose) {
                log.addMessage("Finding closest to average real structure... ");
            }

            //determine smallest RMSD compared to average.
            this.progress(35);






            if (verbose) {
                log.addMessage("Fitting to average structure... ");
            }


            RealMatrix covMat = this.findCovarianceMatrix(top, traj, frameSize, avStructure);
            this.progress(68);

            //find and save closest to average structure
            this.savePDBFile(pdb, this.nearestViableStructure);
            String rmsdStr = String.format("%.3f", Math.sqrt(this.smallestMSD));
            log.addMessage("The Smallest RMSD is approximately : " + System.lineSeparator() + rmsdStr + " pose number ( " + this.smallestRmsdIndex + " ) ");
            this.progress(70);


            //request garbage collection.
            System.gc();

            if (verbose) {
                log.addMessage("Calculate Eigendecomposition..");
            }
            Eigenvalues ev = calculateEigenvalues(covMat);
            this.progress(90);

            if (err != null && !err.errorHandlingComplete()) {
                boolean logMessagePrinted = false;
                ConsoleController.log.addMessage("Waiting for Error handling to complete (User input)");

                while (!err.errorHandlingComplete()) {
                    sleep(1000);
                }


            }

            //Save topology file.
            GromacsTopFileReaderWriter tmp = new GromacsTopFileReaderWriter();
            tmp.saveTopFile(this.topSavePath, top);

            this.sortAndSaveEigenvalues(numberOfAtoms, ev);

            log.addMessage("Complete!");
            this.progress(100);


        } catch (Exception e) {

            log.addMessage(e.toString());
            log.addMessage("FAILED.");
            log.saveLogToFile("C:\\njm\\scratch\\crashlog.log");

        } finally {

            ConsoleController.computeThreadRunning = false;
        }

        double exeEndTime = System.nanoTime();

        double rt = (exeEndTime - exeStartTime)/1E9;
        log.addMessage("Execution time: " + rt + " seconds.");


//        ConsoleController.postBox.offer(ConsoleController.MESSAGE.EXIT);
    }        //Save Topology in TOP format.


    public TestReport verifyFileCompatibility(PDBFile pdb, TopologyFile top, Trajectory traj, Forcefield f) throws ProteinPrepToolException {


        ProteinVerifierFactory pvf = new ProteinVerifierFactory();
        ProteinVerifier pv;
        if (f == null) {
            pv = pvf.ProteinVerifierFactory(pdb, top, traj);
        } else {
            pv = pvf.ProteinVerifierFactory(pdb, top, traj, f);
        }

        TestReport tr = pv.runTests();


        return tr;


    }


    private static int checkMemoryAndConfigureMemory(int numberOfAtoms, int numberOfEigs) throws ProteinPrepToolException {

        /*at the very least, the covariance matrix, the square matrix of eigenvalues and the matrix of eigenvectors
        will be in memory at any one time.*/

        double numberOfDoubles = numberOfAtoms * 3;

        long numberOfCopies = 0;

        //cov matrix construction
        numberOfCopies++;

        //cov matrix copy;
        numberOfCopies++;

        //eigenvectors construction
        numberOfCopies++;
        numberOfCopies++;

        //eigenvalues construction
        numberOfCopies++;
        numberOfCopies++;


        //covmatrix + eigenvector matrix + eigenvalue matrix
        long minHeapSize = (long) (numberOfDoubles * numberOfDoubles) * numberOfCopies * BYTES_IN_DOUBLE;


        //add copies of eigenvectors and eigenvalues
        minHeapSize += ((numberOfEigs * numberOfEigs) + (numberOfEigs * numberOfDoubles) * BYTES_IN_DOUBLE);

        //add 10% (just to be sure)
        minHeapSize += minHeapSize * 0.1;

        log.addMessage("Estimated required memory: " + minHeapSize / BYTES_IN_MB + "mb");

        //available heap size.
        long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;


        if (presumableFreeMemory >= minHeapSize) {
            //all good.
        } else if (presumableFreeMemory < minHeapSize && ConsoleController.memoryOverride) {
            boolean cont = warnMemory();
            if (cont) {
                ConsoleController.log.addMessage("Heap size may be too small, continuing.");
            }
        } else {
            failMemory(minHeapSize);
        }

        //determine buffer block size. Try 24% of available memory.
        double frameSizeBytes = numberOfAtoms * 3 * (BYTES_IN_DOUBLE);

        double maxFrameMemory = presumableFreeMemory * 0.24;

        double numFrames = maxFrameMemory / frameSizeBytes;

        numFrames = (numFrames % 2 == 0) ? numFrames : numFrames - 1;

        if (numFrames < 10)
            throw new ProteinPrepToolException("Not enough memory!");


        return (int) numFrames;

    }


    private static boolean warnMemory() {
        if (headless) {
            System.out.println("There probably isn't enough memory available to finish. Continue anyway?");
        } else {

        }


        return true;
    }

    static void failMemory(long minMem) throws ProteinPrepToolException {
        if (headless) {
            throw new ProteinPrepToolException("Predicted memory consumption is higher than available memory. Increase heap size to at least: " + (minMem / BYTES_IN_MB) + "MB with -Xmx");
        } else {

        }
    }

    public int getProgress() {
        return progress;

    }


}
