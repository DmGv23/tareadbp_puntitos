package utec.cli.parser;

import utec.cli.commands.TestCommand;
import utec.cli.common.HelpOption;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import static picocli.CommandLine.Option;

@Command(
        subcommands = {TestCommand.class},
        versionProvider = VersionProvider.class
)
public class CommandLineParser {

    @Mixin
    private HelpOption helpOption;

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "Print version information and exit.")
    private boolean versionHelpRequested;
}
