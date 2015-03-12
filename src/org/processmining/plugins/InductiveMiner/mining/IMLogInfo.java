package org.processmining.plugins.InductiveMiner.mining;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class IMLogInfo {

	protected final Dfg dfg;

	protected final MultiSet<XEventClass> activities;

	protected final Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	protected final TObjectIntHashMap<XEventClass> minimumSelfDistances;
	
	protected final long numberOfEvents;
	protected final long numberOfEpsilonTraces;

	public IMLogInfo(Dfg directlyFollowsGraph, MultiSet<XEventClass> activities,
			Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			TObjectIntHashMap<XEventClass> minimumSelfDistances, long numberOfEvents, long numberOfEpsilonTraces) {
		this.dfg = directlyFollowsGraph;
		this.activities = activities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfEvents = numberOfEvents;
		this.numberOfEpsilonTraces = numberOfEpsilonTraces;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("start activities: " + dfg.getStartActivities() + "\n");
		result.append("end activities: " + dfg.getEndActivities());
		return result.toString();
	}
	
	public Dfg getDfg() {
		return dfg;
	}

	public MultiSet<XEventClass> getActivities() {
		return activities;
	}

	public Graph<XEventClass> getDirectlyFollowsGraph() {
		return dfg.getDirectlyFollowsGraph();
	}

	public MultiSet<XEventClass> getStartActivities() {
		return dfg.getStartActivities();
	}

	public MultiSet<XEventClass> getEndActivities() {
		return dfg.getEndActivities();
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

	public long getOccurrencesOfMostOccurringStartActivity() {
		return dfg.getStartActivities().getCardinalityOf(dfg.getStartActivities().getElementWithHighestCardinality());
	}

	public long getOccurrencesOfMostOccurringEndActivity() {
		return dfg.getEndActivities().getCardinalityOf(dfg.getEndActivities().getElementWithHighestCardinality());
	}
}
