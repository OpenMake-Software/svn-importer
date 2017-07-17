package org.polarion.svnimporter.common;

import java.lang.reflect.*;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

    public BaseTestCase(String name) {
        super(name);
    }

    /**
     * Assert that two objects are equal.  This can be used to compare very
     * complicated objects that the standard assertEquals method using the 
     * object.equals() method cannot compare correctly.  In earlier version we
     * could override assertEquals, but now that it is a static class this
     * causes problems.
     * @param expect the expected object value
     * @param actual the actual object value
     */
    public static void assertObjectsEqual(Object expect, Object actual) {
        assertObjectsEqual(null, expect, actual);
    }

    /**
     * Assert that two objects are equal.  This can be used to compare very
     * complicated objects that the standard assertEquals method using the 
     * object.equals() method cannot compare correctly.  In earlier version we
     * could override assertEquals, but now that it is a static class this
     * causes problems.
     * @param title to display in error report
     * @param expect the expected object value
     * @param actual the actual object value
     */
    public static void assertObjectsEqual(String title, Object expect, 
                                          Object actual) {

        // If both values are null, everything is cool.
        // If only one is null this should fail, but we'll call the superclass
        // method because it gives a much nicer failure report
        if (expect == null && actual == null) return;
        if (expect == null || actual == null) {
            TestCase.assertEquals(title, expect, actual);
            return;
        }

        // Both objects must belong to the same class
        Class cls = expect.getClass();
        Assert.assertSame(title + " class", cls, actual.getClass());

        // If this is one of the basic class types, the superclass
        // method can be called to handle it.
        if (cls == Character.class ||
            cls == Byte.class || cls == Short.class || cls == Integer.class ||
            cls == Long.class || cls == Boolean.class || cls == Float.class ||
            cls == Double.class || cls == String.class || cls == Date.class) {
            TestCase.assertEquals(title, expect, actual);
            return;
        }

        // If no title was passed, substitute a null string
        if (title == null) title = "";

        // If this is an array, Call ourselves recursively to handle the array
        // length and each individual array element
        if (cls.isArray()) {
            int length = Array.getLength(expect);
            TestCase.assertEquals(title + " array length", length,
                                  Array.getLength(actual));
            for (int ndx = 0; ndx < length; ndx++) {
                assertObjectsEqual(title + "[" + ndx + "]", Array.get(expect, ndx),
                             Array.get(actual, ndx));
            }
            return;
        }

        // Otherwise this is some kind of non trivial class.
        // Call ourselves recursively for all data fields (including static
        // fields, in the class.
        FieldIterator fldIter = new FieldIterator(cls, true);
        while (fldIter.hasNext()) {
            Field fld = (Field)fldIter.next();
            try {
                assertObjectsEqual(title + "." + fld.getName(), fld.get(expect),
                             fld.get(actual));
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // Really can't happen
            }
        }
    }

    /**
     * Set private member of an object to specified value
     * @param obj the object for which the specified value should be set.
     * @param varName The name of the member field to be set.
     * @param value The value to which the member field should be set
     * @throws java.lang.Exception if anything goes wrong
     */
    public static void setPrivateMember(Object obj, String varName, Object value)
    throws Exception {
        setPrivateMember(obj.getClass(), obj, varName, value);
    }
    
    /**
     * Set private static member of specified class to specfied value
     * @param cls The class for which the member value should be set
     * @param varName The name of the member field to be set.
     * @param value The value to which the member field should be set
     * @throws java.lang.Exception if anything goes wrong
     */
    public static  void setPrivateMember(Class cls, String varName, Object value)
    throws Exception {
        setPrivateMember(cls, null, varName, value);
    }

    /**
     * Set private member of class/object to specfied value
     * @param cls The class for which the member value should be set
     * @param obj the object for which the specified value should be set.
     * Can be null if the member field is declared static.
     * @param varName The name of the member field to be set.
     * @param value The value to which the member field should be set
     * @throws java.lang.Exception if anything goes wrong
     */
    private static void setPrivateMember(Class cls, Object obj, String varName,
                                         Object value) throws Exception {
        Field fld = cls.getDeclaredField(varName);
        fld.setAccessible(true);
        fld.set(obj, value);
    }
}