package com.ovea.tajin.console

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.ovea.tajin.console.command.CreateProjectCommand
import com.ovea.tajin.console.command.HelpCommand
import com.ovea.tajin.console.command.ListProjectCommand
import com.ovea.tajin.console.command.StartProjectCommand

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class TajinApplication {

    static void main(String... args) {
        Map<String, Executor> commands = new HashMap<>()
        JCommander commander = new JCommander()

        commands.put('help', new HelpCommand(commander));
        commands.put('list', new ListProjectCommand());
        commands.put('start', new StartProjectCommand());
        commands.put('create', new CreateProjectCommand());

        for (Map.Entry<String, Executor> entry : commands.entrySet()) {
            commander.addCommand(entry.getKey(), entry.getValue())
        }

        try {
            commander.parse(args)
            commands.get(commander.getParsedCommand()).execute()
        } catch (ParameterException ignored) {
            commander.usage()
            System.exit(1)
        }
    }

}
