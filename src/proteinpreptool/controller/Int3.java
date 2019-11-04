package controller;

/*******************************************************************************
 *
 *	Filename   :	int3.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class extends int2, adding a z component.
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/


public class Int3 extends Int2  {

    public int z;

    public Int3(){
        super();
        z = 0;
    }

    public Int3(int value){
        super(value);
        z = value;
    }

    public Int3(int x, int y, int z) {
        super(x,y);
        this.z = z;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Int3){
            Int3 other = (Int3) obj;
            return (x == other.x && y == other.y && z == other.z);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return x ^ y ^ z;
    }

    public Int2 xy(){
        return new Int2(x,y);
    }

    public Int2 xz(){
        return new Int2(x,z);
    }


    public Int2 yz(){
        return new Int2(y,z);
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(", ").append(z);
        return sb.toString();
    }


}
