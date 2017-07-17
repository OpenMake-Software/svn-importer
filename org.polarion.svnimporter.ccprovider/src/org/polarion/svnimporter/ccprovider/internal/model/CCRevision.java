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
package org.polarion.svnimporter.ccprovider.internal.model;

import org.polarion.svnimporter.common.model.Revision;
import java.util.Set;
import java.util.HashSet;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class CCRevision extends Revision {
	/**
	 *  Revision state ('add', 'change', 'delete')
	 */
	private CCRevisionState state;

	/**
	 * Ordinal number in branch (first revision has number=1)
	 */
	private int numberInBranch;

    private Set labels = new HashSet();

    public CCRevision(String number) {
		super(number);
	}

	public int getNumberInBranch() {
		return numberInBranch;
	}

	public void setNumberInBranch(int numberInBranch) {
		this.numberInBranch = numberInBranch;
	}

	public CCRevisionState getState() {
		return state;
	}

	public void setState(CCRevisionState state) {
		this.state = state;
	}

    public CCLabel newLabel(String labelName) {
        CCLabel label = new CCLabel(this, labelName);
        labels.add(label);
        return label;
    }

    public CCLabel[] getLabels() {
        return (CCLabel[]) labels.toArray(new CCLabel[0]);
    }
}
