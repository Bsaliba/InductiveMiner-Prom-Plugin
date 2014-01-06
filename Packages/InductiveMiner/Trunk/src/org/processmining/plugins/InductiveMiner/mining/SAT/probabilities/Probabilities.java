package org.processmining.plugins.InductiveMiner.mining.SAT.probabilities;

import java.math.BigInteger;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public abstract class Probabilities {

	public abstract double getProbabilityXor(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public abstract double getProbabilitySequence(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public abstract double getProbabilityParallel(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);

	public abstract double getProbabilityLoopSingle(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopDouble(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopIndirect(DirectlyFollowsRelation relation, XEventClass a, XEventClass b);
	
	public abstract String toString();

	public final int doubleToIntFactor = 100000;

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

	public BigInteger getProbabilityLoopSingleB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopSingle(relation, a, b));
	}
	
	public BigInteger getProbabilityLoopDoubleB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopDouble(relation, a, b));
	}
	
	public BigInteger getProbabilityLoopIndirectB(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopIndirect(relation, a, b));
	}

	protected long getActivityCount(XEventClass a, DirectlyFollowsRelation relation) {
		//count how often each activity occurs
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		double sum = 0;
		for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(a)) {
			sum += graph.getEdgeWeight(edge);
		}
		sum += relation.getEndActivities().getCardinalityOf(a);

		return Math.round(sum);
	}

	//Directly follows
	protected boolean D(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		return graph.containsEdge(a, b);
	}

	//Eventually follows
	protected boolean E(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		//DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getEventuallyFollowsGraph();
		DefaultDirectedGraph<XEventClass, DefaultEdge> graph = relation.getDirectlyFollowsTransitiveClosureGraph();
		return graph.containsEdge(a, b);
	}

	protected double z(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return (getActivityCount(a, relation) + getActivityCount(b, relation)) / 2.0;
	}

	protected double w(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		return relation.getMinimumSelfDistanceBetween(a).getCardinalityOf(b)
				+ relation.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}

	protected double x(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		DefaultWeightedEdge edge = graph.getEdge(a, b);
		return graph.getEdgeWeight(edge);
	}
	
	protected boolean noSEinvolvedInMsd(DirectlyFollowsRelation relation, XEventClass a, XEventClass b) {
		Set<XEventClass> SE = Sets.union(relation.getStartActivities().toSet(), relation.getEndActivities().toSet());
		if (w(relation,a,b) > 0 && !SE.contains(a) && !SE.contains(b)) {
			Set<XEventClass> SEmMSD = Sets.intersection(SE, relation.getMinimumSelfDistanceBetween(a).toSet());
			if (SEmMSD.size() == 0) {
				return true;
			}
		}
		return false;
	}
}
