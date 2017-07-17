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

package org.polarion.svnimporter.svnprovider.internal.actions;

import org.polarion.svnimporter.common.IContentRetriever;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.svnprovider.SvnException;
import org.polarion.svnimporter.svnprovider.internal.SvnBranch;
import org.polarion.svnimporter.svnprovider.internal.SvnNodeAction;
import org.polarion.svnimporter.svnprovider.internal.SvnProperties;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 *
 * @author  <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class SvnAddFile extends SvnNodeAction {
    private final IContentRetriever contentRetriever;


    public SvnAddFile(String path, SvnBranch branch, IContentRetriever contentRetriever) {
        super(path, "file", "add", branch);
        this.contentRetriever = contentRetriever;
        setProperties(new SvnProperties());
    }

    protected SvnAddFile(String path, SvnBranch branch,
                         IContentRetriever contentRetriever, String nodeAction) {
        super(path, "file", nodeAction, branch);
        this.contentRetriever = contentRetriever;
        setProperties(new SvnProperties());
    }

    private static final int BUFSIZE = 10000;
    private int textContentLength;
    private String checksum;


    public int getTextContentLength() {
        return textContentLength;
    }

    public String getChecksum() {
        return checksum;
    }


    public void dump(PrintStream out) {
        writeNodePath(out);
        writeNodeKind(out);
        writeNodeAction(out);
        SvnProperties props = getProperties();
        //if (!props.isEmpty()) {
            props.writeLength(out);
        //}
        calculateLengthAndChecksum();
        writeTextContentLength(out, textContentLength);
        out.print("Text-content-md5: " + checksum + "\n");
        writeContentLength(out, textContentLength + /*(props.isEmpty() ? 0 :*/ props.getLength()/*)*/ );
        out.print("\n");
        //if (!props.isEmpty()) {
            props.writeContent(out);
        //}
        writeContent(out);
        out.print("\n\n");
    }

    /**
     * Calculate content length and checksum
     */
    protected void calculateLengthAndChecksum() {
        try {
            textContentLength = 0;
            checksum = null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[BUFSIZE];
            InputStream in = contentRetriever.getContent();
            try {
                int rsize;
                while ((rsize = in.read(buf)) != -1) {
                    md.update(buf, 0, rsize);
                    textContentLength += rsize;
                }
            } finally {
                in.close();
            }
            checksum = Util.toString(md);
        } catch (NoSuchAlgorithmException e) {
            throw new SvnException("can't calculate checksum", e);
        } catch (IOException e) {
            throw new SvnException("can't calculate checksum", e);
        }
    }

    protected void writeContent(PrintStream out) {
        try {
            byte[] buf = new byte[BUFSIZE];
            InputStream in = contentRetriever.getContent();
            try {
                int rsize;
                while ((rsize = in.read(buf)) != -1)
                    out.write(buf, 0, rsize);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new SvnException("can't write content", e);
        }
    }
}

