package gov.nci.nih.meltzerlab.model;

import gov.nci.nih.meltzerlab.utils.Constants;


public class NamedGenomicRange extends GenomicRange
{
	private String name;
	public NamedGenomicRange(String chromosome, Integer start, Integer end, String name) 
	{
		super(chromosome, start, end);
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	public String toString() {
		return "NamedGenomicRange [name=" + name + ", Chromosome="
				+ getChromosome() + ", Start=" + getStart()
				+ ", End=" + getEnd() + "]";
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
		builder.append(this.getName());
		return builder.toString();
	}


}
