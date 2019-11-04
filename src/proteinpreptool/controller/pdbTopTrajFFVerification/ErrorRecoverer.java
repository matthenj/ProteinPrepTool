package controller.pdbTopTrajFFVerification;

/*******************************************************************************
 *
 *	Filename   :	ErrorRecoverer.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Error recover acts as a worker class to handle the recovery of
 *	verification failures, where recovery is built into ProteinPrepTool.
 *	In order to be recoverable, the verification test must implement the
 *	interface "RecoverableOnFail"
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import static java.lang.Thread.sleep;

public class ErrorRecoverer implements Runnable{



    enum errorHandlerMessage{PROCEED};

    private static ConcurrentLinkedQueue<errorHandlerMessage> postBox = new ConcurrentLinkedQueue<>();
    private static Thread exeThread;

    private LinkedList<RecoverableOnFail> erc;
    private TestReport tr;



    public ErrorRecoverer(TestReport tr){
        this.tr = tr;
        erc = tr.getRecoverableErrors();
    }

    public void handle(){
        exeThread = new Thread(this);
        //option to allow computation to continue in the background.
        exeThread.run();
    }

    public boolean errorHandlingComplete() {
        return erc.size() == 0;
    }

    @Override
    public void run() {

        while(erc.size() > 0){
            RecoverableOnFail rof = erc.remove(0);
            rof.recover();
            while(!rof.isRecovered()) {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
        }
    }

    public static void postHandleComplete(){

        postBox.offer(errorHandlerMessage.PROCEED);
        exeThread.interrupt();

    }
}




