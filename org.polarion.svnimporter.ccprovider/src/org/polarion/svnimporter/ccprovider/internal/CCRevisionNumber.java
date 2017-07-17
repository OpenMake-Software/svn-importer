package org.polarion.svnimporter.ccprovider.internal;

import org.polarion.svnimporter.common.Log;

import java.util.StringTokenizer;

/**
 * CCRevisionNumber
 *
 * @author Fedor Zhigaltsov
 * @since 16.11.2005
 */
public class CCRevisionNumber {
    private static final Log LOG = Log.getLog(CCRevisionNumber.class);
    /**
     * Parse clearcase revision
     *
     * @param clearcase revision: string like "/main/1" or "\main\1"
     * @return array of strings like {"main", "1"} or null if revision has wrong format
     */
    public static String[] parseRevision(String revision) {
        if (revision == null || revision.length() == 0) {
            LOG.error("Wrong revision format: empty revision " + revision);
            return null;
        }
        char revisionSeparator = revision.charAt(0);
        // Revision separator can be '\' (on Windows) or '/' (on Solaris).
        // First symbol in revision must be the separator.
        if (revisionSeparator != '\\' && revisionSeparator != '/') {
            LOG.error("Wrong revision format: unknown revision separator " + revision);
            return null;
        }
        StringTokenizer st = new StringTokenizer(revision, "" + revisionSeparator);
        String rev[] = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            rev[i++] = st.nextToken();
        }
        if (rev.length < 2 /*|| rev.length > 3*/) {
            LOG.error("Wrong revision format: " + revision);
            return null;
        }
        return rev;
    }

    /**
     * Return numeral number in branch
     *
     * @param revision
     * @return
     */
    public static int getNumberInBranch(String[] parsedRevision) {
        String number = parsedRevision[parsedRevision.length - 1];
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            LOG.error("Wrong revision number: " + number);
            return -1;
        }
    }

    /**
     * Get branch names
     *
     * @param parsedRevision: array of Strings like {"main", "one", "123"}
     * @return array of branch names like {"main", "one"}
     */
    public static String[] getBranchNames(String[] parsedRevision) {
        String[] branchNames = new String[parsedRevision.length - 1];
        for (int i = 0; i < parsedRevision.length - 1; i++) {
            branchNames[i] = parsedRevision[i];
        }
        return branchNames;
    }

    public static int compare(String revisionNumber1, String revisionNumber2) {
        String[] n1 = CCRevisionNumber.parseRevision(revisionNumber1);
        if (n1 == null) {
            LOG.error("Wrong revision number: " + revisionNumber1);
            return 0;
        }

        String[] n2 = CCRevisionNumber.parseRevision(revisionNumber2);
        if (n2 == null) {
            LOG.error("Wrong revision number: " + revisionNumber2);
            return 0;
        }
        if (n1.length == n2.length) {
            String[] bn1 = CCRevisionNumber.getBranchNames(n1);
            String[] bn2 = CCRevisionNumber.getBranchNames(n2);
            boolean equalBranches = true;
            for (int i = 0; i < bn1.length; i++) {
                if (!bn1[i].equals(bn2[i])) {
                    equalBranches = false;
                    break;
                }
            }
            if (equalBranches) {
                // if revisions are in the same branch compare numbers in branch
                int nib1 = getNumberInBranch(n1);
                int nib2 = getNumberInBranch(n2);
                return new Integer(nib1).compareTo(new Integer(nib2));
            }
        }

        // we cannot compare revisions in different branches
        LOG.error("We cannot compare revisions in different branches: " + revisionNumber1 + ":" + revisionNumber2, new Exception());
        return new Integer(n1.length).compareTo(new Integer(n2.length));
    }
}
