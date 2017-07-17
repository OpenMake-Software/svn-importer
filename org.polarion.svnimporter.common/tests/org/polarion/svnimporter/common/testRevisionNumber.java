package org.polarion.svnimporter.common;

public class testRevisionNumber extends BaseTestCase {

    public testRevisionNumber(String name) {
        super(name);
    }

    public void testParse() {
       assertObjectsEqual(new int[]{25,4,157,1,6},
                          RevisionNumber.parse("25.4.157.1.6"));
    }

    public void testCompare() {
        assertTrue(RevisionNumber.compare("101.2", "99.3") > 0);
        assertTrue(RevisionNumber.compare("23.4.2.5", "23.4.2.5") == 0);
        assertTrue(RevisionNumber.compare("23.4.2.5.5.6", "23.4.2.5") > 0);
        assertTrue(RevisionNumber.compare("23.4.2.5.5", "23.4.2.5.20") < 0);
        assertTrue(RevisionNumber.compare("23.4.5", "23.5") < 0);
    }

    public void testJoin() {
        assertEquals("25.4.157.1.6", RevisionNumber.join(new int[]{25,4,157,1,6}));
    }

    public void testGetSubNumber() {
        int[]nary = new int[]{25,4,157,1,6};
        assertEquals("25.4", RevisionNumber.getSubNumber(nary, 0, 2));
        assertEquals("157.1.6", RevisionNumber.getSubNumber(nary, 2, 5));
    }

}
