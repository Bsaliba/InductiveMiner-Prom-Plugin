package org.processmining.plugins.InductiveMiner.dfgOnly.log2dfg;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;

public interface IMLog2IMLogInfo {

	/**
	 * Create an IMLogInfo from an IMLog
	 * 
	 * @param log
	 * @return the IMLogInfo
	 */
	public IMLogInfo createLogInfo(IMLog log);

}
