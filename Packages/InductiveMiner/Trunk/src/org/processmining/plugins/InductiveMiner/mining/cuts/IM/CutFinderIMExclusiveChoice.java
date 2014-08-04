package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;

public class CutFinderIMExclusiveChoice implements CutFinder, DfgCutFinder {

	public Cut findCut(final IMLog log, final IMLogInfo logInfo, final MinerState minerState) {
		return findCut(logInfo.getDirectlyFollowsGraph());
	}
	
	public Cut findCut(final Dfg dfg, final DfgMinerState minerState) {
		return findCut(dfg.getDirectlyFollowsGraph());
	}
	
	public static Cut findCut(final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph) {
		//compute the connected components of the directly-follows graph
		ConnectivityInspector<XEventClass, DefaultWeightedEdge> connectedComponentsGraph = 
				new ConnectivityInspector<XEventClass, DefaultWeightedEdge>(
				graph);
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();

		return new Cut(Operator.xor, connectedComponents);
	}

}
