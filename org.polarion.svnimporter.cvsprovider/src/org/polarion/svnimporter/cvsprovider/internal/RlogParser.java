package org.polarion.svnimporter.cvsprovider.internal;

import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.log.LogBuilder;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.event.EventManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * RlogParser
 *
 * @author Fedor Zhigaltsov
 * @version $Id: RlogParser.java 5635 2006-07-04 10:19:03Z GigaltsovF $
 */
public class RlogParser {
    private Vector logInfos;
    private LogBuilder logBuilder;

    public RlogParser() {
        logInfos = new Vector();
        logBuilder = createLogBuilder();
    }

    private LogBuilder createLogBuilder() {
        EventManager eventManager = new EventManager();
        eventManager.addCVSListener(new CvsLogListener() {
            public void addLogInfo(LogInformation logInformation) {
                logInfos.add(logInformation);
            }
        });
        return new CustomizedLogBuilder(eventManager, new RlogCommand());
    }

    public void parse(File rlogfile) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(rlogfile));
        try {
            String s;
            while ((s = r.readLine()) != null) {
                parseLine(s);
            }
        } finally {
            try {
                r.close();
            } catch (IOException e) {
            }
        }
    }

    public void parseLine(String line) {
        logBuilder.parseLine(line, false);
    }

    public LogInformation[] getLogInfos() {
        return (LogInformation[]) logInfos.toArray(new LogInformation[0]);
    }

    private class CustomizedLogBuilder extends LogBuilder {
        public CustomizedLogBuilder(EventManager eventMan, BasicCommand command) {
            super(eventMan, command);
        }

        protected File createFile(String fileName) {
            return null;
        }
    }
}
