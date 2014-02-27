package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;

public class CutFinderIMParallelWithMinimumSelfDistance implements CutFinder {
	
	CutFinderIMParallel parallelCutFinder;
	
	public CutFinderIMParallelWithMinimumSelfDistance() {
		parallelCutFinder = new CutFinderIMParallel();
	}

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return parallelCutFinder.findCut(log, logInfo, minerState, true);
	}

}
