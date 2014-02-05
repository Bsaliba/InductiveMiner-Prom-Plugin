package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public interface LogSplitter {
	
	/*
	 * usage: returns a list of sublogs.
	 * 
	 * Must be thread-safe and abstract, i.e, no side-effects allowed.
	 */
	public List<IMLog> split(IMLog log, LogInfo logInfo, Cut cut);
}
