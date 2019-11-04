package controller;

/*******************************************************************************
 *
 *	Filename   :	ProteinPrepToolException.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Exception class for use with ProteinPrepTool.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class ProteinPrepToolException extends Exception {


    private String friendlyMessage;
    private boolean isFatal;

    public ProteinPrepToolException(String message){
       this(message, "");
    }

    public ProteinPrepToolException(String message, String friendlyMessage, boolean isFatal ){
        super(message);
        this.friendlyMessage = friendlyMessage;
        this.isFatal = isFatal;
        if(ConsoleController.verbose){
            ConsoleController.log.addMessage(friendlyMessage);
        }
    }

    public ProteinPrepToolException(String message, String friendlyMessage){
       this(message, friendlyMessage, false);
    }

    public String getFriendlyMessage() {
        return friendlyMessage;
    }

    public boolean isFatal() {
        return isFatal;
    }
}
