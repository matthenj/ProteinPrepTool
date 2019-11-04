package controller.Trajectory.tests

/*******************************************************************************
 *
 *	Filename   :	CovarianceCalculatorTest.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class designed to test the basic funcationality of the
 *	CovarianceCalculator class.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.Trajectory.CovarianceCalculator
import controller.Trajectory.TrajectoryStorageBuffer
import org.apache.commons.math3.linear.RealMatrix

class CovarianceCalculatorTest extends GroovyTestCase {


    TrajectoryStorageBuffer tsb;
    int numAtoms = 3431*3;



    void setUp() {
        super.setUp();

        FileReader fr = null;
        BufferedReader br = null;


        tsb = new TrajectoryStorageBuffer();

        try {
            fr = new FileReader("test_data/processedTraj1GGG.csv");
            br = new BufferedReader(fr);

            String line = null;
            while ((line = br.readLine()) != null) {
                double[] tmp = new double[numAtoms];
                String[] dataArr = line.split(",");

                if(line.isEmpty())
                    continue;

                for (int i = 0; i < numAtoms; i++) {
                    tmp[i] = Double.parseDouble(dataArr[i]);
                }
                tsb.addFrame(tmp);
            }



        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            if (br != null) {
                br.close();
            }
            if(fr != null) {
                fr.close();
            }
        }

    }


    void tearDown() {

        tsb = new TrajectoryStorageBuffer();

    }

    boolean compareMatricies(double[][] a, double[][] b){


        //if matrix dimensions are different, return false.
        if(a.length != b.length)
            return false;

        //check matricies are square.
        for (int i = 0; i < a.length; i++) {
            if(a.length != a[i].length)
                return false;

            if(b.length != b[i].length)
                return false;
        }

        //check arrays are identical.
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length ; j++) {
                if(Math.abs(a[i][j] - b[i][j]) > 0.0001){
                    println (a[i][j] + ", " + b[i][j]);
                    return false;
                }
            }
        }


        return true;
        
    }


    void testCalculateCovariance() {

        CovarianceCalculator cc = new CovarianceCalculator(tsb);
        RealMatrix tst = cc.calculateCovariance();



        FileReader frSol = null;
        BufferedReader brSol = null;
        double[][] cov = new double[numAtoms][numAtoms];

        try {
            frSol = new FileReader("test_data/cov_1ggg_matlab.csv");
            brSol = new BufferedReader(frSol);


            int row = 0;
            String line;
            while ((line = brSol.readLine()) != null) {

                String[] dataArr = line.split(",");

                for (int i = 0; i < numAtoms; i++) {
                    cov[row][i] = Double.parseDouble(dataArr[i]);
                }
                row++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            if (brSol != null) {
                brSol.close();
            }
            if(frSol != null) {
                frSol.close();
            }
        }


        if(!compareMatricies(tst.getData(), cov))
            fail();






    }


}
