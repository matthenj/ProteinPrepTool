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
 *	Class used to collect a list of tests that can be performed on the
 *	PDBFile, Topology and Trajectory  to test compatibility. Should be
 *	created using the ProteinVerifierFactory. Calling runtest will perform
 *	all of the tests within the verifier.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ConsoleController;
import controller.ProteinPrepToolException;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProteinVerifier {

    private ArrayList<VerificationTest> testsToPerform;

    protected ProteinVerifier(){
        testsToPerform = new ArrayList();
    }


    protected void addTest(VerificationTest pv){
        testsToPerform.add(pv);
    }

    /**
     * Runs each of the tests added to the verifier.
     *
     * @return TestReport - details of the success/failure of each of the tests performed
     *
     */
    public TestReport runTests() {

        TestReport trt = new TestReport(testsToPerform.size(), this);
        for (int i = 0; i < testsToPerform.size(); i++) {
            VerificationTest vt = testsToPerform.get(i);
            boolean passed = vt.performTest();

            try {
                if (passed) {
                    trt.addReport(i, TestReport.TEST_STATUS.PASSED, "", false);
                } else {
                    trt.addReport(i, TestReport.TEST_STATUS.FAILED, vt.getFailMessage(), vt.isFatal());
                }
            }catch(ProteinPrepToolException pex){
                ConsoleController.log.addMessage(pex.getFriendlyMessage());
            }
        }

        return trt;
    }

    /**
     *
     * @return list of all tests added.
     */
    public String toString(){

        StringBuilder sb = new StringBuilder();

        for (VerificationTest vt : this.testsToPerform){
            sb.append(vt.toString()).append(System.lineSeparator());
        }

        return sb.toString();
    }


    public LinkedList<RecoverableOnFail> getRecoverableErrors() {

        LinkedList<RecoverableOnFail> rof = new LinkedList<>();
        for (VerificationTest vt : this.testsToPerform) {
            if(vt instanceof RecoverableOnFail){
                rof.add((RecoverableOnFail)vt);
            }
        }

        return rof;
    }
}