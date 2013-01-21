package bPrime.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class LoopCut {
	
	public static List<Set<XEventClass>> findLoopCut(DirectlyFollowsRelation drf) {
		
		//initialise the start and end activities as a connected component
		HashMap<XEventClass, Integer> connectedComponents = new HashMap<XEventClass, Integer>();
		for (XEventClass startActivity : drf.getStartActivities()) {
			connectedComponents.put(startActivity, 0);
		}
		for (XEventClass endActivity : drf.getEndActivities()) {
			connectedComponents.put(endActivity, 0);
		}
		
		//find the other connected components
		Integer ccs = 1;
		for (XEventClass node : drf.getGraph().vertexSet()) {
			if (!connectedComponents.containsKey(node)) {
				labelConnectedComponents(drf.getGraph(), node, connectedComponents, ccs);
				ccs += 1;
			}
		}
		
		//find the start activities of each component
		HashMap<Integer, Set<XEventClass>> startActivities = new HashMap<Integer, Set<XEventClass>>();
		for (Integer cc=0;cc<ccs;cc++) {
			startActivities.put(cc, new HashSet<XEventClass>());
		}
		for (XEventClass node : drf.getGraph().vertexSet()) {
			Integer cc = connectedComponents.get(node);
			for (DefaultEdge edge : drf.getGraph().incomingEdgesOf(node)) {
				if (cc != connectedComponents.get(drf.getGraph().getEdgeSource(edge))) {
					//this is a start activity
					Set<XEventClass> start = startActivities.get(cc);
					start.add(node);
					startActivities.put(cc, start);
				}
			}
		}
		
		//find the end activities of each component
		HashMap<Integer, Set<XEventClass>> endActivities = new HashMap<Integer, Set<XEventClass>>();
		for (Integer cc=0;cc<ccs;cc++) {
			endActivities.put(cc, new HashSet<XEventClass>());
		}
		for (XEventClass node : drf.getGraph().vertexSet()) {
			Integer cc = connectedComponents.get(node);
			for (DefaultEdge edge : drf.getGraph().outgoingEdgesOf(node)) {
				if (cc != connectedComponents.get(drf.getGraph().getEdgeTarget(edge))) {
					//this is an end activity
					Set<XEventClass> end = endActivities.get(cc);
					end.add(node);
					endActivities.put(cc, end);
				}
			}
		}
		
		//debug(startActivities.toString());
		//debug(endActivities.toString());
		
		//debug(connectedComponents.toString());
		
		//initialise the candidates
		Boolean[] candidates = new Boolean[ccs];
		//the start and end activities are no candidates
		candidates[0] = false;
		for (int i=1;i<ccs;i++) {
			candidates[i] = true;
		}
		
		//exclude all candidates that are reachable from the start activities (that are not an end activity)
		for (XEventClass startActivity : drf.getStartActivities()) {
			if (!drf.getEndActivities().contains(startActivity)) {
				for (DefaultEdge edge : drf.getGraph().outgoingEdgesOf(startActivity)) {
					candidates[connectedComponents.get(drf.getGraph().getEdgeTarget(edge))] = false;
				}
			}
		}
		
		//exclude all candidates that can reach an end activity (which is not a start activity)
		for (XEventClass endActivity : drf.getEndActivities()) {
			if (!drf.getStartActivities().contains(endActivity)) {
				for (DefaultEdge edge : drf.getGraph().incomingEdgesOf(endActivity)) {
					candidates[connectedComponents.get(drf.getGraph().getEdgeSource(edge))] = false;
				}
			}
		}
		
		//exclude all candidates that have no connection to all start activities
		for (Integer cc=0;cc<ccs;cc++) {
			Set<XEventClass> end = endActivities.get(cc);
			for (XEventClass node1 : end) {
				for (XEventClass node2 : drf.getStartActivities()) {
					if (!drf.getGraph().containsEdge(node1, node2)) {
						candidates[cc] = false;
					}
				}
			}
		}
		
		//exclude all candidates that have no connection from all end activities
		for (Integer cc=0;cc<ccs;cc++) {
			Set<XEventClass> start = startActivities.get(cc);
			for (XEventClass node1 : start) {
				for (XEventClass node2 : drf.getEndActivities()) {
					if (!drf.getGraph().containsEdge(node2, node1)) {
						candidates[cc] = false;
					}
				}
			}
		}
		
		//make the lists of sets of nodes
		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		for (int i=0;i<ccs;i++) {
			result.add(new HashSet<XEventClass>());
		}
		
		//divide the activities
		for (XEventClass node : drf.getGraph().vertexSet()) {
			//debug += node.toString() + " in connected component " + connectedComponents.get(node);
			int index;
			if (candidates[connectedComponents.get(node)]) {
				index = connectedComponents.get(node);
				//debug += ", redo part of loop";
			} else {
				index = 0;
				//debug += ", body part of loop";
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
		return result2;
	}
	
	private static void labelConnectedComponents(
			DefaultDirectedGraph<XEventClass, DefaultEdge> graph,
			XEventClass node, 
			HashMap<XEventClass, Integer> connectedComponents, 
			Integer connectedComponent) {
		if (!connectedComponents.containsKey(node)) {
			connectedComponents.put(node, connectedComponent);
			for (DefaultEdge edge : graph.edgesOf(node)) {
				labelConnectedComponents(graph, graph.getEdgeSource(edge), connectedComponents, connectedComponent);
				labelConnectedComponents(graph, graph.getEdgeTarget(edge), connectedComponents, connectedComponent);
			}
		}
	}
	
	//private static void debug(String x) {
	//	System.out.println(x);
	//}
}
