package controller.pdbTopTrajFFVerification;

/*******************************************************************************
 *
 *	Filename   :	VerificationTest.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Abstract class, which all protein verification tests should implement.
 *
 *	performTest() - execute test code, return false on failure otherwise
 *	true.
 *
 *  getFailMessage() - a friendly message relating to the failure.
 *
 *	isFatal() - if true, execution cannot continue past this point.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/

import controller.PDBFile;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;

public abstract class VerificationTest {

    protected PDBFile pdbFile;
    protected TopologyFile topFile;
    protected Trajectory trajectory;

    //private to prevent default constructor being called.
    private VerificationTest(){

    }

    /*
     *  Only constructor.
     *
     * @param pdb - PDB file from test structure
     * @param tp - Topology File from test structure
     * @param traj - Trajectory used
     */
    public VerificationTest(PDBFile pdb, TopologyFile tp, Trajectory traj){
        this.pdbFile = pdb;
        this.topFile = tp;
        this.trajectory = traj;
    }

    /**
     * Method should run any desired test code, comparing compatibility between the top, pdb and traj
     * (Or any combination of the above).
     *
     * @return return false if the test criteria is not met, otherwise true.
     */
    public abstract boolean performTest();

    /**
     * Gets an English description of the failure from the test.
     *
     * @return String - description of the failure.
     */
    public abstract String getFailMessage();


    /**
     * Returns whether or not test failure is fatal to the continuation of the algorithm.
     *
     * @return boolean: true if fatal.
     */
    public abstract boolean isFatal();





}
