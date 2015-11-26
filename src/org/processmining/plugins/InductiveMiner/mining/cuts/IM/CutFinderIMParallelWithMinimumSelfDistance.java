package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMParallelWithMinimumSelfDistance implements CutFinder {

	public Cut findCut(final IMLog log, final IMLogInfo logInfo, final MinerState minerState) {
		return CutFinderIMParallel.findCut(logInfo.getStartActivities(), logInfo.getEndActivities(),
				logInfo.getDirectlyFollowsGraph(), new Function<XEventClass, MultiSet<XEventClass>>() {

					public MultiSet<XEventClass> call(XEventClass input) throws Exception {
						return logInfo.getMinimumSelfDistanceBetween(input);
					}
				});
	}

}
