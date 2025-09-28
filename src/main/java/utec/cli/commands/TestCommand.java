package utec.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import utec.apitester.Main;
import utec.cli.common.HelpOption;

@Command(name = "test", description = "Tests the API")
public class TestCommand implements Runnable {
    @Mixin
    private HelpOption helpOption;

    @Option(names = "-u", description = "url to api (e.g. localhost:8080)", defaultValue = "localhost:8080")
    private String url;

    @Option(names = "-s", description = "stepped execution", type = Boolean.class, defaultValue = "false")
    private Boolean stepped;

    @Option(names = "-n", description = "include nice-to-have tests", type = Boolean.class, defaultValue = "false")
    private Boolean includeNiceToHave;

    @Override
    public void run() {
        System.out.printf("""
                                  Welcome to the Week07 API Tester!
                                  
                                  You have selected the following options:
                                  -u: url: %s
                                  -s: stepped: %s
                                  -n: includeNiceToHave: %s
                                  
                                  """, url, stepped, includeNiceToHave
        );

        try {
            var main = new Main(url, stepped, includeNiceToHave);
            main.start();
        } catch (Exception ex) {
            System.err.println("ERROR " + ex.getMessage());
        }

        System.out.println("Finished");
    }
}
