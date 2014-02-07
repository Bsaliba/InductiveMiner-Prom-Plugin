package org.processmining.plugins.InductiveMiner.mining;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultWeightedEdge;

@Deprecated
public class DirectlyFollowsRelation extends IMLogInfo {

	@Deprecated
	public DirectlyFollowsRelation(IMLog log) {
		super(log);
	}

	public String debugGraph() {
		String result = "nodes: " + directlyFollowsGraph.vertexSet().toString();

		result += "\nstart nodes: ";
		for (XEventClass event : startActivities) {
			result += event + " (" + startActivities.getCardinalityOf(event) + ") ";
		}

		result += "\nend nodes: ";
		for (XEventClass event : endActivities) {
			result += event + " (" + endActivities.getCardinalityOf(event) + ") ";
		}

		result += "\nedges: ";
		for (DefaultWeightedEdge edge : directlyFollowsGraph.edgeSet()) {
			result += "\t(" + directlyFollowsGraph.getEdgeSource(edge);
			result += " => " + directlyFollowsGraph.getEdgeTarget(edge) + " "
					+ ((int) directlyFollowsGraph.getEdgeWeight(edge)) + ") ";
		}

		return result;
	}

}
