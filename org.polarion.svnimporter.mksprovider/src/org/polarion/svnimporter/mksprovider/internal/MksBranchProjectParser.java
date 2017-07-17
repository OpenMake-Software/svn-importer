package org.polarion.svnimporter.mksprovider.internal;

import java.util.Date;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.mksprovider.MksException;
import org.polarion.svnimporter.mksprovider.MksProvider;
import org.polarion.svnimporter.mksprovider.internal.model.MksBranch;
import org.polarion.svnimporter.mksprovider.internal.model.MksCheckpoint;
import org.polarion.svnimporter.mksprovider.internal.model.MksFile;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevisionState;

/**
 * This class sets up branches for the main development path and for
 * any alternate development paths
 */
public class MksBranchProjectParser extends MksViewProjectParser {
    
    private static final Log LOG = Log.getLog(MksBranchProjectParser.class);
    
    private String devPath;
    private MksCheckpoint rootCheckpoint;
    private MksCheckpoint checkpoint;
    private Date nextCheckpointDate;
    
    private Date lastCommitDate = null;
    private MksRevision lastCommitRevision = null;
    
    public MksBranchProjectParser(MksConfig config) {
        super(config);
    }
    

    /**
     * Set up model branch info
     * @param provider MksProvider object
     * @param model model
     * @param devPathP alternate development path name (or null for main path)
     * @param rootCheckpointP root checkpoint for alternate dev path
     * (or null for main path)
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    public void parse(MksProvider provider, MksModel model, 
                      String devPathP, MksCheckpoint rootCheckpointP) {
        parse(provider, model, devPathP, rootCheckpointP, null, null);
    }

    /**
     * Set up model branch info
     * @param provider MksProvider object
     * @param model model
     * @param devPathP alternate development path name (or null for main path)
     * @param rootCheckpointP root checkpoint for alternate dev path
     * (or null for main path)
     * @param checkpointP specific checkpoint being scanned or null to scan
     * development path endpoint
     * @param nextCheckpointDate date of next checkpoint.  This date is used
     * as the deleting date when we discover a file has been deleted sometime
     * between this checkpoint and the next one.
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    public void parse(MksProvider provider, MksModel model, 
                      String devPathP, MksCheckpoint rootCheckpointP,
                      MksCheckpoint checkpointP, Date nextCheckpointDateP) {
        LOG.debug("branch project view for path:" + devPathP +
                  " root checkpoint:" + (rootCheckpointP == null ? null : rootCheckpointP.getNumber()) +
                  " checkpoint:" + (checkpointP == null ? null : checkpointP.getNumber()));
        this.devPath = devPathP;
        this.rootCheckpoint = rootCheckpointP;
        this.checkpoint = checkpointP;
        this.nextCheckpointDate = nextCheckpointDateP;
        this.lastCommitDate = null;
        if (checkpoint == null) {
            this.parseDevPath(provider, model, devPath);
        } else {
            this.parseCheckpoint(provider, model, checkpoint.getNumber());
        }
    }

    /**
     * Process revision for this project view
     * @param provider MksProvider object
     * @model model
     * @revision revision to be processed
     */
    protected void processRevision(MksProvider provider, MksModel model,
                                   MksRevision revision) {
        
        MksFile file = (MksFile)revision.getModelFile();
        LOG.debug("Processing revision " + file.getPath() +
                  ":" + revision.getNumber());
        
        // Track the most recent commit for this view
        if (lastCommitDate == null || lastCommitDate.before(revision.getDate())) {
            lastCommitDate = revision.getDate();
            lastCommitRevision = revision;
        }
        
        // If we are processing a checkpoint, add this revision to that checkpoint
        if (checkpoint != null) checkpoint.addRevision(revision);
        
        // Get the branch associated with this revision
        MksBranch branch = (MksBranch)revision.getBranch();
        
        // See if we have already assigned a branch for this development path
        MksBranch pathBranch = file.getBranchForDevPath(devPath);
        
        // If a branch has already been assigned for this path and file, then
        // nothing more needs to be done.  But we will show a warning if 
        // this revision is not on the previously assigned branch or the root
        // of the previously assigned branch.
        if (pathBranch != null) {
            if (pathBranch != branch && revision != pathBranch.getSproutRevision()) {
                LOG.warn("Revision " + file.getPath() + ":" + revision.getNumber() +
                         " is not on expected branch " + pathBranch.getNumber()); 
            }
            return;
        }
        
        // Otherwise, See if the branch has already been assigned to a main or 
        // an alternate development path
        if (! branch.isAssigned()) {
            
            // If not, assign to the whatever path we are processing now
            // but first we may have to adjust the branch structure so
            // that this branch sprouts from a branch that has been 
            // assigned to a development path
            branch = branch.adjustRoot();
            
            LOG.debug("Assigning branch " + branch.getNumber() + " to " + 
                      (devPath == null ? "trunk" : devPath + " path"));
            file.setBranchDevPath(branch, devPath);
        }
        
        // Otherwise we need to set up a new empty branch rooted at this revision
        else {
            
            LOG.debug("Creating stub branch from " + revision.getNumber() + " for " + 
                      (devPath == null ? "trunk" : devPath + " path"));
            
            // Can't be done for main development path
            if (devPath == null) {
                throw new MksException("Branch " + branch.getNumber() +
                                       " has already been assigned to " + branch.getName());
            }
            
            // make up a reasonable looking branch number.
            // make sure that it hasn't been used before and create a new
            // branch with it.
            int branchInt = 10000;
            String branchNumber;
            do {
                branchNumber = revision.getNumber() + "." + branchInt++;
            } while (file.getBranch(branchNumber) != null);
            branch = new MksBranch(branchNumber);
            
            branch.setSproutRevision(revision);
            file.addBranch(branch);
            
            // Set the branch name to this development path
            file.setBranchDevPath(branch, devPath);
            
            // and add to collection of child branches for this revision
            revision.addChildBranch(branch);
        }
        
        // Set the branch source revision to the sprout revision for this
        // branch (which may be null if this is the "1" branch)
        MksRevision source = (MksRevision)branch.getSproutRevision();
        
        // Save the revision we want to use for the last date check that 
        // occurs at the end of this method.
        MksRevision checkRevision = revision;
        
        // If no source revision has been identified, set the state of the first
        // revision in the branch to add
        if (source == null) {
            MksRevision firstRevision = (MksRevision)branch.getRevisions().first();
            LOG.debug("set revision state of " + firstRevision.getNumber() + " to ADD");
            firstRevision.setState(MksRevisionState.ADD);
        } 
        
        // if there is a source revision, we need to create a new initial revision
        // in the branch that copies the source revision.  We will use the author
        // date, and description from the root checkpoint
        else {
            LOG.debug("Copy from source " + source.getModelFile().getPath() +
                      ":" + source.getNumber());
            MksRevision copyRevision = 
                new MksRevision(branch.getNumber() + ".0",
                                rootCheckpoint.getAuthor(),
                                rootCheckpoint.getDate(),
                                rootCheckpoint.getMessage());
            copyRevision.setState(MksRevisionState.COPY);
            copyRevision.setSource(source);
            
            // Make sure that the copy date occurs after the source revision
            // commit date
            if (! copyRevision.getDate().after(source.getDate())) {
                LOG.warn("Sequence error for " + revision.getModelFile().getPath() +
                        ":" + revision.getNumber() +
                        " root checkpoint " + rootCheckpoint.getNumber() +
                        " date " + copyRevision.getDate() + 
                        " is not after source revision " + source.getNumber() +
                        " date " + source.getDate());
                copyRevision.setDate(new Date(source.getDate().getTime()+1));
            }
            
            // Likewise if there is a real revision on this branch, make sure
            // that the copy occurs before that revisions commit date
            if (branch.getRevisions().size() > 0) {
                MksRevision firstRevision = (MksRevision)branch.getRevisions().first();
                if (! firstRevision.getDate().after(copyRevision.getDate())) {
                    LOG.warn("Sequence error for " + revision.getModelFile().getPath() +
                            ":" + revision.getNumber() +
                            " root checkpoint " + rootCheckpoint.getNumber() +
                            " date " + copyRevision.getDate() + 
                            " is not before first revision " + firstRevision.getNumber() +
                            " date " + firstRevision.getDate());
                    copyRevision.setDate(new Date(firstRevision.getDate().getTime()-1));
                }
                
                // And one sanity check to make sure we can get room between these
                // two date limits
                if (! copyRevision.getDate().after(source.getDate())) {
                    throw new MksException(
                            "For file " + revision.getModelFile().getPath() +
                            " the first branch revision " + firstRevision.getNumber() +
                            " occurs before the branch root revision " + source.getNumber());
                }
            }
            
            // If and only if, there were no other revisions on this branch, it
            // must mean that we just created a new branch sprouted from the
            // processed revision.  In this one case, the copy revision should
            // replace the actual revision in subsequent checking
            else {
                checkRevision = copyRevision;
            }
            
            // OK, add the new revision to beginning of branch
            revision.getModelFile().addRevision(copyRevision);
            copyRevision.setBranch(branch);
            branch.addRevision(copyRevision);
        }
        
        // Finally, if we set up this branch during a checkpoint project scan, it
        // means that this file wasn't found in any previous project scans. Which
        // means that it must have been deleted sometime between the last revision
        // on the branch and the next checkpoint date.
        if (checkpoint != null) {
            LOG.debug("deleting revision from branch " + branch.getNumber() +
                      " at " + nextCheckpointDate);
            
            // Make sure the revision we are trying to delete is the last one
            // on this branch
            if (branch.getRevisions().last() != checkRevision) {
                
                // If not, there are two courses of action.  Start by asking
                // the provider which one to choose
                
                if (provider.getConfig().isSplitBranchOnDelete()) {
                    
                    // First option is to split all of the revisions past
                    // this one into a new branch created specifically for this
                    // purpose.  The new branch is a potential trunk branch and
                    // will not be the child of any specific revisions.  If it does
                    // end up getting used, it will look like a new member file
                    int branchInt = 2;
                    String branchNumber;
                    do {
                        branchNumber = Integer.toString(branchInt++);
                    } while (file.getBranch(branchNumber) != null);
                    
                    LOG.debug("Spliting revisions past delete point to branch " + branchNumber);
                    MksBranch newBranch = new MksBranch(branchNumber);
                    file.addBranch(newBranch);
                    
                    while (true) {
                        MksRevision tmpRevision = (MksRevision)branch.getRevisions().last();
                        if (tmpRevision == checkRevision) break;
                        branch.deleteRevision(tmpRevision);
                        newBranch.addRevision(tmpRevision);
                        tmpRevision.setBranch(newBranch);
                    }
                }
                
                else {
                    
                    // Second choice is to move the delete date past the
                    // commit date of the last revision in the branch
                    checkRevision = (MksRevision)branch.getRevisions().last();
                }
            }
            
            // Back to the delete process.
            // First make sure that the next checkpoint date is after the
            // revision date
            Date deleteDate = nextCheckpointDate;
            if (deleteDate == null || ! deleteDate.after(checkRevision.getDate())) {
                LOG.warn("Sequence error for " + checkRevision.getModelFile().getPath() +
                         ":" + checkRevision.getNumber() +
                         " delete checkpoint date " + deleteDate +
                         " is not after revision date " + checkRevision.getDate());
                deleteDate = new Date(checkRevision.getDate().getTime() + 1);
            }
            MksRevision deleteRevision =
                new MksRevision(branch.getNumber() + ".99999",
                                revision.getAuthor(), deleteDate, "");
            deleteRevision.setState(MksRevisionState.DELETE);
            revision.getModelFile().addRevision(deleteRevision);
            deleteRevision.setBranch(branch);
            branch.addRevision(deleteRevision);
        }
    }
    
    /**
     * Return the most recent commit date for all of the files in the most
     * recently processed branch.
     * @return the most recent commit date
     */
    public Date getLastCommitDate() {
        return lastCommitDate;
    }

    /** 
     * return the most recently committed revision in the most recently 
     * processed project view
     * @return last committed revision
     */
    public MksRevision getLastCommitRevision() {
        return lastCommitRevision;
    }
}
