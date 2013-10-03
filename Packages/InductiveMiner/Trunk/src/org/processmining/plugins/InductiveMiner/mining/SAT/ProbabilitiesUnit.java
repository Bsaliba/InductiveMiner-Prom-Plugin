
package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesUnit extends Probabilities {

	public double getProbabilityXor(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) || graph.containsEdge(b, a)) {
			return 0;
		} else {
			return 1;
		}
	}

	public double getProbabilitySequence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && !graph.containsEdge(b, a)) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getProbabilityLoopSingle(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return getProbabilitySequence(relation, a, b);
	}

	public double getProbabilityLoopDouble(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return getProbabilityParallel(relation, a, b);
	}

}
