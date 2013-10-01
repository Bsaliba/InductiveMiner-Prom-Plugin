package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.math.BigInteger;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public abstract class Probabilities {

	public abstract double getProbabilityXor(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public abstract double getProbabilitySequence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public abstract double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public final int doubleToIntFactor = 1000;

	public BigInteger toBigInt(double probability) {
		return BigInteger.valueOf(Math.round(doubleToIntFactor * probability));
	}

	public BigInteger getProbabilityXorB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityXor(relation, a, b));
	}

	public BigInteger getProbabilitySequenceB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilitySequence(relation, a, b));
	}

	public BigInteger getProbabilityParallelB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityParallel(relation, a, b));
	}

	protected long getActivityCount(DirectlyFollowsRelation relation, XEventClass a) {
		//count how often each activity occurs
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		double sum = 0;
		for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(a)) {
			sum += graph.getEdgeWeight(edge);
		}
		sum += relation.getEndActivities().getCardinalityOf(a);

		return Math.round(sum);
	}
}
