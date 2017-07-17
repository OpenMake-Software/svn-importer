/** created by JawinGen version 1.0.57
*   from TypeLib at C:\Program Files\Microsoft Visual Studio\VSS\win32\SSAPI.DLL
*   using escape file at D:\Documents and Settings\My Documents\Java\jawin\jawingen\bin\tne.xml
*   check for latest version at http://www.gehtland.com/jawingen
*/
package org.polarion.svnimporter.vssprovider.comapi.vss;

import com.develop.jawin.COMException;
import com.develop.jawin.DispatchPtr;
import com.develop.jawin.GUID;
import com.develop.jawin.IUnknown;

public class IVSSDatabase extends DispatchPtr {
	public IVSSDatabase(String progid) throws COMException {
		super(progid);
	}
	public IVSSDatabase(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSDatabase(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public void open(String SrcSafeIni, String Username, String Password) throws COMException {
		invokeN("Open", new Object[] { SrcSafeIni, Username, Password });
	}

	public String getSrcSafeIni() throws COMException {
		return (String) get("SrcSafeIni");
	}

	public String getDatabaseName() throws COMException {
		return (String) get("DatabaseName");
	}

	public String getUsername() throws COMException {
		return (String) get("Username");
	}

	public String getCurrentProject() throws COMException {
		return (String) get("CurrentProject");
	}

	public void setCurrentProject(String pString) throws COMException {
		put("CurrentProject", pString);
	}

	public IVSSItem getVSSItem(String Spec) throws COMException {
		return (new IVSSItem((DispatchPtr) getN("VSSItem", new Object[] { Spec, Boolean.FALSE })));
	}

	public IVSSItem getVSSItem(String Spec, boolean Deleted) throws COMException {
		return (new IVSSItem((DispatchPtr) getN("VSSItem", new Object[] { Spec, new Boolean(Deleted)})));
	}

	public IVSSUser addUser(String User, String Password, boolean ReadOnly) throws COMException {
		return (
			new IVSSUser(
				(DispatchPtr) invokeN("AddUser",
				new Object[] { User, Password, new Boolean(ReadOnly)})));
	}

	public IVSSUser getUser() throws COMException {
		return (new IVSSUser((DispatchPtr) get("User")));
	}

	public IVSSUsers getUsers() throws COMException {
		return (new IVSSUsers((DispatchPtr) get("Users")));
	}

	public boolean getProjectRightsEnabled() throws COMException {
		return ((Boolean) get("ProjectRightsEnabled")).booleanValue();
	}

	public void setProjectRightsEnabled(boolean pboolean) throws COMException {
		put("ProjectRightsEnabled", new Boolean(pboolean));
	}

	public int getDefaultProjectRights() throws COMException {
		return ((Integer) get("DefaultProjectRights")).intValue();
	}

	public void setDefaultProjectRights(int pint) throws COMException {
		put("DefaultProjectRights", new Integer(pint));
	}

}
