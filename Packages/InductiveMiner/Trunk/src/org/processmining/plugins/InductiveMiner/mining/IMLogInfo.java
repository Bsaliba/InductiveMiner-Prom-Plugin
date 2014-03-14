package org.processmining.plugins.InductiveMiner.mining;

import java.util.HashMap;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;

public class IMLogInfo extends IMLogInfoG<XEventClass> {

	public IMLogInfo(MultiSet<? extends IMTraceG<XEventClass>> log) {
		super(log);
	}
	
	public IMLogInfo(DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> directlyFollowsGraph,
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> eventuallyFollowsGraph,
			DefaultDirectedGraph<XEventClass, DefaultEdge> directlyFollowsTransitiveClosureGraph,
			MultiSet<XEventClass> activities, MultiSet<XEventClass> startActivities,
			MultiSet<XEventClass> endActivities,
			HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween,
			HashMap<XEventClass, Integer> minimumSelfDistances, long numberOfTraces, long numberOfEvents,
			long numberOfEpsilonTraces, int longestTrace, int lengthStrongestTrace, int strongestDirectEdge,
			int strongestEventualEdge, int strongestStartActivity, int strongestEndActivity) {
		super(directlyFollowsGraph, eventuallyFollowsGraph, directlyFollowsTransitiveClosureGraph, activities, startActivities,
				endActivities, minimumSelfDistancesBetween, minimumSelfDistances, numberOfTraces, numberOfEvents,
				numberOfEpsilonTraces, longestTrace, lengthStrongestTrace, strongestDirectEdge, strongestEventualEdge,
				strongestStartActivity, strongestEndActivity);
	}
}
