package gov.nci.nih.meltzerlab.replicon.main;

import gov.nci.nih.meltzerlab.replicon.utils.BedToIPLS;
import gov.nci.nih.meltzerlab.replicon.utils.CghToTiming;
import gov.nci.nih.meltzerlab.replicon.utils.DbChromosomeToIPLS;
import gov.nci.nih.meltzerlab.replicon.utils.TimingAgreement;

public class RepliconWrench {

	public static void main(String[] args) {
		if (args.length == 0) 
		{
			System.out.println("Usage: java -jar Replicon.jar [Desired-Replicon-Tool] [Tool-specific-options]");
		//	System.out.println("Available tools are: BedToIPLS, CghToTiming, DbChromosomeToIPLS, TimingAgreement");
		} 
		else 
		{
			String desiredTool = args[0];
			if (desiredTool.equalsIgnoreCase("BedToIPLS"))
			{
				BedToIPLS.entry(args);
			}
			else if (desiredTool.equalsIgnoreCase("CghToTiming")) {
				CghToTiming.entry(args);
			}
			else if (desiredTool.equalsIgnoreCase("DbChromosomeToIPLS")) {
				DbChromosomeToIPLS.entry(args);
			}
			else if (desiredTool.equalsIgnoreCase("TimingAgreement")) {
				TimingAgreement.entry(args);
			}
			else 
			{
				System.out.println("Desired tool " + desiredTool + " is not found");
			}
		}
	}
}