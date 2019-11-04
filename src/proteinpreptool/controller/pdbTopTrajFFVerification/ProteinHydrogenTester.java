package controller.pdbTopTrajFFVerification;

import controller.PDBFile;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	ProteinHydrogenTester.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class looks to determine whether the loaded molecular  structure
 *	contains hydrogens. If it does not, the long  range interaction forces
 *	will not be true to life.  However, execution can continue.  If over 5 %
 *	of the atoms are hydrogens, the  biomolecule is assumed to contain
 *	hydrogen atoms.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/




public class ProteinHydrogenTester extends VerificationTest {

    private final String failMessage = "Does the biomolecule contain all necessary hydrogens? For best results with" +
            " haptimol, file should contain hydrogens.  ";


    public ProteinHydrogenTester(PDBFile pdb, TopologyFile tp, Trajectory traj) {
        super(pdb, tp, traj);
    }


    @Override
    public boolean performTest() {
        if(pdbFile.getNumberOfHydrogenAtoms() > pdbFile.getNumberOfAtoms() * 0.05){
            return true;
        } else {
            return false;
        }

    }

    @Override
    public String getFailMessage() {
        return failMessage;
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
