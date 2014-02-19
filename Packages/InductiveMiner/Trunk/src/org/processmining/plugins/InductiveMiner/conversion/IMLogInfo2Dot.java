package org.processmining.plugins.InductiveMiner.conversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.graphviz.colourMaps.ColourMaps;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;

public class IMLogInfo2Dot {
	public static Dot toDot(IMLogInfo logInfo, boolean useEventuallyFollows, Collection<Set<XEventClass>> cut) {

		final DefaultDirectedWeightedGraph<XEventClass, DefaultWeightedEdge> graph;
		if (!useEventuallyFollows) {
			graph = logInfo.getDirectlyFollowsGraph();
		} else {
			graph = logInfo.getEventuallyFollowsGraph();
		}
		
		Dot dot = new Dot();

		//prepare the nodes
		HashMap<XEventClass, DotNode> activityToNode = new HashMap<XEventClass, DotNode>();
		for (Set<XEventClass> branch : cut) {
			//dot += "subgraph \"cluster_" + UUID.randomUUID().toString() + "\" {\n";
			for (XEventClass activity : branch) {
				DotNode node = dot.addNode(activity.toString());
				activityToNode.put(activity, node);
				
				String options = "shape=\"box\"";

				//determine node colour using start and end activities
				if (logInfo.getStartActivities().contains(activity) && logInfo.getEndActivities().contains(activity)) {
					options += ", style=\"filled\"" + ", fillcolor=\""
							+ ColourMaps.colourMapGreen(logInfo.getStartActivities().getCardinalityOf(activity), logInfo.getStrongestStartActivity())
							+ ":" + ColourMaps.colourMapRed(logInfo.getEndActivities().getCardinalityOf(activity), logInfo.getStrongestEndActivity())
							+ "\"";
				} else if (logInfo.getStartActivities().contains(activity)) {
					options += ", style=\"filled\"" + ", fillcolor=\""
							+ ColourMaps.colourMapGreen(logInfo.getStartActivities().getCardinalityOf(activity), logInfo.getStrongestStartActivity())
							+ ":white\"";
				} else if (logInfo.getEndActivities().contains(activity)) {
					options += ", style=\"filled\"" + ", fillcolor=\"white:"
							+ ColourMaps.colourMapRed(logInfo.getEndActivities().getCardinalityOf(activity), logInfo.getStrongestEndActivity()) + "\"";
				}
				
				node.setOptions(options);
			}
		}

		//add the edges
		for (DefaultWeightedEdge edge : graph.edgeSet()) {
			XEventClass from = graph.getEdgeSource(edge);
			XEventClass to = graph.getEdgeTarget(edge);
			int weight = (int) graph.getEdgeWeight(edge);
			
			DotNode source = activityToNode.get(from);
			DotNode target = activityToNode.get(to);
			String label = String.valueOf(weight);
			String options = "color=\"" + ColourMaps.colourMapBlackBody(weight, logInfo.getStrongestDirectEdge()) + "\"";
			
			dot.addEdge(source,target,label,options);
		}

		return dot;
	}

	public static Dot toDot(IMLogInfo logInfo, boolean useEventuallyFollows) {
		Set<Set<XEventClass>> cut = new HashSet<Set<XEventClass>>();
		cut.add(logInfo.getActivities().toSet());
		return toDot(logInfo, useEventuallyFollows, cut);
	}
}
