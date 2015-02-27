package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog2;

public class CutFinderIMLoop implements CutFinder, DfgCutFinder {

	public Cut findCut(IMLog2 log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getStartActivities(), logInfo.getEndActivities(), logInfo.getDirectlyFollowsGraph());
	}

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		return findCut(dfg.getStartActivities(), dfg.getEndActivities(), dfg.getDirectlyFollowsGraph());
	}

	public static Cut findCut(MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities,
			Graph<XEventClass> graph) {
		//initialise the start and end activities as a connected component
		Map<XEventClass, Integer> connectedComponents = new THashMap<XEventClass, Integer>();
		for (XEventClass startActivity : startActivities.toSet()) {
			connectedComponents.put(startActivity, 0);
		}
		for (XEventClass endActivity : endActivities.toSet()) {
			connectedComponents.put(endActivity, 0);
		}

		//find the other connected components
		Integer ccs = 1;
		for (XEventClass node : graph.getVertices()) {
			if (!connectedComponents.containsKey(node)) {
				labelConnectedComponents(graph, node, connectedComponents, ccs);
				ccs += 1;
			}
		}

		//find the start activities of each component
		TIntObjectMap<Set<XEventClass>> subStartActivities = new TIntObjectHashMap<Set<XEventClass>>();
		for (Integer cc = 0; cc < ccs; cc++) {
			subStartActivities.put(cc, new THashSet<XEventClass>());
		}
		for (XEventClass node : graph.getVertices()) {
			Integer cc = connectedComponents.get(node);
			for (long edge : graph.getIncomingEdgesOf(node)) {
				if (cc != connectedComponents.get(graph.getEdgeSource(edge))) {
					//this is a start activity
					Set<XEventClass> start = subStartActivities.get(cc);
					start.add(node);
					subStartActivities.put(cc, start);
				}
			}
		}

		//find the end activities of each component
		TIntObjectMap<Set<XEventClass>> subEndActivities = new TIntObjectHashMap<Set<XEventClass>>();
		for (Integer cc = 0; cc < ccs; cc++) {
			subEndActivities.put(cc, new THashSet<XEventClass>());
		}
		for (XEventClass node : graph.getVertices()) {
			Integer cc = connectedComponents.get(node);
			for (long edge : graph.getOutgoingEdgesOf(node)) {
				if (cc != connectedComponents.get(graph.getEdgeTarget(edge))) {
					//this is an end activity
					Set<XEventClass> end = subEndActivities.get(cc);
					end.add(node);
					subEndActivities.put(cc, end);
				}
			}
		}

		//initialise the candidates
		Boolean[] candidates = new Boolean[ccs];
		//the start and end activities are no candidates
		candidates[0] = false;
		for (int i = 1; i < ccs; i++) {
			candidates[i] = true;
		}

		//exclude all candidates that are reachable from the start activities (that are not an end activity)
		for (XEventClass startActivity : startActivities.toSet()) {
			if (!endActivities.contains(startActivity)) {
				for (long edge : graph.getOutgoingEdgesOf(startActivity)) {
					candidates[connectedComponents.get(graph.getEdgeTarget(edge))] = false;
				}
			}
		}

		//exclude all candidates that can reach an end activity (which is not a start activity)
		for (XEventClass endActivity : endActivities.toSet()) {
			if (!startActivities.contains(endActivity)) {
				for (long edge : graph.getIncomingEdgesOf(endActivity)) {
					candidates[connectedComponents.get(graph.getEdgeSource(edge))] = false;
				}
			}
		}

		//exclude all candidates that have no connection to all start activities
		for (Integer cc = 0; cc < ccs; cc++) {
			Set<XEventClass> end = subEndActivities.get(cc);
			for (XEventClass node1 : end) {
				for (XEventClass node2 : startActivities.toSet()) {
					if (!graph.containsEdge(node1, node2)) {
						candidates[cc] = false;
						//debug("body part for no connection to all start activities " + cc.toString());
					}
				}
			}
		}

		//exclude all candidates that have no connection from all end activities
		for (Integer cc = 0; cc < ccs; cc++) {
			Set<XEventClass> start = subStartActivities.get(cc);
			for (XEventClass node1 : start) {
				for (XEventClass node2 : endActivities.toSet()) {
					if (!graph.containsEdge(node2, node1)) {
						candidates[cc] = false;
						//debug("body part for no connection from all end activities " + node2.toString() + " " + node1.toString());
					}
				}
			}
		}

		//make the lists of sets of nodes
		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		for (int i = 0; i < ccs; i++) {
			result.add(new HashSet<XEventClass>());
		}

		//divide the activities
		for (XEventClass node : graph.getVertices()) {
			//debug(node.toString() + " in connected component " + connectedComponents.get(node));
			int index;
			if (candidates[connectedComponents.get(node)]) {
				index = connectedComponents.get(node);
				//debug(", redo part of loop");
			} else {
				index = 0;
				//debug(", body part of loop");
			}
			Set<XEventClass> s = result.get(index);
			s.add(node);
			result.set(index, s);
		}

		//filter the empty sets
		List<Set<XEventClass>> result2 = new ArrayList<Set<XEventClass>>();
		for (Set<XEventClass> set : result) {
			if (set.size() > 0) {
				result2.add(set);
			}
		}

		//debug(result2.toString());
		return new Cut(Operator.loop, result2);
	}

	private static void labelConnectedComponents(Graph<XEventClass> graph,
			XEventClass node, Map<XEventClass, Integer> connectedComponents, Integer connectedComponent) {
		if (!connectedComponents.containsKey(node)) {
			connectedComponents.put(node, connectedComponent);
			for (long edge : graph.getEdgesOf(node)) {
				labelConnectedComponents(graph, graph.getEdgeSource(edge), connectedComponents, connectedComponent);
				labelConnectedComponents(graph, graph.getEdgeTarget(edge), connectedComponents, connectedComponent);
			}
		}
	}

}
