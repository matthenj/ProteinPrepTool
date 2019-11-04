package controller.pdbTopTrajFFVerification;

import controller.ProteinPrepToolException;

import java.util.ArrayList;
import java.util.LinkedList;

/*******************************************************************************
 *
 *	Filename   :	TestReport.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Reporting structure for ProteinVerification tests. For each test, a
 *	status and (should the status be fail), message is stored. class also
 *	acts as a gateway between the verification tests and error recoverer.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class TestReport {




    public enum TEST_STATUS {NOT_STARTED, PASSED, FAILED}


    private TEST_STATUS passed[];
    private String[] failMessages;
    private int numberOfTests;
    private boolean[] isFatal;
    private ProteinVerifier pv;


    public TestReport(int numberOfTests, ProteinVerifier pv) {

        this.pv = pv;
        this.numberOfTests = numberOfTests;
        passed = new TEST_STATUS[numberOfTests];
        failMessages = new String[numberOfTests];
        isFatal = new boolean[numberOfTests];

        for (int i = 0; i < numberOfTests; i++) {
            passed[i] = TEST_STATUS.NOT_STARTED;
            failMessages[i] = "";
        }
    }

    /********************************************
     *
     * Add a report to the test report.
     *
     * @param testNumber - test to which the results are being added
     * @param status - The status of the test - PASS/FAILURE
     * @param msg - Message relevant upon failure.
     * @throws ProteinPrepToolException - If test number is outside the initialised test count, or less than 0
     */
    public void addReport(int testNumber, TEST_STATUS status, String msg, boolean isFatal) throws ProteinPrepToolException {
        if (testNumber < 0 || testNumber >= numberOfTests) {
            throw new ProteinPrepToolException("Invalid test ID");
        }

        this.isFatal[testNumber] = isFatal;
        passed[testNumber] = status;
        failMessages[testNumber] = msg;
    }


    /*****************************************
     * Method determines if any of the tests within the report faile
     * @return True, if any test failed. False if all tests passed.
     */
    public boolean anyFailures() {
        for (TestReport.TEST_STATUS ts : passed) {
            if (ts == TEST_STATUS.FAILED)
                return true;
        }

        return false;
    }

    public String printFailMessages(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < passed.length; i++) {
            TEST_STATUS ts = passed[i];
            if (ts == TEST_STATUS.FAILED)
                sb.append(this.failMessages[i]).append(System.lineSeparator());
        }

        return sb.toString();
    }


    public boolean hasFatal() {

        for (int i = 0; i < numberOfTests; i++) {
            if(isFatal[i])
                return true;

        }
        return false;

    }

    public String printFatalMessages() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < isFatal.length; i++) {
            boolean b = isFatal[i];
            if (b) {
                sb.append(failMessages[i]).append(System.lineSeparator());
            }
        }

        return  sb.toString();

    }

    public LinkedList<RecoverableOnFail> getRecoverableErrors() {

        return this.pv.getRecoverableErrors();

    }

}
