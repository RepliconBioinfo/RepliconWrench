package gov.nci.nih.meltzerlab.replicon.utils;

import gov.nci.nih.meltzerlab.model.ScoredGenomicRange;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * Given a bed file with more than one chromosome,
 * generate individual replication initiation files
 * one per chromosome
 * @author yev
 *
 */
public class BedToIPLS 
{

	private static Logger LOG = Logger.getLogger(BedToIPLS.class);

	private static File INPUT;
	private static File OUTPUT_DIR;
	private static String OUT_PREFIX;
	private static int CHROMOSOME_COLUMN = 0;
	private static int START_COLUMN = 1;
	private static int END_COLUMN = 2;
	private static int VALUE_COLUMN = 3;
	private static int BIN_WIDTH = 500;
	private static boolean NULLIFY_SCORE = false;

	public static void entry(String[] args)
	{
		Options options = new Options();
		options.addOption("i", "input", true, "input .bed file");
		options.addOption("c", "chromosome", true, "chromosome column (1-based) default 1");
		options.addOption("s", "start", true, "start column (1-based) default 2");
		options.addOption("e", "end", true, "end column (1-based) default 3");
		options.addOption("v", "value", true, "value column (1-based) default 4");
		options.addOption("o", "out-dir", true, "output directory");
		options.addOption("b", "bin-width", true, "specify bin width (default=500)");
		options.addOption("g", "ignore-score", false, "ignore the score column (P for all sites will be set to 1.0)");


		CommandLineParser parser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (commandLine.hasOption('h') || commandLine.getOptions().length == 0)
		{
			Utils.printUsageAndExit("BedToIPLS", options);
		}

		if (commandLine.hasOption('g'))
		{
			NULLIFY_SCORE = true;
		}


		if (commandLine.hasOption('b'))
		{
			BIN_WIDTH = Integer.parseInt(commandLine.getOptionValue('b'));
		}

		INPUT = new File(commandLine.getOptionValue('i'));
		if (INPUT.getName().contains(Constants.PERIOD))
		{
			OUT_PREFIX = INPUT.getName().substring(0, INPUT.getName().indexOf('.'));
		}
		else
		{
			OUT_PREFIX = INPUT.getName();
		}

		OUTPUT_DIR = new File(commandLine.getOptionValue('o'));
		if (commandLine.hasOption('c'))
		{
			CHROMOSOME_COLUMN = Integer.parseInt(commandLine.getOptionValue('c')) - 1;
		}

		if (commandLine.hasOption('s'))
		{
			START_COLUMN = Integer.parseInt(commandLine.getOptionValue('s')) - 1;
		}

		if (commandLine.hasOption('e'))
		{
			END_COLUMN = Integer.parseInt(commandLine.getOptionValue('e')) - 1;
		}

		if (commandLine.hasOption('v'))
		{
			VALUE_COLUMN = Integer.parseInt(commandLine.getOptionValue('v')) - 1;
		}

		work();
	}

	private static void work() 
	{
		Set<String> chromosomes = discoverChromosomes();
		for (String chromosome : chromosomes)
		{
			processChromosome(chromosome);
		}
	}

	private static void processChromosome(String chromosome) 
	{
		LOG.info("Processing IPLS for chromosome " + chromosome);
		List<ScoredGenomicRange> data = new ArrayList<ScoredGenomicRange>();
		try {
			BufferedReader in;
			if (IOUtils.isGzipped(INPUT))
			{
				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(INPUT))));
			}
			else
			{
				in = new BufferedReader(new FileReader(INPUT));
			}
			String line = null;
			String[] columns = null;
			ScoredGenomicRange range = null;
			while((line = in.readLine()) != null)
			{
				columns = StringUtils.split(line);
				if (columns[CHROMOSOME_COLUMN].equals(chromosome))
				{
					if (NULLIFY_SCORE)
					{
						range = new ScoredGenomicRange(columns[CHROMOSOME_COLUMN], 
								Integer.parseInt(columns[START_COLUMN]), 
								Integer.parseInt(columns[END_COLUMN]), 
								Double.MAX_VALUE);
					}
					else
					{
						range = new ScoredGenomicRange(columns[CHROMOSOME_COLUMN], 
								Integer.parseInt(columns[START_COLUMN]), 
								Integer.parseInt(columns[END_COLUMN]), 
								Double.parseDouble(columns[VALUE_COLUMN]));				
					}
					data.add(range);
				}
			}
			in.close();

			Collections.sort(data);

			List<String> printData = new ArrayList<String>(data.size());
			for (ScoredGenomicRange datum : data)
			{
				printData.add(datum.toBed());
			}

			File bedFile = File.createTempFile(RandomStringUtils.randomAlphanumeric(8), null);
			FileUtils.writeLines(bedFile, printData);

			BedToInit.BIN_SIZE = BIN_WIDTH;
			BedToInit.INPUT_BED = bedFile;
			BedToInit.OUTPUT_FILE = new File(OUTPUT_DIR + "/" + chromosome, chromosome + "_" + OUT_PREFIX);
			BedToInit.TELOMERE_BOOST = false;
			BedToInit.USE_SCORE = true;
			BedToInit.work();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Discovers chromosomes in file
	 * @return
	 */
	private static Set<String> discoverChromosomes() 
	{
		Set<String> chromosomes = new HashSet<String>();

		try {
			BufferedReader in;
			if (IOUtils.isGzipped(INPUT))
			{
				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(INPUT))));
			}
			else
			{
				in = new BufferedReader(new FileReader(INPUT));
			}

			String line = null;
			String[] columns = null;

			while((line = in.readLine()) != null)
			{
				columns = StringUtils.split(line);
				chromosomes.add(columns[CHROMOSOME_COLUMN]);
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return chromosomes;
	}

}
