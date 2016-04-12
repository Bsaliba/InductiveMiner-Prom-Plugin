package org.processmining.plugins.InductiveMiner.mining;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;

public class IMLogInfo {

	protected final Dfg dfg;

	protected final MultiSet<XEventClass> activities;

	protected final Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	protected final TObjectIntHashMap<XEventClass> minimumSelfDistances;

	protected final long numberOfEvents;
	protected final long numberOfActivityInstances;
	protected final long numberOfEpsilonTraces;

	public IMLogInfo(Dfg directlyFollowsGraph, MultiSet<XEventClass> activities,
			Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			TObjectIntHashMap<XEventClass> minimumSelfDistances, long numberOfEvents, long numberOfActivityInstances,
			long numberOfEpsilonTraces) {
		this.dfg = directlyFollowsGraph;
		this.activities = activities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfEvents = numberOfEvents;
		this.numberOfActivityInstances = numberOfActivityInstances;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
	}

	public Dfg getDfg() {
		return dfg;
	}

	public MultiSet<XEventClass> getActivities() {
		return activities;
	}

	public Map<XEventClass, MultiSet<XEventClass>> getMinimumSelfDistancesBetween() {
		return minimumSelfDistancesBetween;
	}

	public MultiSet<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (!minimumSelfDistances.containsKey(activity)) {
			return new MultiSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public TObjectIntHashMap<XEventClass> getMinimumSelfDistances() {
		return minimumSelfDistances;
	}

	public int getMinimumSelfDistance(XEventClass a) {
		if (minimumSelfDistances.containsKey(a)) {
			return minimumSelfDistances.get(a);
		}
		return 0;
	}

	public long getNumberOfEvents() {
		return numberOfEvents;
	}

	public long getNumberOfActivityInstances() {
		return numberOfActivityInstances;
	}

	/**
	 * Gives the number of empty traces in the log.
	 * 
	 * @return
	 */
	public long getNumberOfEpsilonTraces() {
		return numberOfEpsilonTraces;
	}

	/**
	 * Gives the number of times the trace that occurs the most occurs in the
	 * log.
	 * 
	 * @return
	 */
	//	public long getHighestTraceCardinality() {
	//		return highestTraceCardinality;
	//	}

	/**
	 * Gives the number of times the directed edge that occurs the most occurs
	 * in the log.
	 * 
	 * @return
	 */
	public long getOccurencesOfMostOccuringDirectEdge() {
		return dfg.getDirectlyFollowsGraph().getWeightOfHeaviestEdge();
	}
}
