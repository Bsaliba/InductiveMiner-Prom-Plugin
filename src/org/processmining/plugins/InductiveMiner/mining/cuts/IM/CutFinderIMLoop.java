package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.processmining.plugins.InductiveMiner.mining.LogInfo;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.filteredLog.IMLog;

public class CutFinderIMLoop implements CutFinder {

	public Cut findCut(IMLog log, LogInfo logInfo, MiningParameters parameters) {
		//initialise the start and end activities as a connected component
		HashMap<XEventClass, Integer> connectedComponents = new HashMap<XEventClass, Integer>();
		for (XEventClass startActivity : logInfo.getStartActivities().toSet()) {
			connectedComponents.put(startActivity, 0);
		}
		for (XEventClass endActivity : logInfo.getEndActivities().toSet()) {
			connectedComponents.put(endActivity, 0);
		}

		//find the other connected components
		Integer ccs = 1;
		for (XEventClass node : logInfo.getActivities()) {
			if (!connectedComponents.containsKey(node)) {
				labelConnectedComponents(logInfo.getDirectlyFollowsGraph(), node, connectedComponents, ccs);
				ccs += 1;
			}
		}

		//find the start activities of each component
		HashMap<Integer, Set<XEventClass>> startActivities = new HashMap<Integer, Set<XEventClass>>();
		for (Integer cc = 0; cc < ccs; cc++) {
			startActivities.put(cc, new HashSet<XEventClass>());
		}
		for (XEventClass node : logInfo.getActivities()) {
			Integer cc = connectedComponents.get(node);
			for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().incomingEdgesOf(node)) {
				if (cc != connectedComponents.get(logInfo.getDirectlyFollowsGraph().getEdgeSource(edge))) {
					//this is a start activity
					Set<XEventClass> start = startActivities.get(cc);
					start.add(node);
					startActivities.put(cc, start);
				}
			}
		}

		//find the end activities of each component
		HashMap<Integer, Set<XEventClass>> endActivities = new HashMap<Integer, Set<XEventClass>>();
		for (Integer cc = 0; cc < ccs; cc++) {
			endActivities.put(cc, new HashSet<XEventClass>());
		}
		for (XEventClass node : logInfo.getActivities()) {
			Integer cc = connectedComponents.get(node);
			for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().outgoingEdgesOf(node)) {
				if (cc != connectedComponents.get(logInfo.getDirectlyFollowsGraph().getEdgeTarget(edge))) {
					//this is an end activity
					Set<XEventClass> end = endActivities.get(cc);
					end.add(node);
					endActivities.put(cc, end);
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
		for (XEventClass startActivity : logInfo.getStartActivities().toSet()) {
			if (!logInfo.getEndActivities().contains(startActivity)) {
				for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().outgoingEdgesOf(startActivity)) {
					candidates[connectedComponents.get(logInfo.getDirectlyFollowsGraph().getEdgeTarget(edge))] = false;
				}
			}
		}

		//exclude all candidates that can reach an end activity (which is not a start activity)
		for (XEventClass endActivity : logInfo.getEndActivities().toSet()) {
			if (!logInfo.getStartActivities().contains(endActivity)) {
				for (DefaultWeightedEdge edge : logInfo.getDirectlyFollowsGraph().incomingEdgesOf(endActivity)) {
					candidates[connectedComponents.get(logInfo.getDirectlyFollowsGraph().getEdgeSource(edge))] = false;
				}
			}
		}

		//exclude all candidates that have no connection to all start activities
		for (Integer cc = 0; cc < ccs; cc++) {
			Set<XEventClass> end = endActivities.get(cc);
			for (XEventClass node1 : end) {
				for (XEventClass node2 : logInfo.getStartActivities().toSet()) {
					if (!logInfo.getDirectlyFollowsGraph().containsEdge(node1, node2)) {
						candidates[cc] = false;
						//debug("body part for no connection to all start activities " + cc.toString());
					}
				}
			}
		}

		//exclude all candidates that have no connection from all end activities
		for (Integer cc = 0; cc < ccs; cc++) {
			Set<XEventClass> start = startActivities.get(cc);
			for (XEventClass node1 : start) {
				for (XEventClass node2 : logInfo.getEndActivities().toSet()) {
					if (!logInfo.getDirectlyFollowsGraph().containsEdge(node2, node1)) {
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
		for (XEventClass node : logInfo.getActivities()) {
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

	private static void labelConnectedComponents(DefaultDirectedGraph<XEventClass, DefaultWeightedEdge> graph,
			XEventClass node, HashMap<XEventClass, Integer> connectedComponents, Integer connectedComponent) {
		if (!connectedComponents.containsKey(node)) {
			connectedComponents.put(node, connectedComponent);
			for (DefaultWeightedEdge edge : graph.edgesOf(node)) {
				labelConnectedComponents(graph, graph.getEdgeSource(edge), connectedComponents, connectedComponent);
				labelConnectedComponents(graph, graph.getEdgeTarget(edge), connectedComponents, connectedComponent);
			}
		}
	}

}
