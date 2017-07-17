package org.polarion.svnimporter.cvsprovider;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.polarion.svnimporter.common.Exec;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.StreamConsumer;
import org.polarion.svnimporter.common.ProviderConfig;
import org.polarion.svnimporter.common.ISvnModel;
import org.polarion.svnimporter.common.Util;
import org.polarion.svnimporter.cvsprovider.internal.RlogParser;
import org.polarion.svnimporter.cvsprovider.internal.CvsRcsConfig;
import org.polarion.svnimporter.cvsprovider.internal.CvsTransform;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsModel;
import org.polarion.svnimporter.cvsprovider.internal.model.CvsRevision;
import org.polarion.svnimporter.svnprovider.SvnModel;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collection;
import java.util.Iterator;

/**
 * CvsRcsProvider
 *
 * @author Fedor Zhigaltsov
 * @version $Id: CvsRcsProvider.java 27966 2009-11-15 21:56:31Z kencorbin $
 */
public class CvsRcsProvider extends CvsProvider {
    private static final Log LOG = Log.getLog(CvsRcsProvider.class);
    private Map path2rcsfile = new HashMap();
    private CvsRcsConfig config;

    public void configure(Properties properties) {
        config = new CvsRcsConfig(properties);
    }

    public ProviderConfig getConfig() {
        return config;
    }

    public boolean validateConfig() {
        return config.validate();
    }

    public void logEnvironmentInformation() {
        config.logEnvironmentInformation();
    }

    public void listFiles(PrintStream out) {
        Collection files = buildCvsModel().getFiles().keySet();
        for (Iterator i = files.iterator(); i.hasNext();)
            out.println(i.next());
    }

    public ISvnModel buildSvnModel() {
        CvsModel cvsModel = buildCvsModel();
        SvnModel svnModel = new CvsTransform(this).transform(cvsModel);
        LOG.info("Svn model has been created");
        LOG.info("total number of revisions in svn model: " + svnModel.getRevisions().size());
        return svnModel;
    }

    public CvsModel buildCvsModel() {
        File repo = new File(config.getRepositoryPath());
        CvsModel cvsModel = new CvsModel();
        path2rcsfile.clear();
        processRepoDirectory(repo, "", cvsModel);
        cvsModel.finishModel();
        cvsModel.printSummary();
        return cvsModel;
    }

    private void processRepoDirectory(File rcsDir, String pathName, CvsModel model) throws CvsException {
        LOG.debug("Process RCS repo directory: " + rcsDir.getPath() + " (" + pathName + ")");
        File[] files = rcsDir.listFiles();
        if (files == null || files.length == 0) {
            LOG.warn("No files in RCS directory: " + rcsDir.getPath());
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(",v")) {
                String filename = file.getName().replaceAll(",v$", "");
                if (!"".equals(pathName))
                    filename = pathName + "/" + filename;
                processRcsFile(file, filename, model);
            } else if (file.isDirectory()) {
                if ("Attic".equals(file.getName())) {
                    processRepoDirectory(file, pathName, model);
                } else {
                    String dirName = file.getName();
                    if (!"".equals(pathName))
                        dirName = pathName + "/" + dirName;
                    processRepoDirectory(file, dirName, model);
                }
            }
        }
    }

    private void processRcsFile(File rcsFile, String pathName, CvsModel model) throws CvsException {
        LOG.debug("Process RCS file: " + rcsFile.getPath() + " (" + pathName + ")");
        LogInformation[] infos = getLogInformation(rcsFile);
        if (infos.length == 0 || infos.length > 1) {
            LOG.warn("Wrong RCS file: " + rcsFile.getAbsolutePath());
            return;
        }
        path2rcsfile.put(pathName, rcsFile);
        model.addLogInfo(pathName, infos[0]);
    }

    private LogInformation[] getLogInformation(File rcsFile) {
        Exec exec = new Exec(new String[]{config.getRlogCommand(), "-x,v", rcsFile.getAbsolutePath()});
        exec.setVerboseExec(true);
        final RlogParser parser = new RlogParser();
        exec.setStdoutConsumer(new StreamConsumer() {
            public void consumeLine(String line) {
                parser.parseLine(line);
            }
        });
        exec.exec();
        Exception e = exec.getException();
        if(e != null) {
        	LOG.error("Execution of rlog command failed.",e);
        }
        return parser.getLogInfos();
    }

    public File checkout(CvsRevision revision) {
        File tempDir = config.getTempDir();

        String path = revision.getModelFile().getPath();
        File rcsFile = (File) path2rcsfile.get(path);
        File tempFile = new File(tempDir, rcsFile.getName().replaceAll(",v$", ""));
        if (tempFile.exists()) tempFile.delete();

        Exec exec = new Exec(new String[]{
                config.getCoCommand(),"-x,v",
                "-r" + revision.getNumber(),
                rcsFile.getAbsolutePath()});
        exec.setWorkdir(tempDir);
        exec.setVerboseExec(true);
        exec.exec();
        return tempFile;
    }

    public void cleanup() {
        LOG.debug("cleanup");
        File tempDir = config.getTempDir();
        if (!Util.delete(tempDir)) {
            LOG.error("can't delete temp dir: " + tempDir.getAbsolutePath());
        }
    }
}
