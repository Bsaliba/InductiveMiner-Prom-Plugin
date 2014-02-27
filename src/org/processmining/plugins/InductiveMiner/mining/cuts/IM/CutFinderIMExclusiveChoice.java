package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;

public class CutFinderIMExclusiveChoice implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		//compute the connected components of the directly-follows graph
		ConnectivityInspector<XEventClass, DefaultWeightedEdge> connectedComponentsGraph = 
				new ConnectivityInspector<XEventClass, DefaultWeightedEdge>(
				logInfo.getDirectlyFollowsGraph());
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();

		return new Cut(Operator.xor, connectedComponents);
	}

}
