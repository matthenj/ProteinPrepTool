package controller.TopFile;

/*******************************************************************************
 *
 *	Filename   :	AtomType.java
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Class wraps the String class. Should be used to store the "AtomType"
 *	field in the AtomTopology class. Should only be created using static
 *	methods inside of the AtomTopology class.
 *
 *  TODO: enforce creation only by AtomTopology class.
 *
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


public class AtomType {

    private String type;


    public AtomType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomType atomType = (AtomType) o;
        return type.equals(atomType.type);
    }

    @Override
    public int hashCode() {
           return type.hashCode();
    }

    @Override
    public String toString() {
        return type;
    }
}
