/*
 * Copyright (c) 2004, 2005 Polarion Software, All rights reserved. 
 * Email: community@polarion.org
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 (the "License"). You may not use 
 * this file except in compliance with the License. Copy of the License is
 * located in the file LICENSE.txt in the project distribution. You may also
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  
 * POLARION SOFTWARE MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. POLARION SOFTWARE
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 */
/*
 * $Log$
 */
package org.polarion.svnimporter.vssprovider.internal.model;

import org.polarion.svnimporter.common.model.Commit;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class VssCommit extends Commit {
	public VssCommit() {
	}

//	public String getDebugInfo() {
//		StringBuffer b = new StringBuffer();
//		b.append("Commit d[" + Util.toString(getDate()) + "] a[" + getAuthor() + "] c[" + Util.escape(getMessage()) + "]\n");
//		for (Iterator i = getFileRevisions().iterator(); i.hasNext();) {
//			VssRevision revision = (VssRevision) i.next();
//			VssFile vssFile = (VssFile) revision.getModelFile();
//			b.append(vssFile.getVssPath() + "\n");
//			b.append("\t" + revision.getDebugInfo() + "\n");
//		}
//		return b.toString();
//	}
}

