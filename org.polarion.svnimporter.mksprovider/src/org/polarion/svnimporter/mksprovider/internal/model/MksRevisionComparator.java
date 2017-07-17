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
package org.polarion.svnimporter.mksprovider.internal.model;

import org.polarion.svnimporter.common.RevisionNumber;
import org.polarion.svnimporter.common.model.Revision;

import java.util.Comparator;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class MksRevisionComparator<T extends Revision> implements Comparator<T>{
	public static final MksRevisionComparator<MksRevision> INSTANCE = new MksRevisionComparator<MksRevision>(+1);
	public static final MksRevisionComparator<MksRevision> REV_INSTANCE = new MksRevisionComparator<MksRevision>(-1);
  public static final MksRevisionComparator<MksCheckpoint> CHECKPOINT_INSTANCE = new MksRevisionComparator<MksCheckpoint>(+1);
  public static final MksRevisionComparator<MksCheckpoint> CHECKPOINT_REV_INSTANCE = new MksRevisionComparator<MksCheckpoint>(-1);
	
	

	private int sign;
	
	private MksRevisionComparator(int sign) {
	    this.sign = sign;
	}

	public int compare(T r1, T r2) {
		if (r1 == r2) return 0;
		
		// Have to cast to revision because this might be a MksRevision
		// or a MksCheckpoint
		return sign*RevisionNumber.compare(r1.getNumber(), r2.getNumber());
	}
}

