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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.util.Properties;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class Log {
	private Logger log;
	private static boolean configured = false;
	private boolean enableDebug = false;
	private static final String DEFAULT_LOG4J_RESOURCE = "default_log4j.properties";

	public static void configure(Properties properties) {
		PropertyConfigurator.configure(properties);
		configured = true;
	}

	public static void configureDefault() {
		try {
			InputStream in = Log.class.getResourceAsStream(DEFAULT_LOG4J_RESOURCE);
			try {
				configure(Util.loadProperties(in));
			} finally {
				in.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	public static Log getLog(Class<?> c) {
		if (!configured) configureDefault();
		Log lg = new Log(LogManager.getLogger(c));
		return lg;
	}

	public static Log getLog(String name) {
		if (!configured) configureDefault();
		Log lg = new Log(LogManager.getLogger(name));
		return lg;
	}

	protected Log(Logger log) {
		this.log = log;
	}

	public void debug(Object obj) {
		log.debug(obj);
	}

	public void info(Object obj) {
		log.info(obj);
	}

	public void warn(Object obj) {
		log.warn(obj);
	}

	public void warn(Object obj, Throwable e) {
		log.warn(obj, e);
	}

	public void error(Object obj) {
		log.error(obj);
	}

	public void error(Object obj, Throwable e) {
		log.error(obj, e);
	}

	public void fatal(Object obj) {
		log.fatal(obj);
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled() || enableDebug;
	}

	public void enableDebug() {
		enableDebug = true;
	}
}

