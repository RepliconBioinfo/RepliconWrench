package gov.nci.nih.meltzerlab.model;

import gov.nci.nih.meltzerlab.utils.BinUtils;
import gov.nci.nih.meltzerlab.utils.Constants;

/**
 * Chrosomosome, start, end
 * @author yev
 *
 */
public abstract class GenomicRange implements Comparable<GenomicRange>, GenomicRangeInterface
{
	private String chromosome;
	private Integer start;
	private Integer end;

	/**
	 * 
	 * @param chromosome
	 * @param start
	 * @param end
	 */
	public GenomicRange(String chromosome, Integer start, Integer end) 
	{
		this.chromosome = chromosome;
		if (start > end)
		{
			this.start = end;
			this.end = start;
		}
		else
		{
			this.start = start;
			this.end = end;
		}
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getRegionLength()
	 */
	public int getRegionLength()
	{
		return this.end - this.getStart();
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getOverlapLength(model.genome.range.GenomicRangeInterface)
	 */
	public int getOverlapLength(GenomicRangeInterface other)
	{
		if (this.overlaps(other))
		{
			return Math.min(this.end, other.getEnd()) - Math.max(this.start, other.getStart());
		}
		else
		{
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#overlaps(model.genome.range.GenomicRangeInterface)
	 */
	public boolean overlaps(GenomicRangeInterface other)
	{
		if (this.chromosome.equals(other.getChromosome()))
		{
			if (this.getEnd() >= other.getStart() && this.getStart() <= other.getEnd())
			{
				return true;
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getChromosome()
	 */
	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#getChromosome()
	 */
	@Override
	public String getChromosome() {
		return chromosome;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#setChromosome(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#setChromosome(java.lang.String)
	 */
	@Override
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getStart()
	 */

	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#getStart()
	 */
	@Override
	public Integer getStart() {
		return start;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#setStart(java.lang.Integer)
	 */

	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#setStart(java.lang.Integer)
	 */
	@Override
	public void setStart(Integer start) {
		this.start = start;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getEnd()
	 */

	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#getEnd()
	 */
	@Override
	public Integer getEnd() {
		return end;
	}

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#setEnd(java.lang.Integer)
	 */

	/* (non-Javadoc)
	 * @see yg.model.genome.range.GenomicRangeInterface#setEnd(java.lang.Integer)
	 */
	@Override
	public void setEnd(Integer end) {
		this.end = end;
	}




	public int compareTo(GenomicRange other) 
	{
		if (! this.chromosome.equals(other.getChromosome()))
		{
			return this.chromosome.compareTo(other.getChromosome());
		}
		else if (! this.start.equals(other.getStart()))
		{
			return this.start.compareTo(other.getStart());
		}
		else
		{
			return this.end.compareTo(other.getEnd());
		}
	}
	
	/**
	 * Bin is calculated according to binning scheme used
	 * at UCSC geneome browser
	 */
	public int getBin()
	{
		return BinUtils.getBinFromCoords(start, end);
	}
	
	
	public String toBed() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append(this.getChromosome());
		builder.append(Constants.TAB);
		builder.append(this.getStart());
		builder.append(Constants.TAB);
		builder.append(this.getEnd());
		return builder.toString();
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GenomicRange [chromosome=");
		builder.append(chromosome);
		builder.append(", start=");
		builder.append(start);
		builder.append(", end=");
		builder.append(end);
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((chromosome == null) ? 0 : chromosome.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GenomicRange other = (GenomicRange) obj;
		if (chromosome == null) {
			if (other.chromosome != null) {
				return false;
			}
		} else if (!chromosome.equals(other.chromosome)) {
			return false;
		}
		if (end == null) {
			if (other.end != null) {
				return false;
			}
		} else if (!end.equals(other.end)) {
			return false;
		}
		if (start == null) {
			if (other.start != null) {
				return false;
			}
		} else if (!start.equals(other.start)) {
			return false;
		}
		return true;
	}
	
	public String getName()
	{
		return null;
	}
}
