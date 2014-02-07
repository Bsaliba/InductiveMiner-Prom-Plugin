package org.processmining.plugins.InductiveMiner.mining.logSplitter;

import java.util.List;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public interface LogSplitter {
	
	/*
	 * usage: returns a list of sublogs.
	 * 
	 * Must be thread-safe and abstract, i.e, no side-effects allowed.
	 */
	public List<IMLog> split(IMLog log, IMLogInfo logInfo, Cut cut);
}
