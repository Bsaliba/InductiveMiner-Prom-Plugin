package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;

public class DfgCutFinderMergeParallelGraph implements DfgCutFinder {

	/**
	 * Combine the parallel graph with the directly-follows graph.
	 */
	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		
		//add edges from parallel graph
		for (DefaultWeightedEdge e : dfg.getParallelGraph().edgeSet()) {
			dfg.getDirectlyFollowsGraph().addEdge(dfg.getParallelGraph().getEdgeSource(e), dfg.getParallelGraph().getEdgeTarget(e));
			dfg.getDirectlyFollowsGraph().addEdge(dfg.getParallelGraph().getEdgeTarget(e), dfg.getParallelGraph().getEdgeSource(e));
		}
		
		return null;
	}
	
}
