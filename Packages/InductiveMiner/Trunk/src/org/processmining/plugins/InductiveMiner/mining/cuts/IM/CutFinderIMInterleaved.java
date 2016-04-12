package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.graphs.Components;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMInterleaved implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getDfg());
	}

	public Cut findCut(Dfg dfg) {
		Graph<XEventClass> graph = dfg.getDirectlyFollowsGraph();

		//put each activity in a component.
		Components<XEventClass> components = new Components<XEventClass>(graph.getVertices());

		/*
		 * By semantics of the interleaved operator, a non-start activity cannot
		 * have connections from other subtrees. Thus, walk over all such
		 * activities and merge components.
		 */
		for (int activityIndex : graph.getVertexIndices()) {
			if (!dfg.isStartActivity(activityIndex)) {
				for (long edgeIndex : graph.getIncomingEdgesOf(activityIndex)) {
					int source = graph.getEdgeSourceIndex(edgeIndex);
					components.mergeComponentsOf(source, activityIndex);
				}
			}
			if (!dfg.isEndActivity(activityIndex)) {
				for (long edgeIndex : graph.getOutgoingEdgesOf(activityIndex)) {
					int target = graph.getEdgeTargetIndex(edgeIndex);
					components.mergeComponentsOf(activityIndex, target);
				}
			}
		}

		/*
		 * All start activities need to be doubly connected from all end
		 * activities from other components. Thus, walk through the start
		 * activities and end activities and violating pairs. The reverse
		 * direction is implied.
		 */
		for (int startActivity : dfg.getStartActivityIndices()) {
			for (int endActivity : dfg.getEndActivityIndices()) {
				if (startActivity != endActivity) {
					if (!graph.containsEdge(endActivity, startActivity)) {
						components.mergeComponentsOf(startActivity, endActivity);
					}
				}
			}
		}

		return new Cut(Operator.interleaved, components.getComponents());
	}
}
