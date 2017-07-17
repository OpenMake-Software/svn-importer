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

public class IVSSCheckout extends DispatchPtr {
	public IVSSCheckout(String progid) throws COMException {
		super(progid);
	}
	public IVSSCheckout(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSCheckout(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public String getUsername() throws COMException {
		return (String) get("Username");
	}

	public Date getDate() throws COMException {
		return (Date)get("Date");
	}

	public String getLocalSpec() throws COMException {
		return (String) get("LocalSpec");
	}

	public String getMachine() throws COMException {
		return (String) get("Machine");
	}

	public String getProject() throws COMException {
		return (String) get("Project");
	}

	public String getComment() throws COMException {
		return (String) get("Comment");
	}

	public int getVersionNumber() throws COMException {
		return ((Integer) get("VersionNumber")).intValue();
	}

}
