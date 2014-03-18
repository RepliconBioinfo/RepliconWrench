package gov.nci.nih.meltzerlab.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinUtils 
{
	 private static final int BIN_OFFSETS[] = {512+64+8+1, 64+8+1, 8+1, 1, 0};
	/**
	 * Given start,end in chromosome coordinates assign it
	 * a bin.   There's a bin for each 128k segment, for each
	 * 1M segment, for each 8M segment, for each 64M segment,
	 * and for each chromosome (which is assumed to be less than
	 * 512M.)  A range goes into the smallest bin it will fit in. 
	 * @param start
	 * @param end
	 * @return
	 */
	public static int getBinFromCoords (int start, int end)
	{
		int startBin = start;
		int endBin = end - 1;


		startBin = startBin>>17; //shift this many bits left to get to the finest bin
		endBin = endBin>>17;

		for (int n=0; n < BIN_OFFSETS.length; n++)
		{
			if (startBin == endBin)
			{
				return (BIN_OFFSETS[n]+startBin);
			}	
			startBin = startBin>>3; //onto the progressively coarser bin
		endBin = endBin>>3;			
		}
		return -1; //error checking
	}

	/**
	 * Returns a @param result of bins 'above' this one. In other words, larger sized-bins that overlap with this one.
	 * The 0 bin is NOT excluded. Similiar to getBinFromCoords, but keeps on recording bins after appropriate bin is found. 
	 * @param start
	 * @param end
	 * @param bin
	 * @return
	 */
	public static List<Integer> getBinsAbove(int startCoordinate, int endCoordinate)
	{
		int startBin = startCoordinate;
		int endBin = endCoordinate - 1;
		int bottomBin = getBinFromCoords(startCoordinate, endCoordinate);
		List<Integer> binsAbove = new ArrayList<Integer>();
		
		startBin = startBin>>17; //shift this many bits left to get to the finest bin
		endBin = endBin>>17;
		
		for (int n=0; n < BIN_OFFSETS.length; n++)
		{
		//	System.out.print(binOffsets[n]+"+"+startBin+"=");
			//System.out.print(binOffsets[n]+startBin);
			if ( (BIN_OFFSETS[n]+startBin <= bottomBin) && (BIN_OFFSETS[n] >= 0) ) //if calculated bin is coarser than the bin which contains this feature, but is not as coarse as bin#0
			{
			//	System.out.println(binOffsets[n]+startBin);
				binsAbove.add(BIN_OFFSETS[n]+startBin); //add the bin to a collection of overlapping bins
			}
			
			startBin = startBin>>3; //onto the progressively coarser bin
			endBin = endBin>>3;			
		}
		return binsAbove;
	}
	
	/**
	 * Visit each 'child' bin using recursion. Keep track of all the bins visited.
	 * Will return all possible bins if bin = 0;
	 * @param tempBin - the bin from which the search is started
	 * @param result - List where below bins ought to be kept
	 */
	public static void getBinsBelow (int tempBin, Set<Integer> result)
	{
		//LinkedList result = new LinkedList();
		int level = getBinLevel(tempBin);
		
		if (level == 0) //already the smaller bin
			return;
		
		else
		{
			int maxBinLowerLevel = (tempBin - BIN_OFFSETS[level]) * 8 + ( (BIN_OFFSETS[level-1])); //get the lowest child's bin# 
			
			//binsBelowTracker(maxBinLowerLevel); //add the maximum allowable smaller bin; 
			
			for (int n=0; n<8; n++) //add the  8 possible 'children' bins to the bin tracker.
			{
				result.add(maxBinLowerLevel+n);
			//	System.out.println("adding "+maxBinLowerLevel);
				getBinsBelow(maxBinLowerLevel+n, result); //recursion
			}
		}
	}

	/**
	 * Levels correspond to the offsets array {512+64+8+1, 64+8+1, 8+1, 1, 0}
	 * @param bin
	 * @return
	 */
	private static int getBinLevel (int bin)
	{
		if (bin >= 585 )
			return 0;
		else if (bin >= 73)
			return 1;
		else if (bin >= 9)
			return 2;
		else if (bin >= 1)
			return 3;
		else if (bin ==0)
			return 4;
		else
			return -1;
	}
	
	/**
	 * Returns the bin corresponding to the coordinates as well as all the overlapping bins.
	 * This method combines: getBinFromCoords, getBinsAbove, and getBinsBelow methods
	 * @param startCoordinate
	 * @param endCoordinate
	 * @return List of Integers representing bin numbers
	 */
	public static Set<Integer> getOverlappingBins(int startCoordinate, int endCoordinate)
	{
		int bin = getBinFromCoords(startCoordinate, endCoordinate);
		Set<Integer> bins = new HashSet<Integer>();
		// Add larger overlapping bins, which will include present bin
		bins.addAll(getBinsAbove(startCoordinate, endCoordinate));

		// Add smaller overlapping bins from
		getBinsBelow(bin, bins);
		
		return bins;

	}
	
	
	public static void main (String[] args)
	{
		
//		System.out.println(getQueryOverlappingBins(1, 1000));
//		System.out.println(getBinFromCoords(0, 129000));
//		int i = 585;
//		int value = 1000;
//		for (int i = 0; i<3; i++)
//		{
//			value = value<<3;
//		System.out.print(value+" ");
//		}
		
//		List<Integer> above  = getBinsAbove(42569698, 42569698);
//		List<Integer> below = new ArrayList<Integer>(); 
//		getBinsBelow(bin, below);
//		
//		System.out.println("bin:"+bin);
//		System.out.println("Bins above: ");
//		
//		Iterator<Integer> iter = above.iterator();
//		while (iter.hasNext())
//		{
//			System.out.println("\t"+iter.next());
//		}
//		
//		iter = below.iterator();
//		System.out.println("Bins below: ");
//		while (iter.hasNext())
//		{
//			System.out.println("\t"+iter.next());
//		}
//		
	}

}
