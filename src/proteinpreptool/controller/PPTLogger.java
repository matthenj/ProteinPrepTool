package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*******************************************************************************
 *
 *	Filename   :	PPTLogger.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class acts as a basic log system. If verbose is enabled, log is printed
 *	to the console in execution.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class PPTLogger {

    private StringBuilder log;

    public PPTLogger(){
        log = new StringBuilder();
    }

    public void addMessage(String logMsg){
        addMessage(logMsg, true);
    }


    public void addMessage(String logMsg, boolean addLine) {
        if (ConsoleController.verbose)
            System.out.println(logMsg);

        log.append(logMsg);
        if (addLine)
            log.append(System.lineSeparator());
    }

    public String toString(){
        return log.toString();
    }

    public void saveLogToFile(String path){

        // If the file doesn't exists, create and write to it
        // If the file exists, truncate (remove all content) and write to it
        try (FileWriter writer = new FileWriter(path);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write(this.toString());

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

    }


}
