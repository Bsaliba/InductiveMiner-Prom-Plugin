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
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIMParallel;

public class CutFinderIMi implements CutFinder {

	private static CutFinder cutFinderIM = new CutFinderIM();
	private static CutFinder cutFinderIMParallel = new CutFinderIMParallel();

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
		MultiSet<XEventClass> filteredStartActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : logInfo.getStartActivities()) {
			if (logInfo.getStartActivities().getCardinalityOf(activity) >= logInfo.getStrongestStartActivity()
					* threshold) {
				filteredStartActivities.add(activity, logInfo.getStartActivities().getCardinalityOf(activity));
			}
		}

		//filter end activities
		MultiSet<XEventClass> filteredEndActivities = new MultiSet<XEventClass>();
		for (XEventClass activity : logInfo.getEndActivities()) {
			if (logInfo.getEndActivities().getCardinalityOf(activity) >= logInfo.getStrongestEndActivity() * threshold) {
				filteredEndActivities.add(activity, logInfo.getEndActivities().getCardinalityOf(activity));
			}
		}

		//filter directly-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredDirectlyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : logInfo.getActivities()) {
			filteredDirectlyFollowsGraph.addVertex(activity);
		}
		//add edges

		/*
		 * //method 1: global threshold for (DefaultWeightedEdge edge :
		 * directlyFollowsGraph.edgeSet()) { if
		 * (directlyFollowsGraph.getEdgeWeight(edge) >= strongestDirectEdge *
		 * threshold) { XEventClass from =
		 * directlyFollowsGraph.getEdgeSource(edge); XEventClass to =
		 * directlyFollowsGraph.getEdgeTarget(edge); DefaultWeightedEdge
		 * filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
		 * filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge,
		 * directlyFollowsGraph.getEdgeWeight(edge)); } }
		 */

		//method 2: local threshold
		for (XEventClass activity : logInfo.getActivities()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = logInfo.getEndActivities().getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) logInfo.getDirectlyFollowsGraph().getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().outgoingEdgesOf(activity)) {
				if (logInfo.getDirectlyFollowsGraph().getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = logInfo.getDirectlyFollowsGraph().getEdgeSource(edge);
					XEventClass to = logInfo.getDirectlyFollowsGraph().getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredDirectlyFollowsGraph.addEdge(from, to);
					filteredDirectlyFollowsGraph.setEdgeWeight(filteredEdge, logInfo.getDirectlyFollowsGraph()
							.getEdgeWeight(edge));
				}
			}
		}

		//filter eventually-follows graph
		DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> filteredEventuallyFollowsGraph = new DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//add nodes
		for (XEventClass activity : logInfo.getEventuallyFollowsGraph().vertexSet()) {
			filteredEventuallyFollowsGraph.addVertex(activity);
		}
		//add edges
		/*
		 * //method 1: global threshold for (DefaultWeightedEdge edge :
		 * eventuallyFollowsGraph.edgeSet()) { if
		 * (eventuallyFollowsGraph.getEdgeWeight(edge) >= strongestEventualEdge
		 * * threshold) { XEventClass from =
		 * eventuallyFollowsGraph.getEdgeSource(edge); XEventClass to =
		 * eventuallyFollowsGraph.getEdgeTarget(edge); DefaultWeightedEdge
		 * filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
		 * filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge,
		 * eventuallyFollowsGraph.getEdgeWeight(edge)); } }
		 */

		//method 2: local threshold
		for (XEventClass activity : logInfo.getActivities()) {
			//find the maximum outgoing weight of this node
			Integer maxWeightOut = logInfo.getEndActivities().getCardinalityOf(activity);
			for (DefaultWeightedEdge edge : logInfo.getEventuallyFollowsGraph().outgoingEdgesOf(activity)) {
				maxWeightOut = Math.max(maxWeightOut, (int) logInfo.getEventuallyFollowsGraph().getEdgeWeight(edge));
			}

			//add all edges that are strong enough
			for (DefaultWeightedEdge edge : logInfo.getEventuallyFollowsGraph().outgoingEdgesOf(activity)) {
				if (logInfo.getEventuallyFollowsGraph().getEdgeWeight(edge) >= maxWeightOut * threshold) {
					XEventClass from = logInfo.getEventuallyFollowsGraph().getEdgeSource(edge);
					XEventClass to = logInfo.getEventuallyFollowsGraph().getEdgeTarget(edge);
					DefaultWeightedEdge filteredEdge = filteredEventuallyFollowsGraph.addEdge(from, to);
					filteredEventuallyFollowsGraph.setEdgeWeight(filteredEdge, logInfo.getEventuallyFollowsGraph()
							.getEdgeWeight(edge));
				}
			}
		}

		return new IMLogInfo(filteredDirectlyFollowsGraph, filteredEventuallyFollowsGraph,
				TransitiveClosure.transitiveClosure(filteredDirectlyFollowsGraph), logInfo.getActivities().copy(),
				filteredStartActivities, filteredEndActivities, logInfo.getMinimumSelfDistancesBetween(),
				logInfo.getMinimumSelfDistances(), logInfo.getNumberOfTraces(), logInfo.getNumberOfEvents(),
				logInfo.getNumberOfEpsilonTraces(), logInfo.getLongestTrace(), logInfo.getLengthStrongestTrace(),
				logInfo.getStrongestDirectEdge(), logInfo.getStrongestEventualEdge(),
				logInfo.getStrongestStartActivity(), logInfo.getStrongestEndActivity());
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
				logInfo.getNumberOfTraces(), logInfo.getNumberOfEvents(), logInfo.getNumberOfEpsilonTraces(),
				logInfo.getLongestTrace(), logInfo.getLengthStrongestTrace(), logInfo.getStrongestDirectEdge(),
				logInfo.getStrongestEventualEdge(), logInfo.getStrongestStartActivity(),
				logInfo.getStrongestEndActivity());
	}
}