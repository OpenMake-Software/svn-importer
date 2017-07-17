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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author <A HREF="mailto:svnimporter@polarion.org">Polarion Software</A>
 */
public class FileCache {
    private static final Log LOG = Log.getLog(FileCache.class);
    private static final String MAP_FILE = "cache_map";
    private static final String CACHE_IDX = "#cache#idx";
    private final File baseDir;
    private int index = 0;
    private final Map key2file = new HashMap();
    private boolean syncToDisc;

    public FileCache(File baseDir) {
        LOG.info("baseDir=" + baseDir.getAbsolutePath());
        this.baseDir = baseDir;
    }

    public boolean isSyncToDisc() {
        return syncToDisc;
    }

    public void setSyncToDisc(boolean syncToDisc) {
        this.syncToDisc = syncToDisc;
    }

    public void put(Object key, InputStream in) throws IOException {
        synchronized (key2file) {
            String filename;
            if (key2file.containsKey(key)) {
                filename = (String) key2file.get(key);
            } else {
                filename = "cached" + index++;
            }
            LOG.debug("Add to cache: " + key + " -> " + filename);
            OutputStream out = new FileOutputStream(new File(baseDir, filename));
            try {
                Util.copy(in, out);
                if (!key2file.containsKey(key)) {
                    index++;
                }
                key2file.put(key, filename);
                if (syncToDisc) save();
            } finally {
                Util.close(out);
            }
        }
    }

    public void put(Object key, File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            put(key, in);
        } finally {
            Util.close(in);
        }
    }

    public File getFile(Object key) throws IOException {
        String filename = (String) key2file.get(key);
        File file = null;
        if (filename != null) {
            file = new File(baseDir, filename);
            if (!file.exists()) {
                LOG.warn("Cached file deleted: " + file.getAbsolutePath());
                synchronized (key2file) {
                    key2file.remove(key);
                    if (syncToDisc) save();
                }
                file = null;
            }
        }
        return file;
    }

    public InputStream getInputStream(Object key) throws IOException {
        File file = getFile(key);
        if (file != null) return new FileInputStream(file);
        return null;
    }

    public void save() throws IOException {
        Properties props;
        props = new Properties();
        props.setProperty(CACHE_IDX, String.valueOf(index));
        for (Iterator i = key2file.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            String filename = (String) key2file.get(key);
            props.setProperty(String.valueOf(key), filename);
        }
        File outfile = new File(baseDir, MAP_FILE);
        LOG.debug("Saving cache to " + outfile.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(outfile);
        try {
            props.store(out, "");
        } finally {
            Util.close(out);
        }
    }

    public void load() throws IOException {
        File infile = new File(baseDir, MAP_FILE);
        LOG.debug("Loading cache from " + infile.getAbsolutePath());
        if (!infile.isFile()) {
            LOG.warn("Cache map file not exist: " + infile.getAbsolutePath());
            return;
        }

        FileInputStream in = new FileInputStream(infile);
        try {
            Properties props;
            props = new Properties();
            props.load(in);
            for (Iterator i = props.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String value = props.getProperty(key);
                if (CACHE_IDX.equals(key)) {
                    index = Integer.parseInt(value);
                } else {
                    File file = new File(baseDir, value);
                    if (!file.isFile()) {
                        LOG.warn("Cache file not exist: key=" + key + " file=" + file.getAbsolutePath());
                    } else {
                        LOG.debug("cache: "+key+"->"+value);
                        key2file.put(key, value);
                    }
                }
            }
        } finally {
            Util.close(in);
        }
    }

    public boolean containsKey(Object key) {
        return key2file.containsKey(key);
    }
}
