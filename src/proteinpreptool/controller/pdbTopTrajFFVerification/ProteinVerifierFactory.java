package controller.pdbTopTrajFFVerification;

/*******************************************************************************
 *
 *	Filename   :	ProteinVerifier.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class can be used to create a protein verifier object, with appropriate
 *	tests. Any new tests should be added here.

 Current implementation
 *	returns an object with all the tests available within PPT.
 *	Customisability will come with later applications.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.PDBFile;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;
import controller.forcefields.Forcefield;

public class ProteinVerifierFactory {

    public ProteinVerifierFactory() {

    }


    /***
     * Adds all available tests to a proteinverifier, and returns it
     * @param pdb - pdb file to test
     * @param top - topology file to test
     * @param traj - trajectory file to tests
     * @return ProteinVerifier Object with all implemented tests added.
     */
    public ProteinVerifier ProteinVerifierFactory(PDBFile pdb, TopologyFile top, Trajectory traj) {


        ProteinVerifier pv = new ProteinVerifier();

        //add tests here.
        pv.addTest(new ProteinSizeComparator(pdb, top, traj));
        pv.addTest(new ProteinHydrogenTester(pdb, top, traj));
        pv.addTest(new PdbTopResNameComparator(pdb, top, traj));


        return pv;

    }


    public ProteinVerifier ProteinVerifierFactory(PDBFile pdb, TopologyFile top, Trajectory traj, Forcefield ff) {

        ProteinVerifier pv = ProteinVerifierFactory(pdb, top, traj);
        pv.addTest(new TopologyForcefieldCompatibilityTest(pdb, top, traj, ff));


        return pv;
    }
}
