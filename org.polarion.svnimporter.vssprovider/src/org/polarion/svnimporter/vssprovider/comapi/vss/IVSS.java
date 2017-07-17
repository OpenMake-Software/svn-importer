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

public class IVSS extends DispatchPtr
{
	public IVSS(String progid) throws COMException { super(progid);}
	public IVSS(IUnknown other) throws COMException { super(other);}

	public IVSS(GUID ClsID) throws COMException { super(ClsID);}

		public IVSSDatabase getVSSDatabase() throws COMException
		{
			return ( new IVSSDatabase((DispatchPtr)  get("VSSDatabase")));
		}


}
