package controller;

/*******************************************************************************
 *
 *	Filename   :	Double3.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class stores three doubles,(xyz), for use when they are related to one
 *	another: for example coordinates.
 *
 *	Version History: 11/07/19 (initial version)
 *
 *******************************************************************************/


public class Double3 {


    public double x;
    public double y;
    public double z;

    public Double3(){
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Double3(double value){
        x = y = z = value;
    }

    public Double3(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Double3(int x, int y, int z){
        this.x = (double)x;
        this.y = (double)y;
        this.z = (double)z;
    }

    public Double3(Double3 value){
        this.x = value.x;
        this.y = value.y;
        this.z = value.z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Double3 double3 = (Double3) o;
        return Double.compare(double3.x, x) == 0 &&
                Double.compare(double3.y, y) == 0 &&
                Double.compare(double3.z, z) == 0;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String toString(){
        return x + ", " + y + ", " + z;
    }
}
