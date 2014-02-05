package org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities;

import java.math.BigInteger;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;

public abstract class Probabilities {

	public abstract double getProbabilityXor(LogInfo logInfo, XEventClass a, XEventClass b);

	public abstract double getProbabilitySequence(LogInfo logInfo, XEventClass a, XEventClass b);

	public abstract double getProbabilityParallel(LogInfo logInfo, XEventClass a, XEventClass b);

	public abstract double getProbabilityLoopSingle(LogInfo logInfo, XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopDouble(LogInfo logInfo, XEventClass a, XEventClass b);
	
	public abstract double getProbabilityLoopIndirect(LogInfo logInfo, XEventClass a, XEventClass b);
	
	public abstract String toString();

	public final int doubleToIntFactor = 100000;

	public BigInteger toBigInt(double probability) {
		return BigInteger.valueOf(Math.round(doubleToIntFactor * probability));
	}

	public BigInteger getProbabilityXorB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityXor(logInfo, a, b));
	}

	public BigInteger getProbabilitySequenceB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilitySequence(logInfo, a, b));
	}

	public BigInteger getProbabilityParallelB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityParallel(logInfo, a, b));
	}

	public BigInteger getProbabilityLoopSingleB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopSingle(logInfo, a, b));
	}
	
	public BigInteger getProbabilityLoopDoubleB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopDouble(logInfo, a, b));
	}
	
	public BigInteger getProbabilityLoopIndirectB(LogInfo logInfo, XEventClass a, XEventClass b) {
		return toBigInt(getProbabilityLoopIndirect(logInfo, a, b));
	}

	protected long getActivityCount(XEventClass a, LogInfo logInfo) {
		//count how often each activity occurs
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = logInfo.getDirectlyFollowsGraph();
		double sum = 0;
		for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(a)) {
			sum += graph.getEdgeWeight(edge);
		}
		sum += logInfo.getEndActivities().getCardinalityOf(a);

		return Math.round(sum);
	}

	//Directly follows
	protected boolean D(LogInfo logInfo, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = logInfo.getDirectlyFollowsGraph();
		return graph.containsEdge(a, b);
	}

	//Eventually follows
	protected boolean E(LogInfo logInfo, XEventClass a, XEventClass b) {
		//DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = relation.getEventuallyFollowsGraph();
		DefaultDirectedGraph<XEventClass, DefaultEdge> graph = logInfo.getDirectlyFollowsTransitiveClosureGraph();
		return graph.containsEdge(a, b);
	}

	protected double z(LogInfo logInfo, XEventClass a, XEventClass b) {
		return (getActivityCount(a, logInfo) + getActivityCount(b, logInfo)) / 2.0;
	}

	protected double w(LogInfo logInfo, XEventClass a, XEventClass b) {
		return logInfo.getMinimumSelfDistanceBetween(a).getCardinalityOf(b)
				+ logInfo.getMinimumSelfDistanceBetween(b).getCardinalityOf(a);
	}

	protected double x(LogInfo logInfo, XEventClass a, XEventClass b) {
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph = logInfo.getDirectlyFollowsGraph();
		DefaultWeightedEdge edge = graph.getEdge(a, b);
		return graph.getEdgeWeight(edge);
	}
	
	protected boolean noSEinvolvedInMsd(LogInfo logInfo, XEventClass a, XEventClass b) {
		Set<XEventClass> SE = Sets.union(logInfo.getStartActivities().toSet(), logInfo.getEndActivities().toSet());
		if (w(logInfo,a,b) > 0 && !SE.contains(a) && !SE.contains(b)) {
			Set<XEventClass> SEmMSD = Sets.intersection(SE, logInfo.getMinimumSelfDistanceBetween(a).toSet());
			if (SEmMSD.size() == 0) {
				return true;
			}
		}
		return false;
	}
}
