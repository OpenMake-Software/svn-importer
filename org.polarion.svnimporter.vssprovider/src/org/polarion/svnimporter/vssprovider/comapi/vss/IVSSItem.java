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

public class IVSSItem extends DispatchPtr {
	public IVSSItem(String progid) throws COMException {
		super(progid);
	}
	public IVSSItem(IUnknown other) throws COMException {
		super(other);
	}

	public IVSSItem(GUID ClsID) throws COMException {
		super(ClsID);
	}

	public String getSpec() throws COMException {
		return (String) get("Spec");
	}

	public boolean getBinary() throws COMException {
		return ((Boolean) get("Binary")).booleanValue();
	}

	public void setBinary(boolean pboolean) throws COMException {
		put("Binary", new Boolean(pboolean));
	}

	public boolean isDeleted() throws COMException {
		return ((Boolean) get("Deleted")).booleanValue();
	}

	public void setDeleted(boolean pboolean) throws COMException {
		put("Deleted", new Boolean(pboolean));
	}

	public int getType() throws COMException {
		return ((Integer) get("Type")).intValue();
	}

	public String getLocalSpec() throws COMException {
		return (String) get("LocalSpec");
	}

	public void setLocalSpec(String pString) throws COMException {
		put("LocalSpec", pString);
	}

	public String getName() throws COMException {
		return (String) get("Name");
	}

	public void setName(String pString) throws COMException {
		put("Name", pString);
	}

	public IVSSItem getParent() throws COMException {
		return (new IVSSItem((DispatchPtr) get("Parent")));
	}

	public int getVersionNumber() throws COMException {
		return ((Integer) get("VersionNumber")).intValue();
	}

	public IVSSItems getItems(boolean IncludeDeleted) throws COMException {
		return (new IVSSItems((DispatchPtr) get("Items", new Boolean(IncludeDeleted))));
	}

	public IVSSItems getItems() throws COMException {
		return (new IVSSItems((DispatchPtr) get("Items", Boolean.FALSE)));
	}

	public void get(String Local, int iFlags) throws COMException {
		invokeN("Get", new Object[] { Local, new Integer(iFlags)});
	}

	public void checkout(String comment, String local, int iFlags) throws COMException {
		invokeN("Checkout", new Object[] { comment, local, new Integer(iFlags)});
	}

	public void checkin(String comment, String local, int iFlags) throws COMException {
		invokeN("Checkin", new Object[] { comment, local, new Integer(iFlags)});
	}

	public void undoCheckout(String Local, int iFlags) throws COMException {
		invokeN("UndoCheckout", new Object[] { Local, new Integer(iFlags)});
	}

	public int isCheckedOut() throws COMException {
		return ((Integer) get("IsCheckedOut")).intValue();
	}

	public boolean isFile() throws COMException {
		return (getType() == VSSItemType.VSSITEM_FILE);
	}

	public boolean isProject() throws COMException {
		return (getType() == VSSItemType.VSSITEM_PROJECT);
	}
	
	public IVSSCheckouts getCheckouts() throws COMException {
		return (new IVSSCheckouts((DispatchPtr) get("Checkouts")));
	}

	public boolean isDifferent(String Local) throws COMException {
		return ((Boolean) get("IsDifferent", Local)).booleanValue();
	}

	public IVSSItem add(String Local, String Comment, int iFlags) throws COMException {
		return (
			new IVSSItem((DispatchPtr) invokeN("Add", new Object[] { Local, Comment, new Integer(iFlags)})));
	}

	public IVSSItem newSubproject(String Name, String Comment) throws COMException {
		return (new IVSSItem((DispatchPtr) invokeN("NewSubproject", new Object[] { Name, Comment })));
	}

	public void share(IVSSItem pIItem, String Comment, int iFlags) throws COMException {
		invokeN("Share", new Object[] { pIItem, Comment, new Integer(iFlags)});
	}

	public void destroy() throws COMException {
		invoke("Destroy");
	}

	public void move(IVSSItem pINewParent) throws COMException {
		invokeN("Move", new Object[] { pINewParent });
	}

	public void label(String Label, String Comment) throws COMException {
		invokeN("Label", new Object[] { Label, Comment });
	}

	public IVSSVersions getVersions(int iFlags) throws COMException {
		return (new IVSSVersions((DispatchPtr) get("Versions", new Integer(iFlags))));
	}

	public IVSSItem getVersion(int version) throws COMException {
		return (new IVSSItem((DispatchPtr) get("Version", new Integer(version))));
	}

	public IVSSItem getVersion(String label) throws COMException {
		return (new IVSSItem((DispatchPtr) get("Version", label)));
	}

	public IVSSItems getLinks() throws COMException {
		return (new IVSSItems((DispatchPtr) get("Links")));
	}

	public IVSSItem branch(String Comment, int iFlags) throws COMException {
		return (new IVSSItem((DispatchPtr) invokeN("Branch", new Object[] { Comment, new Integer(iFlags)})));
	}
}
