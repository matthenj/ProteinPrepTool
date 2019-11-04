package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AtomPair.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Wrapper class for int3. Used to generate pairs when reading in non
 *	-GROMACS topologies.
 *
 *	Version History: 16/07/19 (initial version)
 *
 *******************************************************************************/


import controller.Int2;
import controller.Int3;

//class used for pair identification
public class AtomPair extends Int3 {


    protected AtomPair() {
        super();
    }

    protected AtomPair(int value) {
        super(value);
    }

    AtomPair(int x, int y, int z) {
        super(x, y, z);
    }

    AtomPair(Int3 xyz) {
        super(xyz.x, xyz.y, xyz.z);
    }

    AtomPair(Int2 xy, int z) {
        super(xy.x, xy.y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AtomPair) {
            AtomPair other = (AtomPair) obj;
            return (x == other.x && y == other.y) || (x == other.y && y == other.x);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return x ^ y;
    }
}

//residue infomration stored separately in amber format.


