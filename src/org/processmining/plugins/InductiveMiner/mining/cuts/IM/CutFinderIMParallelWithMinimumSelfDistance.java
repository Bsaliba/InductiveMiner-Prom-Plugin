package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;

public class CutFinderIMParallelWithMinimumSelfDistance implements CutFinder {
	
	CutFinderIMParallel parallelCutFinder;
	
	public CutFinderIMParallelWithMinimumSelfDistance() {
		parallelCutFinder = new CutFinderIMParallel();
	}

	public Cut findCut(IMLog log, IMLogInfo logInfo, MiningParameters parameters) {
		return parallelCutFinder.findCut(log, logInfo, parameters, true);
	}

}
