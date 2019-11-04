package controller.pdbTopTrajFFVerification;

import controller.PDBFile;
import controller.ProteinPrepToolException;
import controller.TopFile.AtomTopology;
import controller.TopFile.TopologyFile;
import controller.Trajectory.Trajectory;
import controller.forcefields.Forcefield;
import controller.pdbTopTrajFFVerification.ErrorRecoverer;
import controller.pdbTopTrajFFVerification.RecoverableOnFail;
import controller.pdbTopTrajFFVerification.VerificationTest;
import gui.ForcefieldMissMatchWindow;

import java.util.HashSet;


/*******************************************************************************
 *
 *	Filename   :	TopologyForcefieldCompatibilityTest.groovy
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Test code for the topology/forcefiedl compatibility test. Few basic
 *	tests to ascertain whether the test works or not.,
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


//class verifies whether the requested atom types, found in the topology, match the selected forcefield. If there are
//missing values, the user will be prompted to either provide alternate values, or ignore this disparity.
public class TopologyForcefieldCompatibilityTest extends VerificationTest implements RecoverableOnFail {

    boolean isRecovered;
    HashSet<String> absentValues;
    Forcefield ff;


    public TopologyForcefieldCompatibilityTest(PDBFile pdb, TopologyFile tp, Trajectory traj, Forcefield ff) {
        super(pdb, tp, traj);
        this.ff = ff;
    }

    @Override
    public boolean performTest() {


        absentValues = new HashSet();

        try {

            for (int i = 0; i < topFile.getNumberOfAtomsInTopology(); i++) {
                AtomTopology atm = topFile.getAtom(i);

                String type = atm.getType().getType();
                if(!ff.contains(type)){
                    absentValues.add(type);
                }
            }

        } catch(ProteinPrepToolException pex){

        }

        if(absentValues.size() > 0){

            System.out.println("Missing values: " + System.lineSeparator() + absentValues.toString());
            isRecovered = false;
            return false;
        } else {
            isRecovered = true;
            return true;
        }
    }

    @Override
    public String getFailMessage() {
        return null;
    }

    @Override
    public boolean isFatal() {
        return false;
    }

    @Override
    public boolean recover() {

        ForcefieldMissMatchWindow.createAndShowGUI(this);

        return false;
    }

    public void recoveryComplete(){
        isRecovered = true;
        ErrorRecoverer.postHandleComplete();
    }

    public void skip(){
        isRecovered = true;
        ErrorRecoverer.postHandleComplete();
    }



    @Override
    public boolean isRecovered() {
        return isRecovered;
    }

    public String[] getUnknownAtoms() {
        String[] unknownAtoms = new String[this.absentValues.size()];
        int i = 0;
        for(String s : absentValues){
            unknownAtoms[i++] = s;
        }

        return  unknownAtoms;

    }

    public  String[] getAvailableAtomTypes(){

        return ff.getAvailableAtomTypes();

    }

    public TopologyFile getTopologyFile() {
        return this.topFile;
    }
}
