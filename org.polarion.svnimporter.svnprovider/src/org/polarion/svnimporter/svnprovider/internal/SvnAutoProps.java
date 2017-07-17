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

import org.polarion.svnimporter.common.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAutoProps {
    private static final Log LOG = Log.getLog(SvnAutoProps.class);

    private List entries;
	private Properties properties;

	public SvnAutoProps(String configFilePath) {
		entries = new LinkedList();
		if (configFilePath == null)
			return;
		try {
            properties = new Properties();
            FileInputStream in = new FileInputStream(configFilePath);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			//throw new SvnException(e);
            LOG.warn("Cannot load autoprops: "+e.getMessage());
        }
		Enumeration e = properties.keys();
		while (e.hasMoreElements()) {
				String filePattern = (String) e.nextElement();
				Entry entry = new Entry(filePattern);
				String props = (String) properties.get(filePattern);
				StringTokenizer tok = new StringTokenizer(props, ";");
				while(tok.hasMoreTokens()) {
					String pair = tok.nextToken();
					String propkey, propval;
					int sep = pair.indexOf('=');
					if (sep >= 0) {
						propkey	= pair.substring(0, sep).trim();
						propval	= pair.substring(sep + 1).trim();
					} else {
						propkey = pair.trim();
						propval = null;
					}
					entry.addProperty(propkey, propval);
					//System.out.println("Pattern: " + filePattern + "; property: " + propkey + ", " + propval);
				}
				entries.add(entry);

		}

	}


	public SvnProperties getProperties(String file) {

		SvnProperties props = new SvnProperties();

		for (Iterator it = entries.iterator(); it.hasNext(); ) {
			Entry entry = (Entry)it.next();
			if (entry.pattern.matcher(file).matches()) {
				//System.out.println("File \"" + file + "\" matches pattern " + entry.pattern.pattern());
				props.setAll(entry.props);
			}
		}
		return props;
	}


	class Entry {
		Pattern pattern;
		Map props;

		Entry(String stringPattern) {
			// transform pattern into regular expressions
			// replace "." by "\."
			String s = stringPattern.replaceAll("\\.", "\\\\.");
			// replace "?" by "."
			s = s.replaceAll("\\?", ".");
			// replace "*" by ".*"
			s = s.replaceAll("\\*", ".*");
			pattern = Pattern.compile(s);
			props = new TreeMap();
		}

		void addProperty(String key, String value) {
			props.put(key, value);
		}

		Map getProperties() {
			return props;
		}
	}

}
