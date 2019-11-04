
package controller;

import controller.TopFile.GromacsTopFileReaderWriter;
import controller.TopFile.NamdPsfLoader;
import controller.TopFile.TopologyFile;
import gui.MainGUI;
import gui.ProgressWindow;
import ucar.ma2.InvalidRangeException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;




public class ConsoleController {


    public static final long BYTES_IN_DOUBLE = 8;
    public static final long BYTES_IN_MB = 1000000;


    public static final boolean TEST_MODE = true;
    //Add additional Topology formats to this ENUM
    public enum TOP_FORMAT {TOP, PRMTOP, PSF};

    //Add additional trajectory formats to this ENUM
    public enum TRAJ_FORMAT {NETCDF};

    //Add new messages for the ConsoleControllerThread to this enum.
    public enum MESSAGE {START, EXIT, ERROR, COMPUTE_STOP_FAILED};

    public static boolean verbose = false;
    public static PPTLogger log;
    public static boolean headless = false;
    public static int threadLimit = 8;


    public static boolean memoryOverride;
    public static boolean isWindows;
    public static boolean computing = true;
    public static boolean computeThreadRunning = false;

    public static MainGUI gui = null;
    public static Thread guiThread = null;
    public static Thread computeThread = null;
    public static String applicationName = "ProteinPrepTool";

    public static HaptimolPreProcessor hpp;
    public static ProgressWindow progressWindow;

    //send messages to the main thread.
    private static ConcurrentLinkedQueue<MESSAGE> postBox;
    private static Thread mainThread;
    private static boolean postBoxOpen = false;

    //Main thread should run for the duration of the program.
    public static void main(String[] args) throws ProteinPrepToolException, InvalidRangeException, InterruptedException {


        //logger class (Basic Stringbuilder wrapper)
//        verbose = true;
//        log = new PPTLogger();
//
//        NamdPsfLoader sr = new NamdPsfLoader();
//
//        TopologyFile tf = sr.loadTopFile("test_data/testNamdFile.psf");
//
//        GromacsTopFileReaderWriter gtfrw = new GromacsTopFileReaderWriter();
//        gtfrw.saveTopFile("test_data/convertedNamdFile.top", tf);
//
//
//
//        if(true)
//            return;


        //used to allow other threads to communicate with the main thread
        postBox = new ConcurrentLinkedQueue();

        //Worker class:- performs all computation
        hpp = new HaptimolPreProcessor();
        //logger class (Basic Stringbuilder wrapper)
        log = new PPTLogger();

        //reference to this thread.
        mainThread = Thread.currentThread();

        //allows closing of postbox when shutdown has begun
        postBoxOpen = true;

        //window shown during processing.

        //If no args, running with gui, else run headless.
        if (args.length == 0) {
            //reduce the number of log messages output.
            verbose = true;
            progressWindow = new ProgressWindow();
            gui = new MainGUI();
            gui.run();

            //busy loop keeps thread running, and handling interthread comms.
            while (computing) {
                try {
                    while (postBox.size() > 0 && computing) {
                        handleEvent(postBox.poll());
                    }
                    //long sleep: thread will be interupted after new message has been posted.
                    sleep(10000);
                } catch (InterruptedException iex) {
                    //do nothing.
                }
            }


        } else {
            //TODO: Console mode :- atm set up for debugging only.

            headless = true;

            Thread t = new Thread(new MemoryMonitor());
            t.start();
            threadLimit = 8;

            try {

                isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
                ArrayList<String> trajectoryFrames = new ArrayList<>();
                String trajectoryFolder = "";
                String pdbPath = "";
                String topPath = "";
                String avStructurePath = "";
                String topSavePath = "";
                String eigenValueSavePath = "";
                String eigenVectorSavePath = "";
                String forcefieldPath = "";
                String ffName;
                int desiredEigs = 0;

                if (TEST_MODE) {
                    verbose = true;
                    headless = true;
                    memoryOverride = true;

                    if (isWindows) {

                        pdbPath = "D:\\njm\\Protein_Movement_Data\\1GGG\\trajectory\\m0001.pdb";
                        topPath = "D:\\njm\\Protein_Movement_Data\\1GGG\\trajectory\\1ggg.pro.prmtop";
                        trajectoryFrames.add("D:\\njm\\Protein_Movement_Data\\1GGG\\trajectory\\m0021.nc");
                        trajectoryFolder = "D:\\njm\\Protein_Movement_Data\\1GGG\\trajectory";
                    } else {
                        pdbPath = "/home/njm/scratch/trajectory/m0001.pdb";
                        topPath = "/home/njm/scratch/trajectory/1ggg.pro.prmtop";
                        trajectoryFrames.add("/home/njm/scratch/trajectory/m0021.nc");
                        trajectoryFolder = "";
                    }

                    avStructurePath = "D:\\njm\\scratch\\avStructure.pdb";
                    topSavePath = "D:\\njm\\scratch\\topSavePath.top";
                    eigenValueSavePath = "D:\\njm\\scratch\\eigenvals.csv";
                    eigenVectorSavePath = "D:\\njm\\scratch\\eigenvecs.csv";
                    desiredEigs = 16;
                    forcefieldPath = "C:\\njm\\StuffForFlexiDock_RC\\ff\\amber03.ff\\ffnonbonded.itp";
                    ffName = "amber03.ff";

                } else {

                }

                long st = System.nanoTime();

                HaptimolPreProcessor hpp = new HaptimolPreProcessor();
                hpp.initialise(pdbPath, topPath, trajectoryFolder, avStructurePath, topSavePath, eigenValueSavePath, eigenVectorSavePath, desiredEigs + "", TOP_FORMAT.PRMTOP, TRAJ_FORMAT.NETCDF, ffName, forcefieldPath);

                // prepareForHaptimol(pdbPath, topPath, trajectoryFrames, TOP_FORMAT.PRMTOP, TRAJ_FORMAT.NETCDF,
                //avStructurePath, topSavePath, eigenValueSavePath, eigenVectorSavePath, desiredEigs, trajectoryFolder);

                long ft = System.nanoTime();
                log.addMessage("Execution time:" + ((ft - st) / 1E9));


                log.addMessage("Complete.");
                log.saveLogToFile("D:\\njm\\scratch\\verbose_log.log");

            } catch (java.lang.OutOfMemoryError e) {

                t.interrupt();
                log.addMessage(e.toString());
                log.saveLogToFile("C:\\njm\\scratch\\crashlog.log");
                computing = false;
                t.interrupt();
            }
        }

        //execution finished.

    }

    //
    static void handleEvent(MESSAGE msg) {

        if (msg == null) {
            return;
        }

        switch (msg) {
            case START:
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ProgressWindow.createandshowUI(progressWindow);
                    }
                });

                computeThread = new Thread(hpp);
                computeThread.start();
                break;
            case EXIT:

                postBoxOpen = false;

                //need to post a "cleanup" message if computeThread is running...
                if (computeThreadRunning) {
                    //
                }

                gui.endGUI();
                computing = false;
                progressWindow.cleanExit(false);
                log.saveLogToFile("C:\\njm\\scratch\\completelog.log");
                break;
            case COMPUTE_STOP_FAILED:
                gui.show();
                break;
        }
    }

    /**
     * Send message to main control thread
     * @param msg - Message to be sent.
     */
    public static void postMessage(MESSAGE msg) {

        if (postBoxOpen) {
            postBox.offer(msg);
            mainThread.interrupt();
        }


    }



    /**
     * Worker class that can be used to monitor memory usage during execution.
     */
    private static class MemoryMonitor implements Runnable {

        long maxMemory = 0;


        @Override
        public void run() {

            while (ConsoleController.computing) {


                //available heap size.
                long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

                if (allocatedMemory > maxMemory) {
                    maxMemory = allocatedMemory;
                }

                ConsoleController.log.addMessage("Current Memory Allocated: " + allocatedMemory / BYTES_IN_MB + "MB   Free Memory: " + presumableFreeMemory / BYTES_IN_MB + "MB");

                try {
                    sleep(50000);
                } catch (InterruptedException e) {
                    allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                    presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

                    if (allocatedMemory > maxMemory) {
                        maxMemory = allocatedMemory;
                    }

                    ConsoleController.log.addMessage("Current Memory Allocated: " + allocatedMemory / BYTES_IN_MB + "MB   Free Memory: " + presumableFreeMemory / BYTES_IN_MB + "MB");

                }

            }

        }
    }


}
