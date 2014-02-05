package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public class CutFinderIMExclusiveChoice implements CutFinder {

	public Cut findCut(IMLog log, LogInfo logInfo, MiningParameters parameters) {
		//compute the connected components of the directly-follows graph
		ConnectivityInspector<XEventClass, DefaultWeightedEdge> connectedComponentsGraph = 
				new ConnectivityInspector<XEventClass, DefaultWeightedEdge>(
				logInfo.getDirectlyFollowsGraph());
		List<Set<XEventClass>> connectedComponents = connectedComponentsGraph.connectedSets();

		return new Cut(Operator.xor, connectedComponents);
	}

}
