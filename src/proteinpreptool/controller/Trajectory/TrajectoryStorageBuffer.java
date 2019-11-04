package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	TrajectoryStorageBuffer.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	The trajectory storage buffer should be used to store trajectory frames.
 *	The TSB can be used to store blocks of the trajectory to harddisk,
 *	rather than keeping them in memory.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ProteinPrepToolException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TrajectoryStorageBuffer {


    private ArrayList<String> TrajectoryInScratchPaths;
    private int groupSize;
    private int groupsCreated;
    private TrajectoryGroup activeWrite;
    private TrajectoryGroup activeRead;

    private ArrayList<TrajectoryGroup> trajParts;

    private int totalFramesAdded;
    private int uniqueFileId;


    public TrajectoryStorageBuffer(int groupSize) {
        totalFramesAdded = 0;
        groupsCreated = 0;
        this.groupSize = groupSize;
        uniqueFileId = (int) (Math.random() * 1000.0);
        activeWrite = new TrajectoryGroup(totalFramesAdded, uniqueFileId, groupsCreated);

        trajParts = new ArrayList<>();
        this.trajParts.add(activeWrite);
        groupsCreated++;
        activeRead = activeWrite;
    }


    public TrajectoryStorageBuffer() {
        this(1000);
    }


    public void addFrame(double[] frame) throws ProteinPrepToolException {
        activeWrite.addFrame(frame);
        totalFramesAdded++;

        if (activeWrite.getLength() == groupSize) {

            //check to see if uniqueFileId is unique.
            activeWrite.pushOutOfMemory();
            activeWrite = new TrajectoryGroup(totalFramesAdded, uniqueFileId, groupsCreated);
            this.trajParts.add(activeWrite);
            groupsCreated++;

        }

    }


    public double[] getFrame(int i) throws ProteinPrepToolException {
        //work out which group.
        int group = i / groupSize;

        if (activeRead.getId() != group) {

            for (TrajectoryGroup tg : this.trajParts) {
                if (tg.getId() == group) {
                    if (activeRead.isInMemory()) {
                        activeRead.pushOutOfMemory();
                    }
                    activeRead = tg;

                }
            }

        }

        if (!activeRead.isInMemory()) {
            activeRead.restoreToMemory();
        }


        int realIndex = i - (group * groupSize);
        try {
            return activeRead.getTrajectoryFrame(realIndex);
        } catch (ProteinPrepToolException pex) {

            throw pex;
        }

    }

    public void subtractFromTrajectory(double[] toSubtract) throws ProteinPrepToolException {
        if (this.totalFramesAdded < 1) {
            throw new ProteinPrepToolException("No frames to subtract from!");
        }

        {
            TrajectoryGroup tg = this.trajParts.get(0);
            if (tg.getLength() == 0) {
                throw new ProteinPrepToolException("no data :( ");
            }

            if (tg.getFrameSize() != toSubtract.length) {
                throw new ProteinPrepToolException("Invalid frame size");
            }
        }

        for (TrajectoryGroup tg : this.trajParts) {
            tg.subtractFromAll(toSubtract);
        }

    }


    public void cleanup() {
        for (TrajectoryGroup tg : trajParts) {
            tg.clearWithoutSaving();
            tg.flushStorageFile();
        }


    }


    public int getNumberOfFrames() {
        return this.totalFramesAdded;
    }

    public int getNumberOfElementsPerFrame() throws ProteinPrepToolException {

        if (this.totalFramesAdded < 1)
            throw new ProteinPrepToolException("No trajectory frames loaded");

        return this.trajParts.get(0).getFrameSize();


    }

    public int getNumberOfGroups() {
        return this.groupsCreated;
    }

    public int getGroupSize() {
        return this.groupSize;
    }


    public int getNumberOfFramesInGroup(int i) {
        return this.trajParts.get(i).getLength();
    }

    //dangerous.
    public TrajectoryGroup getTrajGroup(int index) {
        return this.trajParts.get(index);
    }


    public void saveToCSV(String path) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path));

            if (activeRead.inMemory && activeRead.getId() != 0)
                activeRead.pushOutOfMemory();


            for (int i = 0; i < this.trajParts.size(); i++) {

                activeRead = this.trajParts.get(i);
                boolean inMem = activeRead.inMemory;
                if (!inMem)
                    activeRead.restoreToMemory();

                for (int frameI = 0; frameI < activeRead.getLength(); frameI++) {
                    double[] activeFrame = activeRead.getTrajectoryFrame(frameI);
                    for (int j = 0; j < activeFrame.length; j++) {
                        String write = (j+1 < activeFrame.length) ? activeFrame[j] + "," : activeFrame[j] +"";
                        writer.write(write);
                    }
                    writer.write(System.lineSeparator());
                }

                if(!inMem){
                    activeRead.pushOutOfMemory();
                }


            }


            writer.write(System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProteinPrepToolException e) {
            e.printStackTrace();
        }


    }


}
