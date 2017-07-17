package org.polarion.svnimporter.mksprovider.internal.model;

import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.polarion.svnimporter.common.model.Revision;
import org.polarion.svnimporter.mksprovider.internal.MksConfig;

/**
 * This class represents one MKS checkpoint (or project revision)
 */
public class MksCheckpoint extends Revision {

    
    // Labels associated with this checkpoint
    private String[] labels;
    
    private List<String> devPaths = new ArrayList<String>();
    
    private Map<MksFile, MksRevision> revisionMap = new LinkedHashMap<MksFile, MksRevision>();
    
    public MksCheckpoint(String number, String author, Date date,
                         String[] labels, String message) {
        super(number);
        setAuthor(author);
        setDate(date);
        setMessage(message);
        this.labels = labels;
    }
    
    public void addDevPath(String devPath) {
        devPaths.add(devPath);
    }
    
    public void addRevision(MksRevision revision) {
        revisionMap.put((MksFile)revision.getModelFile(), revision);
    }
    
    public String[] getLabels() {
        return labels;
    }
    
    public List<String> getDevPaths() {
        return devPaths;
    }
    
    public Collection<MksRevision> getRevisions() {
        return revisionMap.values();
    }
    
    public MksRevision getRevision(MksFile file) {
        return (MksRevision)revisionMap.get(file);
    }
    
    /**
     * Determine if checkpoint is on an assigned checkpoint branch
     * @return true if checkpoint is on assigned checkpoint branch
     */
    public boolean isAssigned() {
        return ((MksCheckpointBranch)getBranch()).isAssigned();
    }
    
    /**
     * Return list of tags to be generated for this checkpoint
     * @param config Configuration object
     * @return list of tag names to be generated, or null if none
     */
    public String[] getTags(MksConfig config) {
        
        // If this is a trunk only migration, there are no tags
        if (config.isOnlyTrunk()) return null;

        // If there are any labels and configuration says to generate
        // tags from labels, go with them
        if (labels != null && labels.length > 0 && config.isTagLabels()) {
            
            // If we are generated combined number/label tags, build
            // array of results with the number and label name
            if (config.isTagWith()) {
                String[] results = new String[labels.length];
                for (int ndx = 0; ndx < labels.length; ndx++) {
                    results[ndx] = getNumber() + " - " + labels[ndx];
                }
                return results;
            }
            
            // If we are returning both labels and tags, build an array 
            // containing both the checkpoint number and the labels
            else if (config.isTagBoth()) {
                String[] results = new String[labels.length+1];
                results[0] = getNumber();
                System.arraycopy(results, 1, labels, 0, labels.length);
                return results;
            
            // If we aren't including numbers in any form, just return the labels
            } else {
                return labels;
            }
        }
        
        // Otherwise if we are generating tags from numbers, return an
        // array containing only the checkpoint number
        else if (config.isTagNumbers()) {
            return new String[]{getNumber()};
        }
        
        // otherwise return null
        else {
            return null;
        }
    }
}
