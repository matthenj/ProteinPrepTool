package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	TrajectoryGroup.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	A TrajectoryGroup groups a collection of trajectory frames into a block,
 *	which can then be moved in and out of memory as required.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.ProteinPrepToolException;

import java.io.*;
import java.util.ArrayList;

public class TrajectoryGroup implements Serializable {
    private static final long serialVersionUID = 0x00001;

    private Integer firstFrame;
    private ArrayList<double[]> trajectory;
    private Integer frameLength;
    private int numberOfFrames;
    private int groupId;

    boolean isTranspose;
    boolean inMemory;
    String writePath;

    public TrajectoryGroup(int firstFrame, int writeFileId, int groupId){
        trajectory = new ArrayList<>();
        frameLength = -1;
        numberOfFrames = 0;
        this.firstFrame = firstFrame;
        inMemory = true;
        this.groupId = groupId;
        isTranspose= false;
        writePath = "processedTraj_" + writeFileId + "_" + firstFrame + ".ser";

    }

    public int getLength(){
        return numberOfFrames;
    }

    public int getFrameSize(){
        return frameLength;
    }

    public double[] getTrajectoryFrame(int i) throws ProteinPrepToolException {

        if(i >= trajectory.size() || i < 0){
            throw new ProteinPrepToolException("Invalid frame requested!:"  + i);
        }

        return trajectory.get(i);

    }

    public void addFrame(double[] frame) throws ProteinPrepToolException {

        if(isTranspose)
            throw new ProteinPrepToolException("Matrix is READ ONLY in transpose mode.");


        if(frameLength < 0){
            frameLength = frame.length;
        } else {
            if(frame.length != frameLength){
                throw new ProteinPrepToolException("Invalid frame added to trajectory");
            }
        }

        this.trajectory.add(frame);
        numberOfFrames++;

    }

    public void pushOutOfMemory(){
        System.out.println("Writing to FILE");
        ObjectOutputStream out = null;
        long size = this.trajectory.size() * this.frameLength;
        try {
           // out = new FileOutputStream(writePath);
            //FileChannel file = out.getChannel();
            out = new ObjectOutputStream(new FileOutputStream(writePath));
            for (double[] darr : this.trajectory) {
                out.writeObject(darr);
            }
            this.inMemory = false;
            this.trajectory.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public boolean isInMemory(){
        return inMemory;
    }

    public void restoreToMemory(){

        ObjectInputStream ois;
        System.out.println("Reading from FILE");
        try (FileInputStream fis = new FileInputStream(this.writePath)) {
            ois = new ObjectInputStream(fis);
            for (int i = 0; i < this.numberOfFrames; i++){
                double[] d = new double[this.frameLength];
                d = (double[])ois.readObject();
                this.trajectory.add(d);
            }

            ois.close();

            this.inMemory = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void restoreToMemoryTranspose(){

        ObjectInputStream ois;
        System.out.println("Reading from File (TRANSPOSE)");




        for (int i= 0; i < this.frameLength; i++){
            this.trajectory.add(new double[this.numberOfFrames]);
        }

        try (FileInputStream fis = new FileInputStream(this.writePath)) {
            ois = new ObjectInputStream(fis);
            for (int i = 0; i < this.numberOfFrames; i++){
                double[] d = (double[])ois.readObject();
                for (int atom = 0; atom < this.frameLength; atom++){
                    this.trajectory.get(atom)[i] = d[atom];
                }

            }

            ois.close();

            this.inMemory = true;
            this.isTranspose = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void transpose(){



        if(isInMemory()){
            ArrayList <double[]> tmp = new ArrayList<>();

            for (int i = 0; i < this.frameLength; i++){
                tmp.add(new double[this.numberOfFrames]);
            }

            ArrayList<double[]> doubles = this.trajectory;
            for (int i1 = 0, doublesSize = doubles.size(); i1 < doublesSize; i1++) {
                double[] d = doubles.get(i1);
                for (int i = 0; i < d.length; i++) {
                    tmp.get(i)[i1] = d[i];
                }
            }


            this.trajectory.clear();
            this.trajectory = tmp;
            isTranspose = true;

        } else {
            this.restoreToMemoryTranspose();

        }



    }


    public void flushStorageFile(){
        File f = new File(writePath);
        f.delete();
    }

    public int getId() {
        return groupId;
    }
    
    
    public void subtractFromAll(double[] toSubtract){

        boolean needToLoad = !this.isInMemory();

        if(needToLoad){
            this.restoreToMemory();
        }

        for (double[] tarr : this.trajectory){
            for(int i = 0; i < toSubtract.length; i++ ){
                tarr[i] -= toSubtract[i];
            }
        }


        if(needToLoad){
            this.flushStorageFile();
            this.pushOutOfMemory();
        }


    }

    public void clearWithoutSaving(){
        this.trajectory = new ArrayList<>();
    }
    
    
}