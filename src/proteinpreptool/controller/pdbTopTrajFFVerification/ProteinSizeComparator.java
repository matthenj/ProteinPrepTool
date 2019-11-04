package controller.pdbTopTrajFFVerification;

/*******************************************************************************
 *
 *	Filename   :	ProteinSizeComparator.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Simple verification test - checks all three structures, pdb, top and ff,
 *	contain the same number of atoms.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.PDBFile;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;

public class ProteinSizeComparator extends VerificationTest{

    private final String failMessage = "Number of atoms contained within files is different.";


    public ProteinSizeComparator(PDBFile pdb, TopologyFile tp, Trajectory traj) {
        super(pdb, tp, traj);
    }

    @Override
    public boolean performTest() {
        if(pdbFile.getNumberOfAtoms() == topFile.getNumberOfAtomsInTopology() && pdbFile.getNumberOfAtoms() == trajectory.getNumberOfAtoms())
            return true;
        else
            return false;
    }

    @Override
    public String getFailMessage() {
        return failMessage;
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}
