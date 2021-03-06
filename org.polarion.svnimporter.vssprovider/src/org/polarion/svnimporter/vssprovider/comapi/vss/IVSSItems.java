/** created by JawinGen version 1.0.57
*   from TypeLib at C:\Program Files\Microsoft Visual Studio\VSS\win32\SSAPI.DLL
*   using escape file at D:\Documents and Settings\My Documents\Java\jawin\jawingen\bin\tne.xml
*   check for latest version at http://www.gehtland.com/jawingen
*/
package org.polarion.svnimporter.vssprovider.comapi.vss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.develop.jawin.COMException;
import com.develop.jawin.DispatchPtr;
import com.develop.jawin.GUID;
import com.develop.jawin.IEnumVariant;
import com.develop.jawin.IUnknown;

public class IVSSItems extends DispatchPtr {
	private IUnknown _newEnum;

	private Collection _items;

	public IVSSItems(String progid) throws COMException {
		super(progid);
	}

	public IVSSItems(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSItems(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public int getCount() throws COMException {
		return ((Integer) get("Count")).intValue();
	}

	public IVSSItem getItem(int index) throws COMException {
		return (new IVSSItem((DispatchPtr) get("Item", new Integer(index))));
	}

	public IVSSItem getItem(String name) throws COMException {
		return (new IVSSItem((DispatchPtr) get("Item", name)));
	}

	private IEnumVariant _NewEnum() throws COMException {
		if (_newEnum == null) {
			_newEnum = (IUnknown) invoke("_NewEnum");
		}
		return (IEnumVariant) (_newEnum).queryInterface(IEnumVariant.class);
	}

	public synchronized Collection getCollection() throws COMException {
		if (_items != null) {
			return _items;
		}
		final int count = getCount();
		
		if (count == 0) {
			return Collections.EMPTY_LIST;			
		}

		_items = new ArrayList(count);
		IEnumVariant enumVariant = _NewEnum();
		Object[] objects = new Object[count];
		int filledObjects = enumVariant.Next(count, objects);
		for (int i = 0; i < filledObjects; ++i) {
			_items.add(new IVSSItem((IUnknown) objects[i]));
		}
		enumVariant.close();
		return _items;
	}

	/* (non-Javadoc)
	 * @see org.jawin.COMPtr#close()
	 */
	public synchronized void close() {
		if (_newEnum != null) {
			_newEnum.close();
			_newEnum = null;
		}

		if (_items != null) {
			for (Iterator i = _items.iterator(); i.hasNext();) {
				((IUnknown) i.next()).close();
			}
			_items = null;
		}
		super.close();
	}
}
