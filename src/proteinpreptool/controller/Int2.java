package controller;


/*******************************************************************************
 *
 *	Filename   :	int2.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class stores two ints,(xy), for use when they are related to one
 *	another
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/




public class Int2 implements Comparable<Int2>{

    public int x;
    public int y;

    public Int2(){
        x = y = 0;
    }

    public Int2(int value){
        x = y = value;
    }

    public Int2(int x, int y){
        this.x = x;
        this.y = y;
    }


    @Override
    public int compareTo(Int2 o) {

        if(x == o.x){
            return y - o.y;
        } else {
            return x - o.x;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Int2){
            Int2 other = (Int2) obj;
            return (x == other.x && y == other.y);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return x ^ y;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(x).append(", ").append(y);
        return sb.toString();
    }
}
