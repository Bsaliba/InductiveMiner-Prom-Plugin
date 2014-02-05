package org.processmining.plugins.InductiveMiner.mining.cuts;

import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public interface CutFinder {

	public Cut findCut(IMLog log, LogInfo logInfo, MiningParameters parameters);
	
}
