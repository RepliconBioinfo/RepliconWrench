package gov.nci.nih.meltzerlab.replicon.utils;

import gov.nci.nih.meltzerlab.utils.Constants;
import gov.nci.nih.meltzerlab.utils.IOUtils;
import gov.nci.nih.meltzerlab.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;


public class CghToTiming {

	private static final int LINES_TO_SKIP = 8;
	private static final int DEFAULT_BIN_WIDTH = 500;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void entry(String[] args) 
	{
		Options options = new Options();
		options.addOption("i", "input", true, "input CGH file");
		options.addOption("b", "bin-width", true, "desired bin width (default = 500)");
		
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine commandLine = parser.parse(options, args);
			if (commandLine.hasOption('h') || commandLine.getOptions().length == 0)
			{
				Utils.printUsageAndExit("CghToTiming", options);
			}
			
			int binWidth;
			if (commandLine.hasOption('b')) {
				binWidth = Integer.parseInt(commandLine.getOptionValue("b"));
			}
			else {
				binWidth = DEFAULT_BIN_WIDTH;
			}
			
			File cghFile = new File(commandLine.getOptionValue('i'));
			
			work(cghFile, binWidth);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void work(File cghFile, int binWidth) {
		BufferedReader reader;
		try {
			if (IOUtils.isGzipped(cghFile))
			{
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cghFile))));
			}
			else
			{
				reader = new BufferedReader (new FileReader(cghFile));
			}

			for (int i = 0; i < LINES_TO_SKIP; i++)
			{
				reader.readLine();
			}

			String line = null;
			String[] columns = null;
			while ((line = reader.readLine()) != null)
			{
				columns = StringUtils.split(line);
				double timing = 0;
				for (int i = 1; i < columns.length; i++)
				{
					timing += ( Double.parseDouble(columns[i]) - Double.parseDouble(columns[i - 1]) ) * (i - 1);
				}
				System.out.print(Integer.parseInt(columns[0]) * binWidth);
				System.out.print(Constants.TAB);
				System.out.println(timing);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
