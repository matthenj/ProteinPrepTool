package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	CovarianceCalculator.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class can be used to calculate the covariance of a
 *	TrajectoryStorageBuffer object. Designed to work with small amounts of
 *	memory, and in parallel.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ConsoleController;
import controller.ProteinPrepToolException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class CovarianceCalculator {


    private TrajectoryStorageBuffer tsb;
    private RealMatrix cachedCov;
    private boolean matrixCached;
    private int numThreads;

    private class ccMultiplier implements Runnable {

        TrajectoryGroup tg;
        int startRow, endRow;
        double[][] covMat;
        int numAtoms;

        public ccMultiplier(int numAtoms) {
            tg = null;
            this.numAtoms = numAtoms;
            startRow = -1;
            endRow = -1;
            covMat = null;
        }

        public void setupRun(TrajectoryGroup group, int sr, int er, double[][] covMat) {


            this.tg = group;
            this.startRow = sr;
            this.endRow = er;
            this.covMat = covMat;


        }


        @Override
        public void run() {

            try {

                for (int rowI = startRow; rowI < endRow; rowI++) {

                    double[] row = tg.getTrajectoryFrame(rowI);
                    for (int colI = 0; colI < numAtoms; colI++) {
                        double[] col = tg.getTrajectoryFrame(colI);
                        for (int i = 0; i < col.length; i++) {
                            covMat[rowI][colI] += row[i] * col[i];
                        }
                    }
                }
            } catch (ProteinPrepToolException ex) {

                System.err.println("Start row: " + startRow + ", end row: " + endRow);
                ex.printStackTrace();
            }
        }
    }


    public CovarianceCalculator(TrajectoryStorageBuffer tsb) throws ProteinPrepToolException {

        if (tsb == null || tsb.getNumberOfFrames() == 0) {
            throw new ProteinPrepToolException("Invalid trajectory.");
        }

        numThreads = ConsoleController.threadLimit - 1;
        numThreads = (numThreads < 1) ? 1 : numThreads;

        this.tsb = tsb;
        matrixCached = false;
    }


    public void clearCachedMatrix() {
        if (matrixCached) {
            cachedCov = new Array2DRowRealMatrix();
            matrixCached = false;
        }
    }

    public RealMatrix calculateCovariance() throws ProteinPrepToolException {


        int dimension = tsb.getNumberOfElementsPerFrame();


        //careful thought needed here. Maximum of two "blocks" in memory at any one time.
        int xDim = tsb.getNumberOfFrames();
        int numAtoms = dimension;
        int numGroups = tsb.getNumberOfGroups();
        int framesPerGroup = tsb.getGroupSize();


        //each group is a block of (atoms * 3) * framesPerGroup.

        double[][] tmp = new double[numAtoms][numAtoms];

        for (int i = 0; i < numAtoms; i++) {
            for (int j = 0; j < numAtoms; j++) {
                tmp[i][j] = 0.0;
            }
        }

        int lookupFrame = 0;


        ccMultiplier[] ccMultipliers = new ccMultiplier[numThreads];
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            ccMultipliers[i] = new ccMultiplier(numAtoms);
            //     threads[i] = new Thread(ccMultipliers[i]);
        }


        for (int grp = 0; grp < numGroups; grp++) {
            TrajectoryGroup tg = this.tsb.getTrajGroup(grp);
            if (tg.getLength() < 1)
                continue;

            long st = System.nanoTime();

            tg.transpose();

            long ttt = System.nanoTime();


            int workPerThread = numAtoms / numThreads;
            int finalthreadbonuswork = numAtoms % numThreads;

            int start = 0;


            for (int i = 0; i < numThreads; i++) {
                ccMultiplier cc = ccMultipliers[i];
                int end = start + workPerThread;

                end = (i + 1 == numThreads) ? end + finalthreadbonuswork : end;

                cc.setupRun(tg, start, end, tmp);
                start = end;
            }

            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(ccMultipliers[i]);
                threads[i].start();
            }

            for (int i = 0; i < numThreads; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            long ft = System.nanoTime();

            double transposeTime = (ttt - st) / 1e9;
            double computTime = (ft - ttt) / 1e9;

//            ConsoleController.log.addMessage("Transpose time: " + transposeTime + "s, Compute time: " + computTime + "s");


            tg.clearWithoutSaving();
        }

        for (int rowI = 0; rowI < numAtoms; rowI++) {
            for (int colI = 0; colI < numAtoms; colI++) {
                tmp[rowI][colI] /= tsb.getNumberOfFrames();
            }
        }


        RealMatrix cov = new Array2DRowRealMatrix(tmp);
        cachedCov = cov;
        return cachedCov;
    }


}
