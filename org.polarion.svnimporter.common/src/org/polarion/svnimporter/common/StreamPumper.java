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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class StreamPumper extends Thread {
    private Exec exec;
	private BufferedReader in;
	private StreamConsumer consumer = null;
	private static final int SIZE = 1;

	public StreamPumper(Exec exec, InputStream in) {
	    this.exec = exec;
		this.in = new BufferedReader(new InputStreamReader(in), SIZE);
	}

	public StreamPumper(Exec exec, InputStream in, String charset) {
	    this.exec = exec;
		try {
			this.in = new BufferedReader(new InputStreamReader(in, charset), SIZE);
		} catch (UnsupportedEncodingException e) {
			throw new CommonException("wrong charset: " + charset, e);
		}
	}

	public void setConsumer(StreamConsumer consumer) {
		this.consumer = consumer;
	}

	public void run() {
		try {
			String s;
			while ((s = in.readLine()) != null) {
				consumeLine(s);
			}
		} catch (Exception e) {
		    exec.setException(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	private void consumeLine(String line) {
		if (consumer != null) {
			consumer.consumeLine(line);
		}
	}
}

