package bPrime.mining;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import bPrime.Sets;

public class SequenceCut {
	
	public static List<Set<XEventClass>> findSequenceCut(DirectedGraph<XEventClass, DefaultWeightedEdge> G) {
		
		//compute the strongly connected components of the directly-follows graph G
		StrongConnectivityInspector<XEventClass, DefaultWeightedEdge> SCCg = new StrongConnectivityInspector<XEventClass, DefaultWeightedEdge>(G);
		Set<Set<XEventClass>> SCCs = new HashSet<Set<XEventClass>>(SCCg.stronglyConnectedSets());
		
		//condense the strongly connected components
		DirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph1 = new DefaultDirectedGraph<Set<XEventClass>, DefaultEdge>(DefaultEdge.class);
		//add vertices (= components)
		for (Set<XEventClass> SCC : SCCs) {
			condensedGraph1.addVertex(SCC);
		}
		//add edges
		for (DefaultWeightedEdge edge : G.edgeSet()) {
			//find the connected components belonging to these nodes
			XEventClass u = G.getEdgeSource(edge);
			Set<XEventClass> SCCu = Sets.findComponentWith(SCCs, u);
			XEventClass v = G.getEdgeTarget(edge);
			Set<XEventClass> SCCv = Sets.findComponentWith(SCCs, v);
			
			//add an edge if it is not internal
			if (SCCv != SCCu) {
				condensedGraph1.addEdge(SCCu, SCCv); //this returns null if the edge was already present
			}
		}
		
		//debug("nodes in condensed graph 1 " + condensedGraph1.vertexSet().toString());
		
		//condense the pairwise unreachable nodes
		DirectedGraph<Set<XEventClass>, DefaultEdge> xorGraph = new DefaultDirectedGraph<Set<XEventClass>, DefaultEdge>(DefaultEdge.class);
		for (Set<XEventClass> node : condensedGraph1.vertexSet()) {
			//add to the xor graph
			xorGraph.addVertex(node);
		}
		
		SequenceCutReachability scr1 = new SequenceCutReachability(condensedGraph1);
		for (Set<XEventClass> node : condensedGraph1.vertexSet()) {
			Set<Set<XEventClass>> reachableFromTo = scr1.getReachableFromTo(node);
			
			//debug("nodes pairwise reachable from/to " + node.toString() + ": " + reachableFromTo.toString());
			
			Set<Set<XEventClass>> notReachable = Sets.difference(condensedGraph1.vertexSet(), reachableFromTo);
			
			//remove the node itself
			notReachable.remove(node);
			
			//add edges to the xor graph
			for (Set<XEventClass> node2 : notReachable) {
				xorGraph.addEdge(node, node2);
			}
		}
		
		//find the connected components to find the condensed xor nodes
		List<Set<Set<XEventClass>>> xorCondensedNodes = new ConnectivityInspector<Set<XEventClass>, DefaultEdge>(xorGraph).connectedSets();
		//debug("sccs voor xormerge " + xorCondensedNodes.toString());
		
		//make a new condensed graph
		DirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph2 = new DefaultDirectedGraph<Set<XEventClass>, DefaultEdge>(DefaultEdge.class);
		for (Set<Set<XEventClass>> node : xorCondensedNodes) {
			
			//we need to flatten this s to get a new list of nodes
			condensedGraph2.addVertex(Sets.flatten(node));
		}
		
		//debug("sccs na xormerge " + condensedGraph2.vertexSet().toString());
		
		//add the edges
		for (DefaultEdge edge : condensedGraph1.edgeSet()) {
			//find the condensed node belonging to this activity
			Set<XEventClass> u = condensedGraph1.getEdgeSource(edge);
			Set<XEventClass> SCCu = Sets.findComponentWith(condensedGraph2.vertexSet(), u.iterator().next());
			Set<XEventClass> v = condensedGraph1.getEdgeTarget(edge);
			Set<XEventClass> SCCv = Sets.findComponentWith(condensedGraph2.vertexSet(), v.iterator().next());
			
			//add an edge if it is not internal
			if (SCCv != SCCu) {
				condensedGraph2.addEdge(SCCu, SCCv); //this returns null if the edge was already present
				//debug ("nodes in condensed graph 2 " + implode(condensedGraph2.vertexSet(), ", "));
			}
		}
		
		//now we have a condensed graph. we need to return a sorted list of condensed nodes.
		final SequenceCutReachability scr2 = new SequenceCutReachability(condensedGraph2);
		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		result.addAll(condensedGraph2.vertexSet());
		Collections.sort(result, new Comparator<Set<XEventClass>>() {

			public int compare(Set<XEventClass> arg0, Set<XEventClass> arg1) {
				if (scr2.getReachableFrom(arg0).contains(arg1)) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});

		
		//for (Set<Set<XEventClass>> se : xorCondensedNodes) {
		//	debug("xor-free nodes: " + implode2(se, ", "));
		//}
		
		return result;
	}
	
	//private static void debug(String x) {
	//	System.out.println(x);
	//}
}