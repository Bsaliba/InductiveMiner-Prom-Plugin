package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesSimple extends Probabilities {

	public double getProbabilityXor(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) || graph.containsEdge(b, a)) {
			return 0;
		} else {
			double average = (getActivityCount(relation, a) + getActivityCount(relation, b)) / 2.0;
			return 1 - (1 / (average + 1));
		}
	}

	public double getProbabilitySequence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(b, a)) {
			return 0;
		} else if (graph.containsEdge(a, b)) {
			double x = graph.getEdgeWeight(graph.getEdge(a, b));
			return 1 - 1 / (x + 1);
		} else {
			double average = (getActivityCount(relation, a) + getActivityCount(relation, b)) / 2.0;
			return (1 / 3.0) * (1 / (average + 1));
		}
	}

	public double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			return 1;
		} else if (graph.containsEdge(a, b)) {
			double x = graph.getEdgeWeight(graph.getEdge(a, b));
			return 1 / (x + 1);
		} else if (graph.containsEdge(b, a)) {
			double x = graph.getEdgeWeight(graph.getEdge(b, a));
			return 1 / (x + 1);
		} else {
			double average = (getActivityCount(relation, a) + getActivityCount(relation, b)) / 2.0;
			return (1 / 3.0) * (1 / (average + 1));
		}
	}

}
