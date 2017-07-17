package org.polarion.svnimporter.mksprovider.internal.model;

import org.polarion.svnimporter.common.model.ModelFile;

public class MksCheckpointHistory extends ModelFile {
    
    public MksCheckpointHistory() {
        super(null);
    }

    /**
     * Find file branch by number
     * @param number branch number
     * @return checkpoint branch if found, null otherwise
     */
    public MksCheckpointBranch getBranch(String number) {
        return (MksCheckpointBranch) getBranches().get(number);
    }
    
    /**
     * Find checkpoint branch by number
     * @param number revision number
     * @return checkpoint object if found, null otherwise
     */
    public MksCheckpoint getRevision(String number) {
        return (MksCheckpoint) getRevisions().get(number);
    }

    /**
     * Cleanup checkpoint history by removing any checkpoints that were not
     * part of any currently active development path
     */
    public void cleanup() {
        for (java.util.Iterator ii = getRevisions().values().iterator(); ii.hasNext(); ) {
            MksCheckpoint checkpoint = (MksCheckpoint)ii.next();
            if (checkpoint.getRevisions().size() == 0) {
                checkpoint.getBranch().deleteRevision(checkpoint);
                ii.remove();
            }
        }
    }

}
