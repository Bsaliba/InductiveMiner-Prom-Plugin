package org.processmining.plugins.InductiveMiner.conversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;

public class IMLogInfo2Dot {
	public static Dot toDot(IMLogInfo logInfo, boolean useEventuallyFollows, Collection<Set<XEventClass>> cut) {

		final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph;
		if (!useEventuallyFollows) {
			graph = logInfo.getDirectlyFollowsGraph();
		} else {
			graph = logInfo.getEventuallyFollowsGraph();
		}

		String dot = "digraph G {\n";
		dot += "rankdir=LR;\n";

		//prepare the nodes
		HashMap<XEventClass, String> activityToNode = new HashMap<XEventClass, String>();
		for (Set<XEventClass> branch : cut) {
			//dot += "subgraph \"cluster_" + UUID.randomUUID().toString() + "\" {\n";
			for (XEventClass activity : branch) {
				String id = UUID.randomUUID().toString();
				activityToNode.put(activity, id);
				dot += "\"" + id + "\" [ label=\"" + activity.toString() + "\", shape=\"box\"";

				//determine node colour using start and end activities
				if (logInfo.getStartActivities().contains(activity) && logInfo.getEndActivities().contains(activity)) {
					dot += ", style=\"filled\"" + ", fillcolor=\""
							+ ColourMaps.colourMapGreen(logInfo.getStartActivities().getCardinalityOf(activity), logInfo.getStrongestStartActivity())
							+ ":" + ColourMaps.colourMapRed(logInfo.getEndActivities().getCardinalityOf(activity), logInfo.getStrongestEndActivity())
							+ "\"";
				} else if (logInfo.getStartActivities().contains(activity)) {
					dot += ", style=\"filled\"" + ", fillcolor=\""
							+ ColourMaps.colourMapGreen(logInfo.getStartActivities().getCardinalityOf(activity), logInfo.getStrongestStartActivity())
							+ ":white\"";
				} else if (logInfo.getEndActivities().contains(activity)) {
					dot += ", style=\"filled\"" + ", fillcolor=\"white:"
							+ ColourMaps.colourMapRed(logInfo.getEndActivities().getCardinalityOf(activity), logInfo.getStrongestEndActivity()) + "\"";
				}

				dot += "];\n";
			}
		}

		//add the edges
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			XEventClass from = graph.getEdgeSource(edge);
			XEventClass to = graph.getEdgeTarget(edge);
			int weight = (int) graph.getEdgeWeight(edge);
			dot += "\"" + activityToNode.get(from) + "\" -> \"" + activityToNode.get(to) + "\" [";
			dot += "label=\"" + String.valueOf(weight) + "\"";
			dot += ", ";
			dot += "color=\"" + ColourMaps.colourMapBlackBody(weight, logInfo.getStrongestDirectEdge()) + "\"";
			dot += "];\n";
		}

		dot += "}\n";
		Dot dot2 = new Dot(dot);
		return dot2;
	}

	public static Dot toDot(IMLogInfo logInfo, boolean useEventuallyFollows) {
		Set<Set<XEventClass>> cut = new HashSet<Set<XEventClass>>();
		cut.add(logInfo.getActivities().toSet());
		return toDot(logInfo, useEventuallyFollows, cut);
	}
}
