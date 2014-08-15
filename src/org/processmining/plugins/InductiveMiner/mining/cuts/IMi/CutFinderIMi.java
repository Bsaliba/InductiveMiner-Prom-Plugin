package org.processmining.plugins.InductiveMiner.mining.cuts.IMi;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.TransitiveClosure;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;

public class CutFinderIMi implements CutFinder {

	private static CutFinder cutFinderIM = new CutFinderIM();

	//	private static CutFinder cutFinderIMParallel = new CutFinderIMParallel();

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		//filter logInfo
		IMLogInfo logInfoFiltered = filterNoise(logInfo, minerState.parameters.getNoiseThreshold());

		//call IM cut detection
		Cut cut = cutFinderIM.findCut(null, logInfoFiltered, minerState);

		//if (cut != null && cut.isValid()) {
		return cut;
		//}

		//try to add incomplete edges
		//disabled as it does not keep logInfo in good shape
		//IMLogInfo logInfoAddedEdges = addIncompleteEdges(logInfo, parameters.getNoiseThreshold());
		//return cutFinderIMParallel.findCut(log, logInfoAddedEdges, parameters);
	}

	/*
	 * filter noise
	 */

	public static IMLogInfo filterNoise(IMLogInfo logInfo, float threshold) {
		//filter start activities
		MultiSet<XEventClass> filteredStartActivities = filterActivities(logInfo.getStartActivities(), threshold);

		//filter end activities
		MultiSet<XEventClass> filteredEndActivities = filterActivities(logInfo.getEndActivities(), threshold);

		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = filterGraph(
				logInfo.getDirectlyFollowsGraph(), filteredEndActivities, threshold);

		//filter eventually-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = filterGraph(
				logInfo.getEventuallyFollowsGraph(), filteredEndActivities, threshold);

		return new IMLogInfo(filteredDirectlyFollowsGraph, filteredEventuallyFollowsGraph,
				TransitiveClosure.transitiveClosure(filteredDirectlyFollowsGraph), logInfo.getActivities().copy(),
				filteredStartActivities, filteredEndActivities, logInfo.getMinimumSelfDistancesBetween(),
				logInfo.getMinimumSelfDistances(), logInfo.getNumberOfEvents(), logInfo.getNumberOfEpsilonTraces(),
				logInfo.getHighestTraceCardinality(), logInfo.getOccurencesOfMostOccuringDirectEdge(),
				logInfo.getMostOccurringStartActivity(), logInfo.getMostOccurringEndActivity());
	}

	/**
	 * Filter a graph. Only keep the edges that occur often enough, compared
	 * with other outgoing edges of the source. 0 <= threshold <= 1.
	 * 
	 * @param graph
	 * @param threshold
	 * @return
	 */
	public static DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filterGraph(
			DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph, MultiSet<XEventClass> endActivities,
			float threshold) {
		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filtered = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		//add nodes
		for (XEventClass activity : graph.vertexSet()) {
			filtered.addVertex(activity);
		}

		//add edges
		for (XEventClass activity : graph.vertexSet()) {
			//find the maximum outgoing weight of this node
			long maxWeightOut = endActivities.getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) graph.getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(activity)) {
				if (graph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = graph.getEdgeSource(edge);
					XEventClass to = graph.getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filtered.addEdge(from, to);
					filtered.setEdgeWeight(filteredEdge, graph.getEdgeWeight(edge));
				}
			}
		}
		return filtered;
	}

	/*
	 * Try to guess new parallel edges
	 */

	public static IMLogInfo addIncompleteEdges(IMLogInfo logInfo, float threshold) {

		//debug("add incomplete edges");

		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		//add nodes
		for (XEventClass activity : logInfo.getActivities()) {
			filteredDirectlyFollowsGraph.addVertex(activity);
		}

		//add edges
		for (XEventClass activity : logInfo.getActivities()) {

			//add all outgoing edges of this node that are already present
			for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().outgoingEdgesOf(activity)) {
				XEventClass from = logInfo.getDirectlyFollowsGraph().getEdgeSource(edge);
				XEventClass to = logInfo.getDirectlyFollowsGraph().getEdgeTarget(edge);
				DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
				double weight = logInfo.getDirectlyFollowsGraph().getEdgeWeight(edge);
				filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, weight);

				//see if this edge is weak enough to justify adding the reversed edge
				//debug(" check edge " + from + " -> " + to + " weight " + weight + " expected reverse weight " + Math.exp(1 / (1-threshold)));
				if (threshold != 0 && (threshold == 1 || weight < Math.exp(1 / (1 - threshold)))) {
					//if the reversed edge is not present, add it
					if (!logInfo.getDirectlyFollowsGraph().containsEdge(to, from)) {
						//debug("  add edge " + to + " -> " + from);
						DefaultWeightedEdge reversedEdge = filteredDirectlyFollowsGraph.addEdge(to, from);
						filteredDirectlyFollowsGraph.setEdgeWeight(reversedEdge, 1);
					} else {
						//debug("  edge " + to + " -> " + from + " exists already");
					}
				}
			}

		}

		return new IMLogInfo(filteredDirectlyFollowsGraph, null,
				TransitiveClosure.transitiveClosure(filteredDirectlyFollowsGraph), logInfo.getActivities().copy(),
				logInfo.getStartActivities().copy(), logInfo.getEndActivities().copy(),
				logInfo.getMinimumSelfDistancesBetween(), logInfo.getMinimumSelfDistances(),
				logInfo.getNumberOfEvents(), logInfo.getNumberOfEpsilonTraces(), logInfo.getHighestTraceCardinality(),
				logInfo.getOccurencesOfMostOccuringDirectEdge(), logInfo.getMostOccurringStartActivity(),
				logInfo.getMostOccurringEndActivity());
	}

	/**
	 * Filter start or end activities. Only keep those occurring more times than
	 * threshold * the most occurring activity. 0 <= threshold <= 1.
	 * 
	 * @param activities
	 * @param threshold
	 * @return
	 */
	public static MultiSet<XEventClass> filterActivities(MultiSet<XEventClass> activities, float threshold) {
		long max = activities.getCardinalityOf(activities.getElementWithHighestCardinality());
		MultiSet<XEventClass> filtered = new MultiSet<XEventClass>();
		for (XEventClass activity : activities) {
			if (activities.getCardinalityOf(activity) >= threshold * max) {
				filtered.add(activity, activities.getCardinalityOf(activity));
			}
		}
		return filtered;
	}
}