package gov.nci.nih.meltzerlab.replicon.utils;

import gov.nci.nih.meltzerlab.model.ScoredGenomicRange;
import gov.nci.nih.meltzerlab.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class BedToInit 
{

	public static int BIN_SIZE = 500;
	public static double BACKGROUND_FREQ = 0.0001;
	private static final double TELOMERE_BOOST_VALUE = 100;

	public static File INPUT_BED = null;
	private static int MAX_COORDINATE = 0;
	private static double[] INIT_POSITIONS = null;
	private static List<ScoredGenomicRange> BED_FILE = null;
	private static double MAX_VALUE = Double.MIN_VALUE;

	public static boolean USE_SCORE = false;
	private static final double ONE = 1.0;
	public static boolean TELOMERE_BOOST = false;
	public static File OUTPUT_FILE = null;

	private static Logger LOG = Logger.getLogger(BedToInit.class);	
	
	public static void work() 
	{
		try 
		{
			initValues();
			LOG.debug("init done");
			generateInitValues();
			LOG.debug("generated init values");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (TELOMERE_BOOST)
		{
			doTeleomereBoost();
			LOG.debug("telomere boost done");
		}
		LOG.debug("printing data");
		printData();
	}

	private static void initValues() throws IOException 
	{
		if (BED_FILE != null)
		{
			BED_FILE = null;
			INIT_POSITIONS = null;
		}
		BED_FILE = new ArrayList<ScoredGenomicRange>();
		readBedFile();
		
		ScoredGenomicRange range = null;
		MAX_VALUE = Double.MIN_VALUE;
		MAX_COORDINATE = Integer.MIN_VALUE;
		for (int i = 0, n = BED_FILE.size(); i < n; i++)
		{
			range = BED_FILE.get(i);
			MAX_VALUE = Math.max(MAX_VALUE, range.getScore());
			MAX_COORDINATE = Math.max(MAX_COORDINATE, range.getEnd());
		}
		INIT_POSITIONS = new double[MAX_COORDINATE / BIN_SIZE + 1];
	}
	
	/**
	 * Reads the Bed file
	 */
	private static void readBedFile()
	{
		BufferedReader in;
		try 
		{
			in = new BufferedReader(new FileReader(INPUT_BED));
			String line = null;
			String[] columns;
			while ((line = in.readLine()) != null)
			{
				columns = StringUtils.split(line);
				ScoredGenomicRange range = new ScoredGenomicRange(columns[0], Integer.parseInt(columns[1]), Integer.parseInt(columns[2]),
						Double.parseDouble(columns[3]));
				BED_FILE.add(range);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.sort(BED_FILE);
	}

	private static void generateInitValues() throws IOException
	{
		int bedLine = 0;
		int n = BED_FILE.size();
		ScoredGenomicRange bedRecord = null;
		int bedRecordEnd = -1;
		int bedRecordStart = -1;
		double bedRecordValue = 0.0;
		double value = 0.0;
		for (int i = 0; i < INIT_POSITIONS.length; i++)
		{
			int initPosition = i * BIN_SIZE;
			while (initPosition > bedRecordEnd && bedLine < n) // advance the bed file
			{
				bedRecord = BED_FILE.get(bedLine);
				bedRecordStart = bedRecord.getStart();
				bedRecordEnd = bedRecord.getEnd();
				bedRecordValue = bedRecord.getScore();
				bedLine++;
			}

			if ((initPosition + BIN_SIZE) > bedRecordStart && initPosition < bedRecordEnd)
			{
				if (USE_SCORE)
				{
					value = Math.max(bedRecordValue / MAX_VALUE, BACKGROUND_FREQ);
					INIT_POSITIONS[i] =  value;
				}
				else
				{
					INIT_POSITIONS[i] = ONE;
				}
			}
			else // no overlap with bed record
			{
				INIT_POSITIONS[i] = BACKGROUND_FREQ;
			}
		}
	}

	private static void doTeleomereBoost()
	{
		int lastIndex = INIT_POSITIONS.length - 1;
		for (int i = 0; i <= TELOMERE_BOOST_VALUE; i++)
		{
			double boost = (TELOMERE_BOOST_VALUE - i) / TELOMERE_BOOST_VALUE * (1.0 - BACKGROUND_FREQ) + BACKGROUND_FREQ;
			INIT_POSITIONS[i] = boost;
			INIT_POSITIONS[lastIndex - i] = boost;
		}
	}

	private static void printData()
	{
		StringBuilder data = new StringBuilder();
		
		for (int i = 0; i < INIT_POSITIONS.length; i++)
		{
			data.append(i * BIN_SIZE);
			data.append(Constants.TAB);
			data.append(INIT_POSITIONS[i]);
			data.append(Constants.TAB);
			data.append(ONE);
			data.append(Constants.NEW_LINE);
		}
		try {
			FileUtils.writeStringToFile(OUTPUT_FILE, data.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = null;
	}
}
