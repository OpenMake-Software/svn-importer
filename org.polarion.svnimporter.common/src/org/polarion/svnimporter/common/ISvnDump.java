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

package org.polarion.svnimporter.common;

import java.util.List;
import java.util.Date;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public interface ISvnDump {

	/** dump a SvnModel.
	 * @param model
	 * @param lastDate if set, only revisions up to lastDate will be dumped.
	 */
    public void dump(ISvnModel model, Date lastDate);

	/** dump a SvnModel completely.
	 * @param model
	 */
    public void dump(ISvnModel model);

	/**
	 * return the list of dump file names.
	 */
	public List getFileNames();

}

