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
		
		debug(connectedComponents.toString());
		
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
		
		debug(result2.toString());
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
	
	private static void debug(String x) {
		System.out.println(x);
	}
}
