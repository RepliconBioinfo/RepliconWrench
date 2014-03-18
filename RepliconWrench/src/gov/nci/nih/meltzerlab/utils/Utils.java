package gov.nci.nih.meltzerlab.utils;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class Utils 
{
	public static void printMessageAndExit(String message)
	{
		System.out.println(message);
		System.exit(0);
	}
	
	/**
	 * Prints a helpful message and exits
	 * @param message the message that needs to be printed (more helpful the better)
	 * @param options
	 */
	public static void printUsageAndExit(String message, Options options) 
	{
		HelpFormatter help = new HelpFormatter();
		help.setWidth(80);
		help.printHelp(message, options);
		System.exit(0);
	}

}
