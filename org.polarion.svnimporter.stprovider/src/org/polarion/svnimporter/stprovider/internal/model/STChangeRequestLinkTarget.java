/***************************************************************************************************
 * Copyright (c) 2006 Gunnar Wagenknecht and others.
 * All rights reserved. 
 *
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gunnar Wagenknecht - initial API and implementation
 *               Eclipse.org - concepts and code used from Eclipse projects
 **************************************************************************************************/
package org.polarion.svnimporter.stprovider.internal.model;

/**
 * 
 */
public interface STChangeRequestLinkTarget {

	/**
	 * @return the list of linked crs (sorted in the natural order of {@link STChangeRequest})
	 */
	STChangeRequest[] getLinkedChangeRequests();

}