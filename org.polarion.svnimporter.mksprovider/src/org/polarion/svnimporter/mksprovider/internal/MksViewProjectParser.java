package org.polarion.svnimporter.mksprovider.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.polarion.svnimporter.common.Log;
import org.polarion.svnimporter.mksprovider.MksException;
import org.polarion.svnimporter.mksprovider.MksProvider;
import org.polarion.svnimporter.mksprovider.internal.model.MksFile;
import org.polarion.svnimporter.mksprovider.internal.model.MksModel;
import org.polarion.svnimporter.mksprovider.internal.model.MksRevision;
import org.polarion.svnimporter.mksprovider.internal.model.MksSubproject;

/** This is the base class for parse classes that process different
 * form of the viewproject query
 */
public abstract class MksViewProjectParser {
    private static final Log LOG = Log.getLog(MksViewProjectParser.class);
    private MksConfig config;

    public MksViewProjectParser(MksConfig config) {
        this.config = config;
    }

    /**
     * Process all project members on the main development path
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    public void parse(MksProvider provider, MksModel model) {
        parse(provider, model, 
              new MksSubproject(config.getProjectDir(), config.getProjectFilename()));
    }
    
    /**
     * Process all members on a specific development path
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     * @param devPath name of development path
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    public void parseDevPath(MksProvider provider, MksModel model, String devPath) {
        parse(provider, model, 
              new MksSubproject(config.getProjectDir(), config.getProjectFilename(), 
                                devPath, null));
    }
    
    /**
     * Process all members for a specific project revision (checkpoint)
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     * @param projRevNumber project revision number
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    public void parseCheckpoint(MksProvider provider, MksModel model, String projRevNumber) {
        parse(provider, model, 
              new MksSubproject(config.getProjectDir(), config.getProjectFilename(), 
                                null, projRevNumber));
    }

    /**
     * Run an viewproject command and parse the resulting output file, adding the
     * results to an MksModel object 
     * @param provider The parent MksProvider object
     * @param model MksModel object to be updated
     * @param project project with qualifiers, to searched
     * @param Map mapping alternate dev paths to their root checkpoint
     */
    private void parse(MksProvider provider, MksModel model, MksSubproject project) {
        LOG.debug("Processing Project View of " + project);
        List<String> execParms = new ArrayList<String>();
        execParms.add(config.getExecutable());
        execParms.add("viewproject");
        execParms.add("--batch");
        execParms.add("-R");
        project.addCmdList(execParms);
        execParms.add("--fields=indent,type,memberrev,name");
        
        File vpFile = new File(config.getTempDir(), "viewproject.txt");
        BufferedReader reader = null;
        try {
            MksExec exec = new MksExec(execParms);
            reader = new BufferedReader(new InputStreamReader(
                    provider.executeCommand(exec, vpFile), config.getLogEncoding()));
            
            // We need to maintain a list of subprojects for each indent level
            // The first will be our (possibly qualified) main project
            List<MksSubproject> indentPrefix = new ArrayList<MksSubproject>();
            indentPrefix.add(project);
            
            int skipIndent = -1;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                VpFields fields = parseLine(line);
                
                // If we are trying to continue processing after failing
                // to build a subproject at indent level skipIndent
                // ignore everything until we find an item with an indent level
                // that indicates it is not a component of the failed project
                // Once we find that component, reset the skip and continue on
                if (skipIndent >= 0) {
                    if (fields.indent > skipIndent) continue;
                    skipIndent = -1;
                }
                
                // Start by adding the appropriate prefix to the member name
                MksSubproject subproj = (MksSubproject)indentPrefix.get(fields.indent);
                
                // Is this some kind of subproject
                if (fields.type.endsWith("subproject")) {
                    
                    // If so then the first thing we need to do use the name
                    // to initialize the next indent level project
                    try {
                      MksSubproject nextProject = parseSubproject(provider, subproj, fields);
                      if (fields.indent+1 >= indentPrefix.size()) {
                          indentPrefix.add(nextProject);
                      } else {
                          indentPrefix.set(fields.indent+1, nextProject);
                      }
                    } 
                    
                    // If an exception gets thrown while parsing the subproject
                    // normally we abort processing.  But to help some debugging
                    // cases, if the MKS exec continue flag is set, we will
                    // log the error and carry no as best we can.  But we have
                    // to set skipIndent to ignore any parsed member that belong
                    // to the subproject we could not build.
                    catch (MksException ex) {
                        if (!provider.getConfig().isMksExecContinue()) throw ex;
                        LOG.error(ex.getMessage());
                        ex.printStackTrace();
                        skipIndent = fields.indent;
                    }
                }
                
                // If this is an ordinary member, invoke the processMember method
                // to process it
                else if (fields.type.equals("archived")) {
                    fields.name = subproj.getProjectDir() + fields.name;
                    processMember(provider, model, fields.name, fields.revision, subproj);
                }
                
                // Ignore non-archived entries with a warning
                // These were observed on old project checkpoints that had been
                // garbled by a previous migration
                else if (fields.type.equals("non-archived")) {
                	LOG.warn("Skipping non-archived element " + subproj.getProjectDir() + fields.name);
                }
                
                else {
                    
                    // Otherwise we have no idea what to do with this
                    throw new MksException("Unknown project review type:" + fields.type);
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {}
            }
            vpFile.delete();
        }
    }
    
    /**
     * Convert subproject line to a subproject object
     * @parent provider MksProvider
     * @parent parent subproject
     * @param fields fields parsed from subproject line
     * @return MksSubproject object
     */
    private MksSubproject parseSubproject(MksProvider provider, 
                                          MksSubproject parent, VpFields fields) {
        
        // subproject names may have a descriptor containing the variant path
        // name or project revision number that first needs to be stripped off
        String name = fields.name;
        String dscr = "";
        int index = name.lastIndexOf(" (");
        if (index >= 0) {
            dscr = name.substring(index+2,name.length()-1);
            name = name.substring(0,index);
        }
        
        // Then check the type to see if this is a build or variant project
        // and build the appropriate subproject object
        MksSubproject newProject;
        if (fields.type.indexOf("variant") >= 0) {
            newProject = new MksSubproject(parent, name, dscr, null);
        } else if (fields.type.indexOf("build") >= 0) {
            newProject = new MksSubproject(parent, name, null, dscr);
        } else {
            newProject = new MksSubproject(parent, name, null, null);
        }
        
        // If this is a shared project, get the absolute path to the real
        // project and save it in the subproject object
        if (fields.type.indexOf("shared") >= 0) {
            adjustProject(provider, parent, newProject);
        }
        
        // Return the new subproject
        return newProject;
    }
    
    /**
     * Adjust the real project path for a shared subproject to point to 
     * the "real" location of the subproject
     * @param provider MksProvider
     * @param parent parent subproject
     * @param project current subproject
     */
    private void adjustProject(MksProvider provider, MksSubproject parent,  MksSubproject project) {
    
        // Executing a projectinfo command to get information about the
        // real project
        List<String> execParms = new ArrayList<String>();
        execParms.add(config.getExecutable());
        execParms.add("projectinfo");
        project.addCmdList(execParms);
        
        // MKS isn't very consistent about reporting the real project link.
        // For normal projects it reports "Project Name:" with the link and
        // "Shared From:" with the real location.  But for build and variant
        // projects it only real project location as the project name. 
        File vpFile = new File(config.getTempDir(), "adjustshared.txt");
        BufferedReader reader = null;
        try {
            MksExec exec = new MksExec(execParms);
            reader = new BufferedReader(new InputStreamReader(
                                            provider.executeCommand(exec, vpFile), 
                                            config.getLogEncoding()));
            
            String realProjectPath = null;
            String[] tags = new String[]{"Shared From:", "Build Project Name:", 
                                         "Variant Project Name:", "Project Name:"};
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                
                for (int ndx = 0; ndx < tags.length; ndx++) {
                    String tag = tags[ndx];
                    if (line.startsWith(tag)) {
                        realProjectPath = line.substring(tag.length()).trim(); 
                    }
                }
            }
            if (realProjectPath == null) {
                throw new MksException(
                        "Could not identify real project path for shared project: " + 
                        project);
            }
            project.setRealPath(realProjectPath);
            
            LOG.debug("Real project path for " + project.getProjectPath() +
                      " set to " + realProjectPath);
        } 
        catch (RuntimeException ex) {
            throw ex;
        } 
        catch (Exception ex) {
            throw new MksException(ex.getMessage(), ex);
        } 
        finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ex) {}
            }
            vpFile.delete();
        }
    }

    private void processMember(MksProvider provider, MksModel model,
                               String name, String revNumber,
                               MksSubproject project) {
        
        // First try to find the file in the current model
        // If it isn't there (which means it didn't exist on the main devpath
        // look up with the current project qualifiers
        MksFile file = (MksFile)model.getFiles().get(name);
        if (file == null) {
            file = new MksRlogParser(provider.getConfig()).parse(provider, model, project, name);
            if (file == null) {
                throw new MksException("Could not find archive for " + name);
            }
        }
        
        // Set the access project for this file, if we haven't found one by now
        file.setProject(project);
        
        // Next look up the specific revision of this file
        MksRevision revision = file.getSourceRevision(revNumber);
        if (revision == null) {
            LOG.error(project + " Revision " + name + ":" + revNumber +
                      " could not be found");
        }
        
        // That's as far as we can go, no call an abstract method to
        // handle the revision
        else {
            processRevision(provider, model, revision);
        }
    }

    /**
     * Process specific revision 
     * @param provider MksProvider object
     * @param model The model
     * @param revision Revision to be processed
     */
    abstract protected void processRevision(MksProvider provider, MksModel model,
                                            MksRevision revision);

    /**
     * Class containing all of the field information we need from a viewproject
     * output line
     */
    private static class VpFields {
        public int indent;      // indent count
        public String type;     // type field
        public String revision; // revision number if present
        public String name;     // name
        
        public String toString() {
            return "indent:" + indent + "  type:" + type + 
                   "  revision:" + revision + "  name:" + name;
        }
    }
    
    private VpFields parseLine(String line) {
        VpFields result = new VpFields();
        
        int stp = 0;
        while (stp < line.length() && line.charAt(stp) == ' ') stp++;
        result.indent = stp / 2;
        
        int endp = line.indexOf(' ', stp);
        result.type = line.substring(stp, endp);
        
        stp = endp+1;
        endp = line.indexOf(' ', stp);
        result.revision = line.substring(stp, endp);
        
        stp = endp+1;
        result.name = line.substring(stp);
        
        LOG.debug("projectview parse result " + result);
        return result;
    }
}
