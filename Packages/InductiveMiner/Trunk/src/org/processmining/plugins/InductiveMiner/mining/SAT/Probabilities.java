package org.processmining.plugins.InductiveMiner.mining.SAT;

import java.math.BigInteger;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.DirectlyFollowsRelation;

public abstract class Probabilities {

	protected DirectlyFollowsRelation relation;

	public Probabilities(DirectlyFollowsRelation relation) {
		this.relation = relation;
	}

	public abstract double getProbabilityXor(XEventClass a, XEventClass b);

	public abstract double getProbabilitySequence(XEventClass a, XEventClass b);

	public abstract double getProbabilityParallel(XEventClass a, XEventClass b);

	public abstract double getProbabilityLoop(XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopSingle(XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopDouble(XEventClass a, XEventClass b);

	public final int doubleToIntFactor = 1000;
	
	public void setDirectlyFollowsRelation(DirectlyFollowsRelation relation) {
		this.relation = relation;
	}

	public BigInteger toBigInt(double probability) {
		return BigInteger.valueOf(Math.round(doubleToIntFactor * probability));
	}

	public BigInteger getProbabilityXorB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityXor(a, b));
	}

	public BigInteger getProbabilitySequenceB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilitySequence(a, b));
	}

	public BigInteger getProbabilityParallelB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityParallel(a, b));
	}

	public BigInteger getProbabilityLoopB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoop(a, b));
	}
	
	public BigInteger getProbabilityLoopSingleB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopSingle(a, b));
	}
	
	public BigInteger getProbabilityLoopDoubleB(XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopDouble(a, b));
	}

	protected long getActivityCount(XEventClass a) {
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
	protected boolean D(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		return graph.containsEdge(a, b);
	}

	//Eventually follows
	protected boolean E(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getEventuallyFollowsGraph();
		return graph.containsEdge(a, b);
	}

	protected double z(XEventClass a, XEventClass b) {
		return (getActivityCount(a) + getActivityCount(b)) / 2.0;
	}

	protected double w(XEventClass a, XEventClass b) {
		return relation.getMinimumSelfDistanceBetween(a).getCardinalityOf(b)
				+ relation.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}

	protected double x(XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getDirectlyFollowsGraph();
		DefaultWeightedEdge edge = graph.getEdge(a, b);
		return graph.getEdgeWeight(edge);
	}
}
