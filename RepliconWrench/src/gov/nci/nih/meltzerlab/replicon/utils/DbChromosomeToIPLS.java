package gov.nci.nih.meltzerlab.replicon.utils;

import gov.nci.nih.meltzerlab.utils.Constants;
import gov.nci.nih.meltzerlab.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/*
 * DbChromosomeToIPLS -c chr1 -d genomeDbMySql.txt -o /Users/gindiny/temp -s -t refGene
 */
public class DbChromosomeToIPLS 
{
	private static String PASSWORD = "PASSWORD";
	private static String URL = "URL";
	private static String DB = "DB";
	private static String USERNAME = "USERNAME";

	private static int BIN_WIDTH = 500;
	private final static String CHROM = "chrom";
	private final static String CHROM_START = "chromStart";
	private final static String CHROM_END = "chromEnd";

	private static String TABLE_KEY = null;
	private static String CHROMOSOME = null;
	private static File OUT_DIR = null;
	private static boolean TELOMERE_BOOST = false;
	public static boolean IGNORE_SCORE = false;

	private static Connection CONNECTION = null;
	private static Set<String> SIGNAL_COLUMNS = null;

	private static int BUFFER_SIZE = 100000;
	static Map<String, Integer> CHROMOSOMES;
	private static Logger LOG = Logger.getLogger(DbChromosomeToIPLS.class);

	/**
	 * @param args
	 */
	public static void entry(String[] args) 
	{
		SIGNAL_COLUMNS = new HashSet<String>();

		Options options = new Options();
		options.addOption("t", "table-key", true, "a key to match tables (i.e. Encode)");
		options.addOption("c", "chromosome", true, "chromosome of interest");
		options.addOption("o", "out-dir", true, "output directory");
		options.addOption("v", "value-column-name", true, "name of the value column to base the landscape on: signalValue; score; pValue");
		options.addOption("s", "ignore-score", false, "ignore score intensity; set those probabilities to 1.0");
		options.addOption("b", "bin-width", true, "specify bin width (default=500)");
		options.addOption("d", "database-settings", true, "database settings file");

		CommandLineParser parser = new BasicParser();
		CommandLine commandLine;

		try {
			commandLine = parser.parse(options, args);
			if (commandLine.getOptions().length == 0 || commandLine.hasOption('h') 
					|| !commandLine.hasOption('t') || !commandLine.hasOption('c') 
					|| !commandLine.hasOption('o') || !commandLine.hasOption('v'))
			{
				Utils.printUsageAndExit("DbChromosomeToIPLS", options);
			}

			TABLE_KEY = commandLine.getOptionValue('t');
			CHROMOSOME = commandLine.getOptionValue('c');
			OUT_DIR = new File(commandLine.getOptionValue('o'));

			if (commandLine.hasOption('b'))
			{
				BIN_WIDTH = Integer.parseInt(commandLine.getOptionValue('b'));
			}

			if (commandLine.hasOption("s"))
			{
				IGNORE_SCORE = true;
			}

			File dbSettings = new File(commandLine.getOptionValue('d'));
			initDbConnection(dbSettings);

			String valueColumn = commandLine.getOptionValue('v');
			SIGNAL_COLUMNS.add(valueColumn);

		} catch (ParseException e) {
			e.printStackTrace();
		} 
		work();
		try {
			CONNECTION.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Does all the work
	 * 
	 */
	private static void work() 
	{
		List<String> candidateTables = getPotentialTables(TABLE_KEY, CONNECTION);
		File tempDir = new File(Constants.TEMP_DIR);
		for (String table : candidateTables)
		{
			String columnName = getIndexOfValueColumn(SIGNAL_COLUMNS, table, CONNECTION); 
			if (columnName != null)
			{
				LOG.debug("Processing " + table);
				File bedFile = makeBedFile(table, CHROMOSOME, tempDir, columnName, true, CONNECTION);

				if (bedFile != null)
				{
					BedToInit.BIN_SIZE = BIN_WIDTH;
					BedToInit.INPUT_BED = bedFile;
					BedToInit.OUTPUT_FILE = new File(OUT_DIR, CHROMOSOME + "_" + table);
					BedToInit.TELOMERE_BOOST = TELOMERE_BOOST;
					if (IGNORE_SCORE)
					{
						BedToInit.USE_SCORE = false;
					}
					else
					{
						BedToInit.USE_SCORE = true;
					}

					BedToInit.work();
				}
				else
				{
					LOG.error("No data were retrieved from table "  + table);
				}
			}
			else
			{
				LOG.debug("No suitable column in table " + table);
			}
		}
	}


	/**
	 * Creates a BED file from an SQL table of genome annotations
	 * @param table
	 * @param parentDirectory output file parent directory
	 * @param indexOfValueColumn the inedex of the colum with the value
	 * @param makeTemp should the BED file be temporary
	 * @return BED file
	 */
	private static File makeBedFile(String table, String chromosome,
			File parentDirectory, String columnName, boolean makeTemp, Connection connection) 
	{
		File bedFile = new File(parentDirectory, chromosome + "_" + table);
		boolean dataRead = false;
		if (bedFile.exists())
		{
			bedFile.delete();
		}
		if (makeTemp)
		{
			bedFile.deleteOnExit();
		}

		List<String> buffer = new ArrayList<String>(BUFFER_SIZE);
		StringBuilder builder = null;
		Statement statement;
		try {
			statement = connection.createStatement(ResultSet.CONCUR_READ_ONLY, ResultSet.FETCH_FORWARD);
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT chrom, chromStart, chromEnd, ");
			sql.append(columnName);
			sql.append(" FROM ");
			sql.append(table);
			sql.append(" WHERE chrom = '");
			sql.append(chromosome);
			sql.append("'");
			ResultSet rs = statement.executeQuery(sql.toString()); // field is first column
			while(rs.next())
			{
				builder = new StringBuilder();
				builder.append(rs.getString(CHROM));
				builder.append(Constants.TAB);
				builder.append(rs.getString(CHROM_START));
				builder.append(Constants.TAB);
				builder.append(rs.getString(CHROM_END));
				builder.append(Constants.TAB);
				builder.append(rs.getString(columnName));

				buffer.add(builder.toString());

				if (buffer.size() >= BUFFER_SIZE)
				{
					FileUtils.writeLines(bedFile, buffer, true);
					buffer.clear();
					dataRead = true;
				}
			}

			if (buffer.size() > 0)
			{
				dataRead = true;
			}

			FileUtils.writeLines(bedFile, buffer, true);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!dataRead)
		{
			return null;
		}
		else
		{
			return bedFile;
		}
	}

	/**
	 * Returns the index of the column of interest mapped onto its name
	 * @param table 
	 * @param valueColumns a collection of columns of interest
	 * @return null if the column was not found
	 */
	public static String getIndexOfValueColumn(Set<String> valueColumns, String table, Connection connection) 
	{
		String result = null;
		try {
			Statement statement = connection.createStatement(ResultSet.CONCUR_READ_ONLY, ResultSet.FETCH_FORWARD);
			ResultSet rs = statement.executeQuery("DESCRIBE " + table); // field is first column
			String column = null;
			while (rs.next())
			{
				column = rs.getString(1);
				if (valueColumns.contains(column))
				{
					result = column;
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Returns the list of all tables of potential interest.
	 * @return
	 */
	public static List<String> getPotentialTables(String tableKey, Connection connection) 
	{
		List<String> tables = new ArrayList<String>();
		try 
		{
			Statement statement = connection.createStatement(ResultSet.CONCUR_READ_ONLY, ResultSet.FETCH_FORWARD);
			String sql = "SHOW TABLES like '%" +  tableKey + "%'";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next())
			{
				tables.add(rs.getString(1));
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tables;
	}

	/**
	 * Initiates the connection to MySQL databset
	 * @param connectionParams the file with connection parameters
	 */
	private static void initDbConnection(File connectionParams) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		Map<String, String> params = getDbParams(connectionParams);

		String url = "jdbc:mysql://".concat(params.get(URL));
		if (!url.endsWith(Constants.FORWARD_SLASH)){
			url = url.concat(Constants.FORWARD_SLASH);
		}

		String db = params.get(DB);
		String userName = params.get(USERNAME);
		String password = params.get(PASSWORD);

		try {
			CONNECTION = DriverManager.getConnection(url.concat(db), userName, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, String> getDbParams(File connectionParams){
		Map<String, String> params = new HashMap<String, String>();
		try {
			List<String> paramText = FileUtils.readLines(connectionParams);
			String[] values = null;
			for (String raw : paramText) {
				values = StringUtils.split(raw, Constants.EQUALS);
				if (values[0].equals(PASSWORD) & values.length == 1) // blank password
				{
					params.put(values[0],Constants.NOTHING);
				}
				else
				{
					params.put(values[0], values[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!params.containsKey(URL) ||
				!params.containsKey(DB) ||
				!params.containsKey(USERNAME) ||
				!params.containsKey(PASSWORD))
		{
			LOG.error("One or more of the connection parameters is/are missing");
		}
		return params;
	}
}
