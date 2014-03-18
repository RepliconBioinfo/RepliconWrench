package gov.nci.nih.meltzerlab.model;

import gov.nci.nih.meltzerlab.utils.Constants;

/**
 * A scored genomic region
 * @author yev
 *
 */
public class ScoredGenomicRange extends GenomicRange
{
	private double score;
	
	/**
	 * 
	 * @param chromosome
	 * @param start
	 * @param end
	 * @param score the score for this genomic region
	 */
	public ScoredGenomicRange(String chromosome, Integer start, Integer end, double score) 
	{
		super(chromosome, start, end);
		this.score = score;
	}
	
	/**
	 * Get the score for this genomic region
	 * @return
	 */
	public double getScore() 
	{
		return score;
	}
	
	public void setScore(double score)
	{
		this.score = score;
	}

	@Override
	public String toBed() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append(this.getChromosome());
		builder.append(Constants.TAB);
		builder.append(this.getStart());
		builder.append(Constants.TAB);
		builder.append(this.getEnd());
		builder.append(Constants.TAB);
		builder.append(this.getScore());
		return builder.toString();
	}

}
