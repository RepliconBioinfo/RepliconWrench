package gov.nci.nih.meltzerlab.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;


public class IOUtils 
{

	public static void writeLines(Collection<?> collection, File file)
	{
		try {
			FileUtils.writeLines(file, collection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	/**
	 * Counts the number of lines in a file
	 * Will read a g-zipped file
	 * @param file
	 * @return
	 */
	public static int countLines(File file)
	{
		int lineNumber = 0;
		try {
			BufferedReader in;
			if (isGzipped(file))
			{
				in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			}
			else
			{
				in = new BufferedReader(new FileReader(file));
			}
			while (in.readLine() != null)
			{
				lineNumber++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lineNumber;
	}
	
	/**
	 * Uncompresses a Gzipped file
	 * @param inFile
	 * @param outFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void unGzip(File inFile, File outFile) throws FileNotFoundException, IOException {
		GZIPInputStream gIn  = new GZIPInputStream(new FileInputStream(inFile));
		FileOutputStream fos = new FileOutputStream(outFile);
		byte[] buffer = new byte[100000];
		int len;
		while ((len = gIn.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
		
		gIn.close();
		fos.close();
	}
	
	/**
	 * Returns true if file has a .gz extension 
	 * @param file
	 * @return
	 */
	public static boolean isGzipped(File file)
	{
		return file.getName().endsWith(".gz");
	}
}
