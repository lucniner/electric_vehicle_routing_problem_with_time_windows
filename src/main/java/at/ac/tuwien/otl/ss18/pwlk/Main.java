package at.ac.tuwien.otl.ss18.pwlk;

import at.ac.tuwien.otl.ss18.pwlk.exceptions.EvrptwInitializeException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    CommandLine cmd = parseArguments(args);
    final Evrptw evrptw;
    try {
      evrptw = Evrptw.build(cmd);
    } catch (EvrptwInitializeException e) {
      logger.error("Could not initialize EVRPTW instance: " + e);
      return;
    }
    evrptw.evrptwRun();

    System.exit(0);
  }

  private static CommandLine parseArguments(String[] args) {
    CommandLine cmd = null;

    Options options = new Options();
    Option help = new Option( "h", "help", false, "print this message" );
    Option input = new Option("t", "timeout", true, "timeout for algorithm in seconds");
    Option file = new Option("f", "file", true, "specify file for input problem");
    Option directory = new Option("d", "directory", true, "load all instances from given directory");

    options.addOption(help);
    options.addOption(input);
    options.addOption(file);
    options.addOption(directory);

    try {
      cmd = new DefaultParser().parse(options, args, false);
    } catch (ParseException e) {
      System.out.println(e.getMessage() + "\n");
      new HelpFormatter().printHelp("java -jar <name>", options, true);
      System.exit(0);
    }

    if (cmd.hasOption("help")) {
      new HelpFormatter().printHelp("java -jar <name>", options, true);
      System.exit(0);
    }

    return cmd;
  }
}
