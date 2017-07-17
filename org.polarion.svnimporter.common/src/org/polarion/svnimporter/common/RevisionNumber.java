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

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class RevisionNumber {
	private static final char SEPARATOR = '.';
	private static final String SEPARATOR_RE = "\\" + SEPARATOR;

	/**
	 * "1.2.3.4" -> int[]{1,2,3,4}
	 */
	public static int[] parse(String number) {
		try {
			String[] sa = number.split(SEPARATOR_RE);
			int na[] = new int[sa.length];
			for (int i = 0; i < sa.length; i++)
				na[i] = Integer.parseInt(sa[i]);
			return na;
		} catch (NumberFormatException e) {
			throw new CommonException("wrong revision number: " + number);
		}
	}

	/**
	 * Compare two parsed revision numbers
	 *
	 * @param n1
	 * @param n2
	 * @return
	 */
	public static int compare(int[] n1, int[] n2) {
	    
	    // Do an component by component compare for the the common length 
	    // of both arrays
	    int n = Math.min(n1.length, n2.length);
	    for (int i = 0; i<n; i++) {
	        int diff = n1[i] - n2[i];
	        if (diff != 0) return diff;
	    }
	    
	    // If the match, compare the array lengths, the longer one will
	    // be considered the larger
	    return n1.length - n2.length;
	}

	/**
	 * Compare two revision numbers
	 *
	 * @param sn1
	 * @param sn2
	 * @return
	 */
	public static int compare(String sn1, String sn2) {
		return compare(parse(sn1), parse(sn2));
	}

	/**
	 * Build string revision number from array
	 *
	 * @param n
	 * @return
	 */
	public static String join(int[] n) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < n.length; i++) {
			int x = n[i];
			if (b.length() > 0) b.append(SEPARATOR);
			b.append(x);
		}
		return b.toString();
	}

	/**
	 * Get subnumber from revision number
	 * (for example n={21,22,23,24} from=1 to=3 --- result = "22.23" )
	 *
	 * @param n    - parsed revision number
	 * @param from - start index
	 * @param to   - end index
	 * @return
	 */
	public static String getSubNumber(int[] n, int from, int to) {
		StringBuffer b = new StringBuffer();
		for (int i = from; i < to; i++) {
			if (b.length() > 0) b.append(SEPARATOR);
			b.append(n[i]);
		}
		return b.toString();
	}
	
	/**
	 * Return revision number stripped of last component
	 * @param revNumber revision number
	 * @return revision number without last numeric component
	 * @throws MksException if revision does not have at least two components
	 */
	public static String stripLastComponent(String revNumber) {
	    int ipt = revNumber.lastIndexOf('.');
	    if (ipt < 0) {
	        throw new RuntimeException("Revision number has only one component");
	    }
	    return revNumber.substring(0, ipt);
	}
}

