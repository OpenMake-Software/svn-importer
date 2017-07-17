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
package org.polarion.svnimporter.stprovider.internal.model;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.polarion.svnimporter.common.model.Model;
import org.polarion.svnimporter.stprovider.internal.STConfig;

/**
 * The model
 */
public class STModel extends Model {
	
	private final SortedMap changeRequestsByNumber = new TreeMap();
	private STConfig config;
	
	/**
	 * @return the config
	 */
	public STConfig getConfig() {
		return config;
	}
	
	/**
	 * @param config the config to set
	 */
	public void setConfig(STConfig config) {
		this.config = config;
	}
	
	public STChangeRequest newChangeRequest(int number) {
    	Integer crNumber = new Integer(number);
    	if(changeRequestsByNumber.containsKey(crNumber))
    		return (STChangeRequest) changeRequestsByNumber.get(crNumber);

    	STChangeRequest newCr = new STChangeRequest(number);
    	changeRequestsByNumber.put(crNumber, newCr);
    	return newCr;
    }

    public STChangeRequest removeChangeRequest(STChangeRequest changeRequest) {
    	return (STChangeRequest) changeRequestsByNumber.remove(new Integer(changeRequest.getNumber()));
    }
    
	public void finishModel() {
		for (Iterator i = getFiles().values().iterator(); i.hasNext();) {
			STFile file = (STFile) i.next();
			for (Iterator j = file.getBranches().values().iterator(); j.hasNext();) {
				STBranch branch = (STBranch) j.next();
				branch.handleDeletedRevisions();
				branch.resolveRevisionStates();
			}
		}
		separateCommits();
	}
	
	private void separateCommits() {
		STCommitsCollection c = new STCommitsCollection(this);
		c.addFiles(getFiles().values());
		getCommits().clear();
		getCommits().addAll(c.separateCommits());
	}
}

