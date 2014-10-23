package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;

public class CutFinderIMinInfo {
	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
	private final Graph<XEventClass> graph;
	private final Graph<XEventClass> transitiveGraph;
	private final HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	private final Probabilities probabilities;
	private final JobList jobList;
	private final boolean debug;

	/**
	 * A CutFinderIMinInfo keeps track of a single call to the IMin cut finder.
	 * 
	 * @param startActivities
	 * @param endActivities
	 * @param graph2
	 * @param transitiveGraph2
	 * @param probabilities
	 * @param debug
	 */
	public CutFinderIMinInfo(MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities,
			Graph<XEventClass> graph2,
			Graph<XEventClass> transitiveGraph2,
			HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween, Probabilities probabilities,
			JobList jobList, boolean debug) {
		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.graph = graph2;
		this.transitiveGraph = transitiveGraph2;
		this.minimumSelfDistancesBetween = minimumSelfDistancesBetween;
		this.probabilities = probabilities;
		this.jobList = jobList;
		this.debug = debug;
	}

	public MultiSet<XEventClass> getStartActivities() {
		return startActivities;
	}

	public MultiSet<XEventClass> getEndActivities() {
		return endActivities;
	}

	public Graph<XEventClass> getGraph() {
		return graph;
	}

	public Graph<XEventClass> getTransitiveGraph() {
		return transitiveGraph;
	}

	public Probabilities getProbabilities() {
		return probabilities;
	}

	public boolean isDebug() {
		return debug;
	}

	public Set<XEventClass> getActivities() {
		return new HashSet<>(Arrays.asList(graph.getVertices()));
	}

	public MultiSet<XEventClass> getMinimumSelfDistanceBetween(XEventClass activity) {
		if (minimumSelfDistancesBetween == null) {
			throw new RuntimeException("Minimum self distances are not available.");
		}
		if (!minimumSelfDistancesBetween.containsKey(activity)) {
			return new MultiSet<XEventClass>();
		}
		return minimumSelfDistancesBetween.get(activity);
	}

	public JobList getJobList() {
		return jobList;
	}
}
