package org.processmining.plugins.InductiveMiner.mining.SAT;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public class ProbabilitiesSimple extends Probabilities {

	public ProbabilitiesSimple(DirectlyFollowsRelation relation) {
		super(relation);
	}

	public double getProbabilityXor(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) || graph.containsEdge(b, a)) {
			return 0;
		} else {
			double average = (getActivityCount(a) + getActivityCount(b)) / 2.0;
			return 1 - (1 / (average + 1));
		}
	}

	public double getProbabilitySequence(XEventClass a, XEventClass b) {
		if (D(b, a)) {
			return 0;
		} else if (D(a, b)) {
			return 1 - 1 / (x(a, b) + 1);
		} else {
			double average = getAverageOccurrence(a, b);
			return (1 / 4.0) * (1 / (average + 1));
		}
	}

	public double getProbabilityParallel(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			if (relation.getMinimumSelfDistanceBetween(a).contains(b)
					|| relation.getMinimumSelfDistanceBetween(b).contains(a)) {
				double w = getMsdOccurrences(a, b);
				return 1 / (w + 1);
			} else {
				double z = getAverageOccurrence(a, b);
				return 1 - 1 / (z + 1);
			}
		} else if (graph.containsEdge(a, b)) {
			double x = graph.getEdgeWeight(graph.getEdge(a, b));
			return (1 / 2.0) * 1 / (x + 1);
		} else if (graph.containsEdge(b, a)) {
			double y = graph.getEdgeWeight(graph.getEdge(b, a));
			return (1 / 2.0) * 1 / (y + 1);
		} else {
			double average = getAverageOccurrence(a, b);
			return (1 / 4.0) * (1 / (average + 1));
		}
	}

	public double getProbabilityLoopSingle(XEventClass a, XEventClass b) {
		return getProbabilitySequence(a, b);
	}

	public double getProbabilityLoopDouble(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		if (graph.containsEdge(a, b) && graph.containsEdge(b, a)) {
			if (relation.getMinimumSelfDistanceBetween(a).contains(b)
					|| relation.getMinimumSelfDistanceBetween(b).contains(a)) {
				double w = getMsdOccurrences(a, b);
				return 1 - 1 / (w + 1);
			} else {
				double z = getAverageOccurrence(a, b);
				return 1 / (z + 1);
			}
		}
		return getProbabilityParallel(a, b);
	}

	private double getAverageOccurrence(XEventClass a, XEventClass b) {
		return (getActivityCount(a) + getActivityCount(b)) / 2.0;
	}

	private double getMsdOccurrences(XEventClass a, XEventClass b) {
		return relation.getMinimumSelfDistanceBetween(a).getCardinalityOf(b)
				+ relation.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}
}
