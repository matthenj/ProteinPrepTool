package controller.Trajectory;

/*******************************************************************************
 *
 *	Filename   :	MassWeightedSuperImposition.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class can be used to perform mass weighted superposition - the
 *	superposition of a trajectory frame onto a reference frame.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.*;
import controller.TopFile.TopologyFile;
import org.apache.commons.math3.linear.*;

public class MassWeightedSuperImposition {



    private TopologyFile referenceTopology;



    private double[] masses;
    private double totalMass;
    private VectorTools vm;

    private double[] referenceCoM;
    private double[] referenceCoords;
    private double[] referenceCoordsDecentered;
    private double[] referenceCoordsDecenteredSquared;

    private RealMatrix massElMultRefCoords;


    private double[] lastDecentred;
    private double   lastRMSD;


    private boolean memoryCleared;


    private MassWeightedSuperImposition(){


    }


    public MassWeightedSuperImposition(PDBFile reference, TopologyFile topology) throws ProteinPrepToolException {
        initialise(reference.getCoordsAsVector(), topology);
    }

    public MassWeightedSuperImposition(double[] reference, TopologyFile topology) throws ProteinPrepToolException {
        initialise(reference, topology);
    }

    private void initialise(double[] reference, TopologyFile topology) throws ProteinPrepToolException {
        this.vm = new VectorTools();
        this.referenceTopology = topology;
        this.totalMass = 0.0;
        int numAtoms = topology.getNumberOfAtomsInTopology();
        masses = new double[reference.length/3];

        if(this.referenceTopology.getNumberOfAtomsInTopology() != reference.length/3){
            throw new ProteinPrepToolException((reference.length/3) +
                    " atoms in reference, " + topology.getNumberOfAtomsInTopology() + " atoms in topology file.");
        }

        for (int i = 0; i < topology.getNumberOfAtomsInTopology(); i++){
            masses[i] = topology.getAtom(i).getMass();
            totalMass+=masses[i];
        }

        referenceCoords = reference;
        referenceCoM = new double[3];
        referenceCoordsDecentered = this.removeCentreOfMass(referenceCoords, referenceCoM);
        referenceCoordsDecenteredSquared = vm.elementWiseVectorScalerPower(referenceCoordsDecentered, 2);

        massElMultRefCoords = new Array2DRowRealMatrix(vm.convertTo3dAndElementWiseMult(masses, referenceCoordsDecentered));

        lastDecentred = null;
        lastRMSD = -1.0;
        memoryCleared = false;
    }


    /**
     * Removes the centre of mass from the target array. Array should be laid out (x1y1z1x2y2z2.....xn-1yn-1zn-1)
     * @param xyzArray - array of coordinates.
     * @return Array of values with centre of mass removed
     */

    private double[] removeCentreOfMass(double[] xyzArray) throws ProteinPrepToolException {
        return removeCentreOfMass(xyzArray, new double[3] );
    }

    /**
     * Removes the centre of mass from the target array. Array should be laid out (x1y1z1x2y2z2.....xn-1yn-1zn-1)
     * @param xyzArray
     * @param  com - centre of mass array. should be initialised to length 3.
     * @return Array of values with centre of mass removed
     */

    private double[] removeCentreOfMass(double[] xyzArray, double[] com) throws ProteinPrepToolException {

        if(com == null || com.length != 3){
            throw new ProteinPrepToolException("Centre of mass array must be of length 3.");
        }

        if(this.memoryCleared)
            throw new ProteinPrepToolException("Memory cleared. recreate object");

        double comX, comY, comZ;
        double[] xCord , yCord, zCord;
        double[] decentred = new double[xyzArray.length];

        int atoms = xyzArray.length / 3;
        xCord = new double[atoms];
        yCord = new double[atoms];
        zCord = new double[atoms];

        //remove center of mass from trajectory frame..


        for (int i = 0; i < atoms; i++){
            xCord[i] = xyzArray[i*3 + 0];
            yCord[i] = xyzArray[i*3 + 1];
            zCord[i] = xyzArray[i*3 + 2];
        }

        com[0] = vm.sum(this.vm.elementWiseVectorVectorMultiplier(this.masses, xCord))/totalMass;
        com[1]= vm.sum(this.vm.elementWiseVectorVectorMultiplier(this.masses, yCord))/totalMass;
        com[2] = vm.sum(this.vm.elementWiseVectorVectorMultiplier(this.masses, zCord))/totalMass;

        for (int i = 0; i < decentred.length; i+=3){
            decentred[i] = xyzArray[i] - com[0];
            decentred[i+1] = xyzArray[i+1] - com[1];
            decentred[i+2] = xyzArray[i+2] - com[2];
        }

        return decentred;
    }

    private double getRmsd(double[] frame) throws ProteinPrepToolException {


        if(this.memoryCleared)
            throw new ProteinPrepToolException("Memory cleared. recreate object");

        double[] intermediate = (vm.elementWiseVectorVectorAddition(vm.elementWiseVectorScalerPower(frame,2), this.referenceCoordsDecenteredSquared));
        double[] compress = new double[intermediate.length/3];

        for (int i = 0; i < compress.length; i++){
            compress[i] = intermediate[i*3];
            compress[i] += intermediate[i*3+1];
            compress[i] += intermediate[i*3+2];
        }

        double rmsd = 0.5 * vm.sum(vm.elementWiseVectorVectorMultiplier(masses,compress));


        return rmsd;
    }

    public double[] superposition(double[] frame) throws ProteinPrepToolException {

        if(this.memoryCleared)
            throw new ProteinPrepToolException("Memory cleared. recreate object");

        if(frame.length/3 != this.masses.length)
            throw new ProteinPrepToolException("Frame size differs from masses");


        //remove centre of mass
        double[] decentred = removeCentreOfMass(frame);
        double rmsd = getRmsd(decentred);

        double[][] frameMatrix = new double[decentred.length/3][3];

        for (int i = 0; i < frameMatrix.length; i++){
            frameMatrix[i][0] = decentred[i * 3 + 0];
            frameMatrix[i][1] = decentred[i * 3 + 1];
            frameMatrix[i][2] = decentred[i * 3 + 2];
        }

        RealMatrix realFrameMatrix = new Array2DRowRealMatrix(frameMatrix);

        //Using Apache Commons for SVD.
        RealMatrix rm = this.massElMultRefCoords.multiply(realFrameMatrix);
        SingularValueDecomposition svd = new SingularValueDecomposition(rm);

        double[] D = svd.getSingularValues();

        RealMatrix U = svd.getU();
        RealMatrix V = svd.getV();


        //check reflection
        LUDecomposition luU = new LUDecomposition(U);
        LUDecomposition luV = new LUDecomposition(V.transpose());
        double reflection = luU.getDeterminant() * luV.getDeterminant();

        //this code is untested...
        if(reflection < 0){
            D[2] = -D[2];
            double[] column3 = V.getColumn(2);
            for(int i = 0; i < column3.length;i++) {
                column3[i] = -column3[i];
            }

            V.setColumn(2, column3);
        }

        rmsd = rmsd - vm.sum(D);


        //calculate rotation matrix
        RealMatrix rotationMatrix = U.multiply(V.transpose());
        double[][] rotatedMolecule = rotationMatrix.multiply(realFrameMatrix.transpose()).getData();

        //restore centre of mass

        for (int i = 0; i < rotatedMolecule[0].length; i++){
            rotatedMolecule[0][i] += this.referenceCoM[0];
            rotatedMolecule[1][i] += this.referenceCoM[1];
            rotatedMolecule[2][i] += this.referenceCoM[2];
        }

        //copy back into 1D array, and return;
        for (int i = 0; i < rotatedMolecule[0].length; i++){
            decentred[3 * i + 0] = rotatedMolecule[0][i];
            decentred[3 * i + 1] = rotatedMolecule[1][i];
            decentred[3 * i + 2] = rotatedMolecule[2][i];
        }


        rmsd = Math.sqrt(2.0*Math.abs(rmsd)/totalMass);
        lastRMSD = rmsd;
        lastDecentred = decentred;


//
//        Double[] rtn = new Double[decentred.length];
//        for(int i = 0; i < rtn.length; i++){
//            rtn[i] = decentred[i];
//        }

        return decentred;

    }

    public void freeMem(){
        this.lastDecentred = new double[0];
        this.massElMultRefCoords = MatrixUtils.createRealMatrix(1,1);
        this.masses = new double[0];
        this.referenceCoords = new double[0];
        this.referenceCoM = new double[0];
        this.referenceCoordsDecentered = new double[0];
        this.referenceCoordsDecenteredSquared = new double[0];

        memoryCleared = true;

    }


    public double[] getLastDecentred() throws ProteinPrepToolException {

        if(this.memoryCleared) {
            throw new ProteinPrepToolException("Memory cleared. recreate object");
        }

        return lastDecentred;
    }



    public double getLastRMSD() {
        return lastRMSD;
    }


}
