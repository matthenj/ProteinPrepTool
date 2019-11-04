package controller;

/*******************************************************************************
 *
 *	Filename   :	int4.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class extends int3, adding a w component.
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/


public class Int4 extends Int3 {

    public int w;

    public Int4(){
        super();
        w = 0;
    }


    public Int4(int value){
        super(value);
        w = value;
    }

    public Int4(int x, int y, int z, int w) {
        super(x, y, z);
        this.w = w;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(", ").append(w);
        return sb.toString();
    }


}
