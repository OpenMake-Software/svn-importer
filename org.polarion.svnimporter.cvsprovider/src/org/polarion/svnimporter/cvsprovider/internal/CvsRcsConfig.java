package org.polarion.svnimporter.cvsprovider.internal;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.common.ProviderConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * CvsRcsConfig
 *
 * @author Fedor Zhigaltsov
 * @version $Id: CvsRcsConfig.java 27966 2009-11-15 21:56:31Z kencorbin $
 */
public class CvsRcsConfig extends ProviderConfig implements ICvsConf {
    private static final Log LOG = Log.getLog(CvsRcsConfig.class);

    private static final String CO_COMMAND = "co";
    private static final String RLOG_COMMAND = "rlog";

    private String repositoryPath;
    private File tempDir;
    private String logDateFormatString;
    private String rlogCommand;
    private String coCommand;


    public CvsRcsConfig(Properties properties) {
        super(properties);
    }

    protected void configure() {
        super.configure();
        repositoryPath = getStringProperty("cvsrcs.repository_path", true);
        logDateFormatString = getStringProperty("cvsrcs.logdateformat", false);
        SimpleDateFormat logDateFormat = getDateFormat(logDateFormatString, null);
        if (logDateFormat != null) {
            LogInformation.DATE_FORMAT = logDateFormat;
        }
        tempDir = getTempDir("cvsrcs.tempdir");

        rlogCommand = getStringProperty("cvsrcs.rlog_command", false);
        if (rlogCommand == null) rlogCommand = RLOG_COMMAND;

        coCommand = getStringProperty("cvsrcs.co_command", false);
        if (coCommand == null) coCommand = CO_COMMAND;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public File getTempDir() {
        return tempDir;
    }

    protected void printError(String error) {
        LOG.error(error);
    }

    public String getRlogCommand() {
        return rlogCommand;
    }

    public String getCoCommand() {
        return coCommand;
    }

    public void logEnvironmentInformation() {
        LOG.info("*** CVSRCS provider configuration ***");
        LOG.info("repository path = \"" + repositoryPath + "\"");
        LOG.info("temp dir = \"" + tempDir.getAbsolutePath() + "\"");
        LOG.info("log date format = \"" + logDateFormatString + "\"");
        LOG.info("rlog command = \"" + rlogCommand + "\"");
        LOG.info("co command = \"" + coCommand + "\"");
        super.logEnvironmentInformation();
    }
}
