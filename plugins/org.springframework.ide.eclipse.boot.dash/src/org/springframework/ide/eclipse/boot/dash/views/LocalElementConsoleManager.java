/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("restriction")
public class LocalElementConsoleManager extends BootDashModelConsoleManager {

	@Override
	public void doWriteToConsole(BootDashElement element, String message, LogType type) throws Exception {
		// Ignore message and log type for now.
		// For local apps, console content is generated by the Java process for
		// the project.
	}

	/**
	 * @return IConsole that is associated with the given process. Null if no
	 *         console is found.
	 */
	protected IConsole getConsole(IProcess process, IConsole[] consoles) {

		for (IConsole console : consoles) {

			if (console instanceof ProcessConsole) {
				IProcess consoleProcess = ((ProcessConsole) console).getProcess();

				if (consoleProcess != null && consoleProcess.equals(process)) {
					return console;
				}
			}
		}
		return null;
	}

	@Override
	public void terminateConsole(BootDashElement element) throws Exception {
		// Not supported
	}

	@Override
	public void showConsole(BootDashElement element) throws Exception {
		IConsoleManager manager = eclipseConsoleManager();

		IConsole appConsole = getConsole(element);


		if (appConsole != null) {
			manager.showConsoleView(appConsole);
		} else {
			throw ExceptionUtil.coreException("Failed to open console for: " + element.getName()
					+ ". Either a process console may not exist or the application is not running.");
		}
	}

	private IConsole getConsole(BootDashElement element) {
		IConsoleManager manager = eclipseConsoleManager();
		IConsole[] activeConsoles = manager.getConsoles();
		if (activeConsoles != null) {
			ImmutableSet<ILaunchConfiguration> launchConfs = element.getLaunchConfigs();
			for (ILaunch launch : BootLaunchUtils.getLaunches(launchConfs)) {
				IProcess[] processes = launch.getProcesses();
				if (processes != null) {
					for (IProcess process : processes) {
						IConsole console = getConsole(process, activeConsoles);
						if (console!=null) {
							return console;
						}
					}
				}
			}
		}
		return null;
	}

	protected IConsoleManager eclipseConsoleManager() {
		return ConsolePlugin.getDefault().getConsoleManager();
	}

	@Override
	public boolean hasConsole(BootDashElement element) {
		return getConsole(element) != null;
	}

	@Override
	public void resetConsole(BootDashElement element) {
		// Not supported
	}

	@Override
	public void reconnect(BootDashElement element) throws Exception {
		// Not supported
	}
}
