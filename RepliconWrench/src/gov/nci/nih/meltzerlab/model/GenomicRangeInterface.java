package gov.nci.nih.meltzerlab.model;

public interface GenomicRangeInterface {

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#getChromosome()
	 */
	public abstract String getChromosome();

	/* (non-Javadoc)
	 * @see model.genome.range.GenomicRangeInterface#setChromosome(java.lang.String)
	 */
	public abstract void setChromosome(String chromosome);

	public abstract Integer getStart();

	public abstract void setStart(Integer start);

	public abstract Integer getEnd();

	public abstract void setEnd(Integer end);
	
	/**
	 * Returns a BED-formatted record. The last column may be name, score, or left blank
	 * @return
	 */
	public abstract String toBed();

}