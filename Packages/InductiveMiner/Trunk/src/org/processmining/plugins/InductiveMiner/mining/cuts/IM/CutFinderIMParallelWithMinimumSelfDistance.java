package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public class CutFinderIMParallelWithMinimumSelfDistance implements CutFinder {
	
	CutFinderIMParallel parallelCutFinder;
	
	public CutFinderIMParallelWithMinimumSelfDistance() {
		parallelCutFinder = new CutFinderIMParallel();
	}

	public Cut findCut(IMLog log, LogInfo logInfo, MiningParameters parameters) {
		return parallelCutFinder.findCut(log, logInfo, parameters, true);
	}

}
