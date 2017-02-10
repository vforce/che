/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.processes;

import org.eclipse.che.api.core.model.machine.Machine;

/**
 *
 */
public class ProcessImpl implements Process {

    private final String  commandName;
    private final String  commandLine;
    private final int     pid;
    private final Machine machine;

    public ProcessImpl(String commandName, String commandLine, int pid, Machine machine) {
        this.commandName = commandName;
        this.commandLine = commandLine;
        this.pid = pid;
        this.machine = machine;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public Machine getMachine() {
        return machine;
    }
}