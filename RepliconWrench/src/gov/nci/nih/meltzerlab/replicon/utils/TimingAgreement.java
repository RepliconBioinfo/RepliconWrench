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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
/**
 * Calculates how closely predicted timing agrees with 
 * established truth
 * @author yev
 *
 */
public class TimingAgreement 
{
	private static File REFERENCE_FILE = null;
	private static File TEST_FILE = null;

	public static void entry(String[] args)
	{

		Options options = new Options();
		options.addOption("t", "test-file", true, "test file");
		options.addOption("r", "reference-file", true, "reference file");
		options.addOption("s", "spearman", false, "do Spearman's correlation, otherwise Pearson's");

		CommandLineParser parser = new PosixParser();
		double[][] profiles = null;
		boolean doSpearman = false;
		try {
			CommandLine commandLine = parser.parse(options, args);
			if (commandLine.getOptions().length == 0 || commandLine.hasOption('h'))
			{
				Utils.printUsageAndExit("TimingAgreement", options);
			}

			TEST_FILE = new File(commandLine.getOptionValue('t'));
			REFERENCE_FILE = new File(commandLine.getOptionValue('r'));
			profiles = getProfiles(REFERENCE_FILE, TEST_FILE);

			if (commandLine.hasOption('s')){
				doSpearman = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		double correlation = 0.0;
		if (doSpearman){
			SpearmansCorrelation spearman = new SpearmansCorrelation();
			correlation = spearman.correlation(profiles[0], profiles[1]);
		}
		else{
			PearsonsCorrelation pearsons = new PearsonsCorrelation();
			correlation = pearsons.correlation(profiles[0], profiles[1]);
		}

		//		double distance = getDistance(profiles);
		System.out.println(shortenFileName(TEST_FILE).replace(Constants.UNDERSCORE, Constants.TAB) 
				+ "\t" + correlation + "\t" + shortenFileName(REFERENCE_FILE));
	}

	/**
	 * Eliminates the file suffix or extension (chops off everything after the first "."
	 * @param file
	 * @return
	 */
	private static String shortenFileName(File file)
	{
		int x = file.getName().indexOf(Constants.DOT);
		return file.getName().substring(0, x);
	}

	/**
	 * Calculates Pearson's correlation between two timing files
	 * @param reference the truth file
	 * @param experiment the experiment file
	 * @param initialDistanceToIgnore will ignore all positions less than or equal to
	 * @return
	 */
	public static double[][] getProfiles(File reference, File experiment, int initialDistanceToIgnore)
	{
		Set<Integer> allowedPositions = getAllowedPostions(reference, initialDistanceToIgnore);
		allowedPositions.retainAll(readPositions(experiment));
		double[] referenceTiming = getTiming(reference, allowedPositions);
		double[] experimentTiming = getTiming(experiment, allowedPositions);

		double[][] result = new double[2][referenceTiming.length];
		result[0] = referenceTiming;
		result[1] = experimentTiming;
		return result;
	}

	public static double[][] getProfiles(File reference, File experiment)
	{
		Set<Integer> allowedPositions = readPositions(reference);
		allowedPositions.retainAll(readPositions(experiment));
		double[] referenceTiming = getTiming(reference, allowedPositions);
		double[] experimentTiming = getTiming(experiment, allowedPositions);

		double[][] result = new double[2][referenceTiming.length];
		result[0] = referenceTiming;
		result[1] = experimentTiming;
		return result;
	}


	/**
	 * Reads the timing information
	 * @param timingFile - this can be a gzipped file
	 * @param allowedPositions a set of allowed positions
	 * @return
	 */
	public static double[] getTiming(File timingFile, Set<Integer> allowedPositions) 
	{
		double[] result = new double[allowedPositions.size()];
		Set<Integer> seenPositions = new HashSet<Integer>();
		BufferedReader reader;
		double d = 0.0;
		try 
		{
			if (IOUtils.isGzipped(timingFile))
			{
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(timingFile))));
			}
			else
			{
				reader = new BufferedReader (new FileReader(timingFile));
			}
			String line = null;
			String[] columns = null;
			int i = 0;
			while ((line = reader.readLine()) != null)
			{
				columns = StringUtils.split(line);
				// Typical line is 500	2.5861246
				if (columns.length > 0)
				{
					d = Double.parseDouble(columns[0]); // has to do with improper string formats
					int position = (int) d;
					if (allowedPositions.contains(position) && ! seenPositions.contains(position))
					{
						result[i] = Double.parseDouble(columns[1]);
						seenPositions.add(position);
						i++;
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		if (seenPositions.size() != allowedPositions.size())
		//		{
		//			LOG.warn("Did not find all positions in file. Out of " + allowedPositions.size() + " found " + seenPositions.size() + " File is " + timingFile.getPath());
		//		}
		return result;
	}

	/**
	 * Read the 'truth / reference' file and get positions
	 * @param reference the file
	 * @param initialDistanceToIgnore will ignore all positions less than or equal to
	 * @return
	 */
	private static Set<Integer> getAllowedPostions(File reference, int initialDistanceToIgnore) 
	{
		Set<Integer> positions = readPositions(reference);
		Set<Integer> result = new HashSet<Integer>();
		for (Integer position : positions)
		{
			if (position > initialDistanceToIgnore)
			{
				result.add(position);
			}
		}

		return result;
	}

	/**
	 * Reads the timing file, not filtering. Witholds all values that have a 0
	 * @param reference
	 * @return
	 */
	public static Set<Integer> readPositions(File reference) 
	{
		Set<Integer> positions = new HashSet<Integer>();
		double d = 0.0;
		try {
			BufferedReader reader;
			if (IOUtils.isGzipped(reference))
			{
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(reference))));
			}
			else
			{
				reader = new BufferedReader (new FileReader(reference));
			}

			String line = null;
			String[] columns = null;
			while ((line = reader.readLine()) != null)
			{
				columns = StringUtils.split(line);
				if (columns.length > 0)
				{
					d = Double.parseDouble(columns[0]); // has to do with improper string formats
					positions.add((int) d);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return positions;
	}

	/**
	 * Returns an intersection of position (positions in common)
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static Set<Integer> getCommonPositions(File f1, File f2)
	{
		Set<Integer> positions = readPositions(f1);
		positions.retainAll(readPositions(f2));
		return positions;
	}

	/**
	 * Returns common position for an arbitrary number of files
	 * @param files
	 * @return
	 */
	public static Set<Integer> getCommonPostions(File ... files)
	{
		Set<Integer> positions = readPositions(files[0]);
		for (int i = 1; i < files.length; i++)
		{
			positions.retainAll(readPositions(files[i]));
		}
		return positions;
	}


	//private static 

	/**
	 * Execute an external command
	 * @param command
	 * @param outFile
	 */
	public static void executeExternalCommand(String command, String outFile)
	{
		try {
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
			String line = null;
			List<String> output = new ArrayList<String>();
			while((line = reader.readLine()) != null) 
			{
				output.add(line);
			}
			FileUtils.writeLines(new File(outFile), output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
