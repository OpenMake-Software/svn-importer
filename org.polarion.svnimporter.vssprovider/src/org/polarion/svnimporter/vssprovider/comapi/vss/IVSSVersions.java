/** created by JawinGen version 1.0.57
*   from TypeLib at C:\Program Files\Microsoft Visual Studio\VSS\win32\SSAPI.DLL
*   using escape file at D:\Documents and Settings\My Documents\Java\jawin\jawingen\bin\tne.xml
*   check for latest version at http://www.gehtland.com/jawingen
*/
package org.polarion.svnimporter.vssprovider.comapi.vss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.develop.jawin.COMException;
import com.develop.jawin.DispatchPtr;
import com.develop.jawin.GUID;
import com.develop.jawin.IEnumVariant;
import com.develop.jawin.IUnknown;

public class IVSSVersions extends DispatchPtr {
	private IUnknown _newEnum;

	private Collection _referencedVersions;

	public IVSSVersions(String progid) throws COMException {
		super(progid);
	}
	public IVSSVersions(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSVersions(GUID ClsID) throws COMException {
		super(ClsID);
	}

	private IEnumVariant _NewEnum() throws COMException {
		if (_newEnum == null) {
			_newEnum = (IUnknown) invoke("_NewEnum");
		}
		return (IEnumVariant) (_newEnum).queryInterface(IEnumVariant.class);
	}

	public synchronized Collection getNext(int maxCount) throws COMException {
		if (_referencedVersions == null) {
			_referencedVersions = new ArrayList(maxCount);
		}
		Collection versions = new ArrayList(maxCount); 
		IEnumVariant enumVariant = _NewEnum();
		Object[] objects = new Object[maxCount];
		int filledObjects = enumVariant.Next(maxCount, objects);
		for (int i = 0; i < filledObjects; ++i) {
			versions.add(new IVSSVersion((IUnknown) objects[i]));
		}
		enumVariant.close();
		_referencedVersions.addAll(versions);
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.jawin.COMPtr#close()
	 */
	public synchronized void close() {
		if (_newEnum != null) {
			_newEnum.close();
			_newEnum = null;
		}
		if (_referencedVersions != null) {
			for (Iterator i = _referencedVersions.iterator(); i.hasNext();) {
				((IUnknown) i.next()).close();
			}
			_referencedVersions = null;
		}
		super.close();
	}
}
