package org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMi.CutFinderIMi;

public class DfgCutFinderNoiseFiltering implements DfgCutFinder {

	private static DfgCutFinder cutFinder = new DfgCutFinderSimple();

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		Dfg filteredDfg = filterDfg(dfg, minerState);
		return cutFinder.findCut(filteredDfg, minerState);
	}

	/**
	 * Filter the dfg as in IMi.
	 * 
	 * @param dfg
	 * @param minerState
	 * @return
	 */
	public static Dfg filterDfg(Dfg dfg, DfgMinerState minerState) {
		//filter the Dfg
		float threshold = minerState.getParameters().getNoiseThreshold();

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = CutFinderIMi
				.filterGraph(dfg.getDirectlyFollowsGraph(), dfg.getEndActivities(), threshold);

		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = CutFinderIMi
				.filterGraph(dfg.getEventuallyFollowsGraph(), dfg.getEndActivities(), threshold);

		MultiSet<XEventClass> filteredStartActivities = CutFinderIMi.filterActivities(dfg.getStartActivities(),
				threshold);

		MultiSet<XEventClass> filteredEndActivities = CutFinderIMi.filterActivities(dfg.getEndActivities(), threshold);

		return new Dfg(filteredDirectlyFollowsGraph, filteredEventuallyFollowsGraph, dfg.getParallelGraph(),
				dfg.getUncertainDirectlyFollowsGraph(), dfg.getUncertainEventuallyFollowsGraph(),
				filteredStartActivities, filteredEndActivities, dfg.getUncertainStartActivities(),
				dfg.getUncertainEndActivities());
	}

}
