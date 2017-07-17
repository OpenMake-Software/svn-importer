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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *  
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class FilePumper extends Thread {
	
    private Exec exec;
    private InputStream in;
	private File outFile;
	
	private static final int BUFSIZE = 4096;

	public FilePumper(Exec exec, InputStream in, File outFile) {
	    this.exec = exec;
	    this.in = in;
	    this.outFile = outFile;
	}

	public void run() {
	    OutputStream os = null;
		try {
		    
            byte[] buffer = new byte[BUFSIZE];
		    os = new FileOutputStream(outFile);
		    
		    while (true) {
		        int cnt = in.read(buffer);
		        if (cnt < 0) break;
		        os.write(buffer, 0, cnt);
		    }
		} catch (Exception ex) {
		    exec.setException(ex);
		} finally {
			try { in.close(); } catch (IOException ex) {}
            try { if (os != null) os.close(); } catch (IOException ex) {}
		}
	}
}

