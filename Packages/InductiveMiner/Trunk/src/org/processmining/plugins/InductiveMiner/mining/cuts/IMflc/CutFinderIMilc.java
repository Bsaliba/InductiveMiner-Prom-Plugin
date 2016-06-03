package org.processmining.plugins.InductiveMiner.mining.cuts.IMflc;

import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMf.CutFinderIMf;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMilc implements CutFinder {

	private static CutFinder cutFinderIM = new CutFinderIM();

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		//filter logInfo
		IMLogInfo logInfoFiltered = CutFinderIMf.filterNoise(logInfo, minerState.parameters.getNoiseThreshold());

		//call IM cut detection
		Cut cut = cutFinderIM.findCut(null, logInfoFiltered, minerState);

		return cut;
	}
}