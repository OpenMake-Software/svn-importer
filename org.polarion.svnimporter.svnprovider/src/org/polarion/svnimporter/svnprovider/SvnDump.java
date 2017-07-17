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

package org.polarion.svnimporter.svnprovider;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ISvnDump;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.svnprovider.internal.SvnRevision;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/** Represents a dump as generated during a single run of svnimporter.
 * It may comprise several dump files.
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnDump  implements ISvnDump {

	private static final Log LOG = Log.getLog(SvnDump.class);

	/** template for the file name of the first dump file in this dump.
	 *
	 */
	private String firstFileNamePattern;

	/** template for the file names of all subsequent files in this dump.
	 *
	 */
	private String nextFileNamePattern;

	/** DateFormat for generating file names from the patterns.
	 *
	 */
	private DateFormat dateStringFormat;

	/** size limit (in bytes) for the dump files.
	 * If this limit is reached, a new dump file is automatically created
	 * for the next revisions to dump.
	 */
	private long fileSizeLimit;


	private LinkedList fileNames;

	private PrintStream out;
    private ByteCounterOutputStream outCounterStream;

    /**
     * Number of current dump part
     */
    private int partNumber = 0;


    /** create a new SvnDump
	 *
	 * @param sizeLimit the limit of the dump files in MB, as given in the configuration file.
	 */
	public SvnDump(String firstFilePattern, String nextFilePattern, DateFormat dateFormat, int sizeLimit)
	{
		firstFileNamePattern = firstFilePattern;
		nextFileNamePattern = nextFilePattern;
		dateStringFormat = dateFormat;
		fileNames = new LinkedList();
		setCurFileName(firstFileNamePattern);
		// the given file size limit is in MB, internally we store it in Bytes.
		fileSizeLimit = (long)sizeLimit * 1024 * 1024;
    }

	/** create a new SvnDump, using a single file name pattern for all files.
	 * This is used for incremental dumps.
	 */
	public SvnDump(String filePattern, DateFormat dateFormat, int sizeLimit)
	{
		this(filePattern, filePattern, dateFormat, sizeLimit);
	}

	/** create a simple SvnDump, dumping a single file. Useful for debugging only.
	 */
	public SvnDump(String filePattern)
	{
		this(filePattern, filePattern, new SimpleDateFormat("yyyyMMdd_HHmmss"), 0);
	}

	/** get the list of file names in this dump. */
	public List getFileNames() {
		return fileNames;
	}


	private String getCurFileName() {
		if (fileNames.isEmpty())
			return null;
		return (String) fileNames.getLast();
	}


//	private void beginDump() throws IOException
//	{
//		if (out != null) {
//			out.close();
//		}
//        outCounterStream = new ByteCounterOutputStream(new FileOutputStream(getCurFileName()));
//        out = new PrintStream(outCounterStream);
//        out.print("SVN-fs-dump-format-version: 2\n\n");
//	}

    private void beginDump() throws IOException
    {
       if (out != null) {
          out.close();
       }
       outCounterStream = new ByteCounterOutputStream(new BufferedOutputStream(new FileOutputStream(getCurFileName())));
       out = new PrintStream(outCounterStream);
       out.print("SVN-fs-dump-format-version: 2\n\n");
    }

	private long getCurFileSize()
	{
        if (out == null) return 0;
        return outCounterStream.getWritten();
    }

	// copied from MainConfig.java. Should be moved to Util.
	private String insertDate(String value) {
		int indexOfDate = value.indexOf("%date%");
		if (indexOfDate != -1) {
			return value.replaceAll("%date%", dateStringFormat.format(new Date()));
		} else {
			return value;
		}
	}

	private void setCurFileName(String pattern)
	{
        String filename = insertDate(pattern);
        if (partNumber > 0)
            filename += "_part" + partNumber;
        fileNames.add(filename);
	}

	public void nextDumpFileIfNeeded() throws IOException
	{
		if (fileSizeLimit == 0) return;
		if (getCurFileSize() >= fileSizeLimit) {
			LOG.info("****************************************************************************");
			LOG.info("Dump file \"" + getCurFileName() + "\" has reached size limit.");
            partNumber++;
            setCurFileName(nextFileNamePattern);
			LOG.info("Creating new dump file \"" + getCurFileName() + "\"");
			beginDump();
		}
	}


	/**
	 * Save model in svn dump format.
	 * Before dumping a revision, check for the size of the current dump file and
	 * create a new one, if necessary.
	 *
	 */
	public void dump(ISvnModel model, Date lastDate) {
		try {
	   	beginDump();
			for (Iterator i = model.getRevisions().iterator(); i.hasNext(); ) {
				SvnRevision revision = (SvnRevision) i.next();
				if (lastDate != null && lastDate.before(revision.getRevisionDate()))
					break;
				nextDumpFileIfNeeded();
				revision.dump(out);
			}
		} catch (IOException e) {
			LOG.error("cannot dump SvnModel", e);
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	/**
	 * Save model in svn dump format, ignoring lastDate.
	 */
	public void dump(ISvnModel model) {
		dump(model, null);
	}

//    /**
//     * Output stream which counts number of written bytes
//     */
//    private class ByteCounterOutputStream extends FilterOutputStream {
//        /**
//         * The number of bytes written to the output stream
//         */
//        private long written = 0;
//
//        public ByteCounterOutputStream(OutputStream out) {
//            super(out);
//        }
//
//        public void write(int b) throws IOException {
//            super.write(b);
//            written++;
//        }
//
//        /**
//         * The number of bytes written to the output stream
//         *
//         * @return
//         */
//        public long getWritten() {
//            return written;
//        }
//    }
	
    /**
     * Output stream which counts number of written bytes
     */
    private class ByteCounterOutputStream extends FilterOutputStream {
        /**
         * The number of bytes written to the output stream
         */
        private long written = 0;

        public ByteCounterOutputStream(OutputStream out) {
            super(out);
        }

        public void write(int b) throws IOException {
            super.write(b);
            written++;
        }
       
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            written += len;
        }
       
        public void write(byte[] b) throws IOException {
            super.write(b);
            written += b.length;
        }
       
        /**
         * The number of bytes written to the output stream
         *
         * @return
         */
        public long getWritten() {
            return written;
        }
    }

}
