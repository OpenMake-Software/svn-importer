/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.lib.cvsclient.command.log;

import org.netbeans.lib.cvsclient.ClientServices;
import org.netbeans.lib.cvsclient.LsCommandNotSupported;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.lib.cvsclient.request.ArgumentRequest;
import org.netbeans.lib.cvsclient.request.CommandRequest;
import org.netbeans.lib.cvsclient.request.ExpandModulesRequest;
import org.netbeans.lib.cvsclient.request.RootRequest;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * "Ls" command
 *
 * @author Fedor Gigaltsov
 */
public class LsCommand extends BasicCommand {


	/**
	 * The modules to checkout. These names are unexpanded and will be passed
	 * to a module-expansion request.
	 */
	private final List modules = new LinkedList();

	/**
	 * The expanded modules.
	 */
	private final List expandedModules = new LinkedList();

	public LsCommand() {
		resetCVSCommand();
	}

	/**
	 * Set the modules to export.
	 *
	 * @param module the name of the module to export
	 */
	public void setModule(String module) {
		modules.add(module);
	}

	/**
	 * clears the list of modules for export.
	 */

	public void clearModules() {
		this.modules.clear();
	}

	/**
	 * Set the modules to export.
	 *
	 * @param modules the names of the modules to export
	 */
	public void setModules(String[] modules) {
		clearModules();
		if (modules == null) {
			return;
		}
		for (int i = 0; i < modules.length; i++) {
			String module = modules[i];
			this.modules.add(module);
		}
	}

	public String[] getModules() {
		String[] mods = new String[modules.size()];
		mods = (String[]) modules.toArray(mods);
		return mods;
	}

	/**
	 * Execute this command.
	 *
	 * @param client the client services object that provides any necessary
	 *               services to this command, including the ability to actually process
	 *               all the requests
	 */
	public void execute(ClientServices client, EventManager em)
			throws CommandException, AuthenticationException {

		client.ensureConnection();

		requests = new LinkedList();
		if (client.isFirstCommand()) {
			requests.add(new RootRequest(client.getRepository()));
		}
		for (Iterator it = modules.iterator(); it.hasNext();) {
			String module = (String) it.next();
			requests.add(new ArgumentRequest(module));
		}
		expandedModules.clear();
		requests.add(new ExpandModulesRequest());
		try {
			client.processRequests(requests);
		} catch (CommandException ex) {
			throw ex;
		} catch (LsCommandNotSupported ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CommandException(ex, ex.getLocalizedMessage());
		}
		requests.clear();
		postExpansionExecute(client, em);
	}

	/**
	 * This is called when the server has responded to an expand-modules
	 * request.
	 */
	public void moduleExpanded(ModuleExpansionEvent e) {
		expandedModules.add(e.getModule());
	}

	/**
	 * Execute this command
	 *
	 * @param client the client services object that provides any necessary
	 *               services to this command, including the ability to actually process
	 *               all the requests
	 */
	private void postExpansionExecute(ClientServices client, EventManager em)
			throws CommandException, AuthenticationException {

		super.execute(client, em);

		for (Iterator it = modules.iterator(); it.hasNext();) {
			String module = (String) it.next();
			requests.add(new ArgumentRequest(module));
		}

		requests.add(CommandRequest.LS);

		try {
			client.processRequests(requests);
			requests.clear();

		} catch (CommandException ex) {
			throw ex;
		} catch (LsCommandNotSupported ex) {
			throw ex;
		} catch (Exception ex) {
			throw new CommandException(ex, ex.getLocalizedMessage());
		}
	}

	public String getCVSCommand() {
		StringBuffer toReturn = new StringBuffer("ls "); //NOI18N
		toReturn.append(getCVSArguments());
		if (modules != null && modules.size() > 0) {
			for (Iterator it = modules.iterator(); it.hasNext();) {
				String module = (String) it.next();
				toReturn.append(module);
				toReturn.append(' ');
			}
		} else {
			String localizedMsg = CommandException.getLocalMessage("ExportCommand.moduleEmpty.text"); //NOI18N
			toReturn.append(" "); //NOI18N
			toReturn.append(localizedMsg);
		}
		return toReturn.toString();
	}

	public String getCVSArguments() {
		StringBuffer toReturn = new StringBuffer(""); //NOI18N
		return toReturn.toString();
	}

	public boolean setCVSCommand(char opt, String optArg) {
		return true;
	}

	public void resetCVSCommand() {
		setRecursive(true);
	}

	/**
	 * String returned by this method defines which options are available for this particular command
	 */
	public String getOptString() {
		return "";
	}


	/**
	 * Create a builder for this command.
	 *
	 * @param eventMan the event manager used to receive events.
	 */
	public Builder createBuilder(EventManager eventMan) {
		return new LsBuilder(eventMan, this);
	}
}
