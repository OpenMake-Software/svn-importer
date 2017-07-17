/** created by JawinGen version 1.0.57
*   from TypeLib at C:\Program Files\Microsoft Visual Studio\VSS\win32\SSAPI.DLL
*   using escape file at D:\Documents and Settings\My Documents\Java\jawin\jawingen\bin\tne.xml
*   check for latest version at http://www.gehtland.com/jawingen
*/
package org.polarion.svnimporter.vssprovider.comapi.vss;

import java.util.Date;

import com.develop.jawin.COMException;
import com.develop.jawin.DispatchPtr;
import com.develop.jawin.GUID;
import com.develop.jawin.IUnknown;

public class IVSSVersion extends DispatchPtr {
	public IVSSVersion(String progid) throws COMException {
		super(progid);
	}
	public IVSSVersion(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSVersion(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public String getUsername() throws COMException {
		return (String) get("Username");
	}

	public int getVersionNumber() throws COMException {
		return ((Integer) get("VersionNumber")).intValue();
	}

	public String getAction() throws COMException {
		return (String) get("Action");
	}

	public Date getDate() throws COMException {
		return (Date) get("Date");
	}

	public String getComment() throws COMException {
		return (String) get("Comment");
	}

	public String getLabel() throws COMException {
		return (String) get("Label");
	}

	public IVSSItem getVSSItem() throws COMException {
		return (new IVSSItem((DispatchPtr) get("VSSItem")));
	}

	public String getLabelComment() throws COMException {
		return (String) get("LabelComment");
	}

}
