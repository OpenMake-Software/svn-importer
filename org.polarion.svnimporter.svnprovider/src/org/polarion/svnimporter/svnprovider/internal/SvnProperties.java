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

package org.polarion.svnimporter.svnprovider.internal;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnProperties {
	/**
	 * Map property key to SvnProperty (sorted by key)
	 */
	private final Map properties = new TreeMap();

	/**
	 * Total properties length (in dump file format)
	 *
	 */
	private int totalLen = 10; //len('PROPS-END\n')

	/**
	 * Record property
	 */
	public void set(String key, String value) {
		if (properties.containsKey(key)) { // this will probably not happen
			totalLen -= calcPropLength(key, (String) properties.get(key));
		}
		properties.put(key, value);
		totalLen += calcPropLength(key, value);
	}

	public String get(String key) {
		return (String) properties.get(key);
	}

	public void setAll(SvnProperties source)
	{
		setAll(source.properties);
	}

	public void setAll(Map props)
	{
		// we cannot use properties.putAll since we have to calculate the property length
		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			set(key, (String)props.get(key));
		}
	}

	/**
	 * return properties length (in dump file format)
	 *
	 * @return
	 */
	public int getLength()
	{
		return totalLen;
	}

	private int calcPropLength(String key, String value) {
		int klen = getLength(key);
		klen += getLength("K " + klen);
		int vlen = getLength(value);
		vlen += getLength("V " + vlen);
		return klen + vlen + 4;// + 4 for the four newlines within a given property's section
	}


	public String getDebugInfo() {
		StringBuffer b = new StringBuffer();
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) properties.get(key);
			if (b.length() > 0) b.append(", ");
			b.append(key + "=" + value);
		}
		return b.toString();
	}

	/**
	 * return true if no properties are set
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return properties.size() < 1;
	}


	public void writeLength(PrintStream out) {
		out.print("Prop-content-length: " + getLength() + "\n");
	}

	public void writeContent(PrintStream out) {
		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) properties.get(key);
			out.print("K " + getLength(key) + "\n");
			out.print(key + "\n");
			out.print("V " + getLength(value) + "\n");
			if (value == null)
				out.print("\n");
			else
				out.print(value + "\n");
		}
		out.print("PROPS-END\n");
	}

	/**
	 * Write properties in dump file format to stream.
	 * This works only for revision properties and for node action properties
	 * where the properties form the only content.
	 *
	 * @param out
	 */
	public void dump(PrintStream out) {
		writeLength(out);
		out.print("Content-length: " + getLength() + "\n");
		out.print("\n");
		writeContent(out);
      out.print("\n");
      out.print("\n");
	}

	/**
	 * Special for unicode strings
	 */
	private int getLength(String s) {
		if (s == null) return 0;
		return s.getBytes().length;
	}
}
