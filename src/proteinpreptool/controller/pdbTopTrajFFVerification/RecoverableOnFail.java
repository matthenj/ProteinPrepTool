package controller.pdbTopTrajFFVerification;

/*******************************************************************************
 *
 *	Filename   :	RecoverableOnFail.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Interface used to allow Verification failures to be recoverable. If a
 *	verification failure is recoverable, implement this interface. Then,
 *	during execution, the "ErrorRecoverer" will call "recover."
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public interface RecoverableOnFail {

    boolean recover();
    boolean isRecovered();

}
