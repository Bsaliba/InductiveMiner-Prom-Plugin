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

	public IMLogInfo(Dfg directlyFollowsGraph, MultiSet<XEventClass> activities,
			Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			TObjectIntHashMap<XEventClass> minimumSelfDistances, long numberOfEvents, long numberOfActivityInstances) {
		this.dfg = directlyFollowsGraph;
		this.activities = activities;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.minimumSelfDistances = minimumSelfDistances;
		this.numberOfEvents = numberOfEvents;
		this.numberOfActivityInstances = numberOfActivityInstances;
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

}
