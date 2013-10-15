package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesEventuallyFollows extends Probabilities {

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
			double average = getAverageOccurrence(relation, a, b);
			return (1 / 4.0) * (1 / (average + 1));
		}
	}

	public double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			if (relation.getMinimumSelfDistanceBetween(a).contains(b)
					|| relation.getMinimumSelfDistanceBetween(b).contains(a)) {
				double w = getMsdOccurrences(relation, a, b);
				return 1 / (w + 1);
			} else {
				double z = getAverageOccurrence(relation, a, b);
				return 1 - 1 / (z + 1);
			}
		} else if (graph.containsEdge(a, b)) {
			double x = graph.getEdgeWeight(graph.getEdge(a, b));
			return (1 / 2.0) * 1 / (x + 1);
		} else if (graph.containsEdge(b, a)) {
			double y = graph.getEdgeWeight(graph.getEdge(b, a));
			return (1 / 2.0) * 1 / (y + 1);
		} else {
			double average = getAverageOccurrence(relation, a, b);
			return (1 / 4.0) * (1 / (average + 1));
		}
	}

	public double getProbabilityLoopSingle(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return getProbabilitySequence(relation, a, b);
	}

	public double getProbabilityLoopDouble(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			if (relation.getMinimumSelfDistanceBetween(a).contains(b)
					|| relation.getMinimumSelfDistanceBetween(b).contains(a)) {
				double w = getMsdOccurrences(relation, a, b);
				return 1 - 1 / (w + 1);
			} else {
				double z = getAverageOccurrence(relation, a, b);
				return 1 / (z + 1);
			}
		}
		return getProbabilityParallel(relation, a, b);
	}

	private double getAverageOccurrence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return (getActivityCount(relation, a) + getActivityCount(relation, b)) / 2.0;
	}

	private double getMsdOccurrences(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return relation.getMinimumSelfDistanceBetween(a).getCardinalityOf(b) + relation.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}
}
