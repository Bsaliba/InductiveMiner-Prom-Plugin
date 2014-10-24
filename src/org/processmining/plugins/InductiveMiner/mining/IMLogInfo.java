package org.processmining.plugins.InductiveMiner.mining;

import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class IMLogInfo extends IMLogInfoG<XEventClass> {

	public IMLogInfo(MultiSet<? extends IMTraceG<XEventClass>> log) {
		super(XEventClass.class, log);
	}

	public IMLogInfo(Graph<XEventClass> directlyFollowsGraph,
			Graph<XEventClass> directlyFollowsTransitiveClosureGraph,
			MultiSet<XEventClass> activities, MultiSet<XEventClass> startActivities,
			MultiSet<XEventClass> endActivities,
			Map<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			Map<XEventClass, Integer> minimumSelfDistances, long numberOfEvents, long numberOfEpsilonTraces,
			long lengthStrongestTrace, long strongestDirectEdge, XEventClass mostOccurringStartActivity,
			XEventClass mostOccurringEndActivity) {
		super(directlyFollowsGraph, directlyFollowsTransitiveClosureGraph, activities,
				startActivities, endActivities, minimumSelfDistancesBetween, minimumSelfDistances, numberOfEvents,
				numberOfEpsilonTraces, lengthStrongestTrace, strongestDirectEdge, mostOccurringStartActivity,
				mostOccurringEndActivity);
	}
}
