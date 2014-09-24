package org.processmining.plugins.InductiveMiner.mining.cuts.IMin;

import java.util.HashMap;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.jobList.JobList;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMin.probabilities.Probabilities;

public class CutFinderIMinInfo {
	private final MultiSet<XEventClass> startActivities;
	private final MultiSet<XEventClass> endActivities;
	private final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph;
	private final DefaultDirectedGraph<XEventClass, DefaultEdge> transitiveGraph;
	private final HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween;
	private final Probabilities probabilities;
	private final JobList jobList;
	private final boolean debug;

	/**
	 * A CutFinderIMinInfo keeps track of a single call to the IMin cut finder.
	 * 
	 * @param startActivities
	 * @param endActivities
	 * @param graph
	 * @param transitiveGraph
	 * @param probabilities
	 * @param debug
	 */
	public CutFinderIMinInfo(MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities,
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph,
			DefaultDirectedGraph<XEventClass, DefaultEdge> transitiveGraph,
			HashMap<XEventClass, MultiSet<XEventClass>> minimumSelfDistancesBetween, Probabilities probabilities,
			JobList jobList, boolean debug) {
		this.startActivities = startActivities;
		this.endActivities = endActivities;
		this.graph = graph;
		this.transitiveGraph = transitiveGraph;
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

	public DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	public DefaultDirectedGraph<XEventClass, DefaultEdge> getTransitiveGraph() {
		return transitiveGraph;
	}

	public Probabilities getProbabilities() {
		return probabilities;
	}

	public boolean isDebug() {
		return debug;
	}

	public Set<XEventClass> getActivities() {
		return graph.vertexSet();
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
