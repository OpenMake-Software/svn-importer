/** created by JawinGen version 1.0.57
*   from TypeLib at C:\Program Files\Microsoft Visual Studio\VSS\win32\SSAPI.DLL
*   using escape file at D:\Documents and Settings\My Documents\Java\jawin\jawingen\bin\tne.xml
*   check for latest version at http://www.gehtland.com/jawingen
*/
package org.polarion.svnimporter.vssprovider.comapi.vss;

import java.util.ArrayList;
import java.util.Collection;

import com.develop.jawin.COMException;
import com.develop.jawin.DispatchPtr;
import com.develop.jawin.GUID;
import com.develop.jawin.IUnknown;

public class IVSSUsers extends DispatchPtr {
	public IVSSUsers(String progid) throws COMException {
		super(progid);
	}
	public IVSSUsers(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSUsers(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public int getCount() throws COMException {
		return ((Integer) get("Count")).intValue();
	}

	public IVSSUser getItem(int index) throws COMException {
		return (new IVSSUser((DispatchPtr) get("Item", new Integer(index))));
	}

	public IVSSUser getItem(String name) throws COMException {
		return (new IVSSUser((DispatchPtr) get("Item", name)));
	}

/*	This does not seem to work.
 	public IEnumVariant _NewEnum() throws COMException {
		return (IEnumVariant) invoke("_NewEnum");
	}*/
	
	public Collection getCollection() throws COMException {
		final int count = getCount();
		final Collection col = new ArrayList(count);
		for (int i = 1; i <= count; ++i) {
			col.add(getItem(i));
		}
		return col;
	}
}
