package controller.pdbTopTrajFFVerification.testing

/*******************************************************************************
 *
 *	Filename   :	PdbTopResNameComparatorTest.groovy
 *
 *	Author     :	njm
 *
 *
 *	Description:
 *
 *	Test code for the PDB - Top residue name comparator class. A few basic
 *	tests to ascertain whether the functionality works as designed.
 *
 *	Version History: 12/07/19 (initial version)
 *
 *******************************************************************************/


import controller.Atom
import controller.TopFile.AtomTopology
import controller.pdbTopTrajFFVerification.PdbTopResNameComparator
import junit.framework.TestCase

class PdbTopResNameComparatorTest extends GroovyTestCase {

    void testPerformTest() {

    }

    void testCompareResidue() {

        //create some residues
        ArrayList<Atom> testResiduePDB = new ArrayList();
        ArrayList<AtomTopology> testResidueTop = new ArrayList();

        TestCase.assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), -2);

        testResiduePDB.add(new Atom("ATOM      1  N   LEU     1      43.796  14.179  37.864  1.00  0.00           N  "));
        testResidueTop.add(new AtomTopology(1, "N3", 0, "LEU", "N", 1, 0.101, 14.01));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), 0);

        testResidueTop.add(new AtomTopology(2, "H", 0, "LEU", "H1", 2, 0.2148, 1.008));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), -1);
        testResiduePDB.add(new Atom("ATOM      2  H1  LEU     1      43.308  13.525  37.269  1.00  0.00           H  "));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), 0);

        testResiduePDB.add(new Atom("ATOM      2 1HB3 LEU     1      43.308  13.525  37.269  1.00  0.00           H  "));
        testResidueTop.add(new AtomTopology(2, "H", 0, "LEU", "HB31", 2, 0.2148, 1.008));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), 0);

        testResiduePDB.add(new Atom("ATOM      2 HB32 LEU     1      43.308  13.525  37.269  1.00  0.00           H  "));
        testResidueTop.add(new AtomTopology(2, "H", 0, "LEU", "2HB3", 2, 0.2148, 1.008));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), 0);

        testResiduePDB.add(new Atom("ATOM      2 HB23 LEU     1      43.308  13.525  37.269  1.00  0.00           H  "));
        testResidueTop.add(new AtomTopology(2, "H", 0, "LEU", "2HB3", 2, 0.2148, 1.008));
        assertEquals(PdbTopResNameComparator.compareResidue(testResiduePDB, testResidueTop), 1);








    }
}
