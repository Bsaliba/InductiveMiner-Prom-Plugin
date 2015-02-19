package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class DfgCutFinderMergeParallelGraph implements DfgCutFinder {

	/**
	 * Combine the parallel graph with the directly-follows graph.
	 */
	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		
		//add edges from parallel graph
		for (long e : dfg.getParallelGraph().getEdges()) {
			long weight = dfg.getParallelGraph().getEdgeWeight(e);
			XEventClass a1 = dfg.getParallelGraph().getEdgeSource(e);
			XEventClass a2 = dfg.getParallelGraph().getEdgeTarget(e);
			
			dfg.addDirectlyFollowsEdge(a1, a2, weight);
			dfg.addDirectlyFollowsEdge(a2, a1, weight);
		}
		
		return null;
	}
	
}
