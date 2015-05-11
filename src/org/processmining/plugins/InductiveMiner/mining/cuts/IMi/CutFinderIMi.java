package org.processmining.plugins.InductiveMiner.mining.cuts.IMi;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMi implements CutFinder {

	private static CutFinder cutFinderIM = new CutFinderIM();

	//	private static CutFinder cutFinderIMParallel = new CutFinderIMParallel();

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		//filter logInfo
		IMLogInfo logInfoFiltered = filterNoise(logInfo, minerState.parameters.getNoiseThreshold());

		//call IM cut detection
		Cut cut = cutFinderIM.findCut(null, logInfoFiltered, minerState);

		return cut;
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
		Graph<XEventClass> filteredDirectlyFollowsGraph = filterGraph(logInfo.getDirectlyFollowsGraph(),
				filteredEndActivities, threshold);

		//filter eventually-follows graph
		//		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = filterGraph(
		//				logInfo.getEventuallyFollowsGraph(), filteredEndActivities, threshold);

		//		return new IMLogInfo(filteredDirectlyFollowsGraph, filteredEventuallyFollowsGraph,
		//				TransitiveClosure.transitiveClosure(filteredDirectlyFollowsGraph), logInfo.getActivities().copy(),
		//				filteredStartActivities, filteredEndActivities, logInfo.getMinimumSelfDistancesBetween(),
		//				logInfo.getMinimumSelfDistances(), logInfo.getNumberOfEvents(), logInfo.getNumberOfEpsilonTraces(),
		//				logInfo.getHighestTraceCardinality(), logInfo.getOccurencesOfMostOccuringDirectEdge(),
		//				logInfo.getMostOccurringStartActivity(), logInfo.getMostOccurringEndActivity());

		Dfg dfg = new Dfg(filteredDirectlyFollowsGraph, null, null, null, null, filteredStartActivities,
				filteredEndActivities, null, null);

		return new IMLogInfo(dfg, logInfo.getActivities().copy(), logInfo.getMinimumSelfDistancesBetween(),
				logInfo.getMinimumSelfDistances(), logInfo.getNumberOfEvents(), logInfo.getNumberOfActivityInstances(),
				logInfo.getNumberOfEpsilonTraces());
	}

	/**
	 * Filter a graph. Only keep the edges that occur often enough, compared
	 * with other outgoing edges of the source. 0 <= threshold <= 1.
	 * 
	 * @param graph
	 * @param threshold
	 * @return
	 */
	public static Graph<XEventClass> filterGraph(Graph<XEventClass> graph, MultiSet<XEventClass> endActivities,
			float threshold) {
		//filter directly-follows graph
		Graph<XEventClass> filtered = GraphFactory.create(XEventClass.class, graph.getNumberOfVertices());

		//add nodes
		filtered.addVertices(graph.getVertices());

		//add edges
		for (XEventClass activity : graph.getVertices()) {
			//find the maximum outgoing weight of this node
			long maxWeightOut = endActivities.getCardinalityOf(activity);
			for (long edge : graph.getOutgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) graph.getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (long edge : graph.getOutgoingEdgesOf(activity)) {
				if (graph.getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = graph.getEdgeSource(edge);
					XEventClass to = graph.getEdgeTarget(edge);
					filtered.addEdge(from, to, graph.getEdgeWeight(edge));
				}
			}
		}
		return filtered;
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