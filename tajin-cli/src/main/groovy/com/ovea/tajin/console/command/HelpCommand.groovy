package com.ovea.tajin.console.command

import com.beust.jcommander.JCommander
import com.ovea.tajin.console.Executor

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class HelpCommand implements Executor {

    JCommander commander

    public HelpCommand(JCommander commander) {
        this.commander = commander
    }

    void execute() {
        commander.usage()
    }
}
