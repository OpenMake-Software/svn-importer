package org.polarion.svnimporter.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import java.lang.reflect.*;

/**
 * This class will iterate through all of the data members of a specified class
 * returning the Field class for each data member.   All declared fields are 
 * processed, even thoughs with private or package access, and the Field will be
 * made accessible before it is returned.  Final members are not included,
 * static members may or may not be included but will not by default.
 */
public class FieldIterator implements Iterator {
	
	private Class nextCls;
	private Class baseCls;
	private boolean incStatic;
	
	private Field[] flds = null;
	private int fldNdx;
	private Field nextFld = null;
	
	/**
	 * Construct Field Iterator to iterate through all of the non static / non final
	 * fields in specfied class
	 * @param cls the class.
	 */
	public FieldIterator(Class cls) {
		this(cls, null, false);
	}
	
	/**
	 * Construct Field Iterator to iterate through all of the non static / non final
	 * fields in specfied class down to a specified base class
	 * @param cls the class.
	 * @param baseCls the base class
	 */
	public FieldIterator(Class cls, Class baseCls) {
		this(cls, baseCls, false);
	}
	
	/**
	 * Construct Field Iterator to iterate through all of the non final
	 * fields in specfied class down to a specified base class
	 * @param cls the class.
	 * @param incStatic true if static fields should be included, false if they
	 * should be excluded.
	 */
	public FieldIterator(Class cls, boolean incStatic) {
		this(cls, null, incStatic);
	}
	
	/**
	 * Construct Field Iterator to iterate through all of the non final
	 * fields in specfied class down to a specified base class
	 * @param cls the class.
	 * @param baseCls the base class
	 * @param incStatic true if static fields should be included, false if they
	 * should be excluded.
	 */
	public FieldIterator(Class cls, Class baseCls, boolean incStatic) {
		
		// If no base class was specified, go down to the Object class
		if (baseCls == null) baseCls = Object.class;
		
		// Make sure that base class is assignable from class, and that they
		// aren't the same
		if (! baseCls.isAssignableFrom(cls)) {
			throw new IllegalArgumentException("base class must be assignable from requested class");
		}
	
		this.nextCls = cls;
		this.baseCls = baseCls;
		this.incStatic = incStatic;
		getNextField();
	}

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
    	return (nextFld != null);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
    	if (nextFld == null) throw new NoSuchElementException();
    	Field result = nextFld;
    	getNextField();
    	return result;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
    	throw new UnsupportedOperationException();
    }

	/**
	 * Bump nextFld to the next field in class
	 */
	private void getNextField() {
		while (true) {
			if (flds == null || fldNdx >= flds.length) {
				if (nextCls == baseCls) {
					nextFld = null;
					return;
				}
				flds = nextCls.getDeclaredFields();
				fldNdx = 0;
				nextCls = nextCls.getSuperclass();
				continue;
			}
			nextFld = flds[fldNdx++];
            
            // Java creates some weird static classes every now and then
            if (nextFld.getName().startsWith("class$")) continue;
            
            // Final members are never returned, static members are only
            // returned if specfically requested.
			int mods = nextFld.getModifiers();
			if (Modifier.isFinal(mods)) continue;
			if (!incStatic && Modifier.isStatic(mods)) continue;
			
			nextFld.setAccessible(true);
			return;
		} 
	}
}
