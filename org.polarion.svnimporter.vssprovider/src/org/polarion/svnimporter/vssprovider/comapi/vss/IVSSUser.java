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

public class IVSSUser extends DispatchPtr
{
	public IVSSUser(String progid) throws COMException { super(progid);}
	public IVSSUser(IUnknown other) throws COMException { super(other);}

	public IVSSUser(GUID ClsID) throws COMException { super(ClsID);}

		public void delete() throws COMException
		{
			invoke("Delete");
		}

		public String getName() throws COMException
		{
			return (String) get("Name");
		}

		public void setName(String pString) throws COMException
		{
			put("Name", pString);
		}

		public void setPassword(String pString) throws COMException
		{
			put("Password", pString);
		}

		public boolean getReadOnly() throws COMException
		{
			return ((Boolean) get("ReadOnly")).booleanValue();
		}

		public void setReadOnly(boolean pboolean) throws COMException
		{
			put("ReadOnly", new Boolean(pboolean));
		}

		public int getProjectRights() throws COMException
		{
			return ((Integer) get("ProjectRights")).intValue();
		}

		public void setProjectRights(int pint) throws COMException
		{
			put("ProjectRights", new Integer(pint));
		}

		public void removeProjectRights(String Project) throws COMException
		{
			invokeN("RemoveProjectRights", new Object[] {Project});
		}


}
