package org.polarion.svnimporter.mksprovider.internal.model;

import java.util.SortedSet;
import java.util.TreeSet;

import org.polarion.svnimporter.common.model.Branch;

public class MksCheckpointBranch extends Branch {
    
    // Note: checkpoint history is sorted in reverse order within branch
    private SortedSet<MksCheckpoint> revisions = new TreeSet<MksCheckpoint>(MksRevisionComparator.CHECKPOINT_REV_INSTANCE);

    public MksCheckpointBranch(String number) {
        super(number);
    }

    public SortedSet<MksCheckpoint> getRevisions() {
        return revisions;
    }
    
    /**
     * Determine if this branch has been assigned to either the main trunk
     * or an alternate development path name
     * @return true if it has been assigned
     */
    public boolean isAssigned() {
        return (isTrunk() || getName() != null);
    }


}
