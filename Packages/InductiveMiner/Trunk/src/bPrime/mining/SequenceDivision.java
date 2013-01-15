package bPrime.mining;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import bPrime.Pair;

public class SequenceDivision {
	
	//return a set with possible divisions of the activities
	public static Set<Pair<Set<XEventClass>, Set<XEventClass>>> getDivisions(
			DefaultDirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph,
			Set<Set<XEventClass>> startNodes,
			Set<Set<XEventClass>> endNodes) {
		
		Set<Set<XEventClass>> realNodes = new HashSet<Set<XEventClass>>(condensedGraph.vertexSet());

		//add taus
		Set<Set<XEventClass>> taus = new HashSet<Set<XEventClass>>();
		Set<Set<XEventClass>> startEndNodes = intersection(startNodes, endNodes);
		for (Set<XEventClass> startEndNode : startEndNodes) {
			
			//add a tau before this node
			Set<XEventClass> beforeTau = new HashSet<XEventClass>();
			beforeTau.add(new XEventClass("beforeTau_"+UUID.randomUUID(), -1));
			condensedGraph.addVertex(beforeTau);
			condensedGraph.addEdge(beforeTau, startEndNode);
			startNodes.remove(startEndNode);
			startNodes.add(beforeTau);
			taus.add(beforeTau);
			
			//add a tau after this node
			Set<XEventClass> afterTau = new HashSet<XEventClass>();
			afterTau.add(new XEventClass("afterTau_"+UUID.randomUUID(), -1));
			condensedGraph.addVertex(afterTau);
			condensedGraph.addEdge(startEndNode, afterTau);
			endNodes.remove(startEndNode);
			endNodes.add(afterTau);
			taus.add(afterTau);
		}
		
		
		//find out which nodes are reachable from/to
		//put the result in a graph and compute the connected components
		DirectedGraph<Set<XEventClass>, DefaultEdge> xorGraph = new DefaultDirectedGraph<Set<XEventClass>, DefaultEdge>(DefaultEdge.class);
		for (Set<XEventClass> node : condensedGraph.vertexSet()) {
			//add to the xor graph
			xorGraph.addVertex(node);
		}
		
		for (Set<XEventClass> node : condensedGraph.vertexSet()) {
			Set<Set<XEventClass>> reachableFromTo = walkBack(condensedGraph, node);
			reachableFromTo.addAll(walkForward(condensedGraph, node));
			//debug("reachable from/to {" + implode(node, ",") + "}: " + implode2(reachableFromTo, ", "));
			
			Set<Set<XEventClass>> notReachable = difference(condensedGraph.vertexSet(), reachableFromTo);
			
			//remove the node itself
			notReachable.remove(node);
			debug("not reachable from/to {" + implode(node, ",") + "}: " + implode2(notReachable, ", "));
			
			//add edges to the xor graph
			for (Set<XEventClass> node2 : notReachable) {
				xorGraph.addEdge(node, node2);
			}
		}
		//find the connected components to find the condensed xor nodes
		List<Set<Set<XEventClass>>> xorCondensedNodes = new ConnectivityInspector<Set<XEventClass>, DefaultEdge>(xorGraph).connectedSets();
		for (Set<Set<XEventClass>> se : xorCondensedNodes) {
			debug("xor-free nodes: " + implode2(se, ", "));
		}
		
		//add all start nodes
		Set<Set<XEventClass>> baseCut = new HashSet<Set<XEventClass>>(startNodes);
		
		//walk back and add nodes
		Set<Set<XEventClass>> queue = new HashSet<Set<XEventClass>>(baseCut);
		for (Set<XEventClass> node : queue) {
			baseCut.addAll(walkBack(condensedGraph, node));
		}
		
		Set<Pair<Set<XEventClass>, Set<XEventClass>>> result = new HashSet<Pair<Set<XEventClass>,Set<XEventClass>>>();
		
		//if this is a valid cut, add it to the result
		Set<XEventClass> baseCutClasses = flatten(difference(baseCut, taus));
		if (baseCutClasses.size() > 0 && baseCutClasses.size() < flatten(realNodes).size()) {
			result.add(new Pair<Set<XEventClass>, Set<XEventClass>>(
					baseCutClasses, 
					complement(baseCutClasses, flatten(realNodes))));	
		}
		
		//add more cuts by extending the base cut
		for (Set<XEventClass> node : realNodes) {
			if (!baseCut.contains(node)) {
				//this node is not yet in the base cut, we could maybe add it
				Set<Set<XEventClass>> cut = extend(baseCut, node);
				
				//add all nodes from which there is a path to node
				cut.addAll(walkBack(condensedGraph, node));
				
				//scheck whether it is a valid cut and add it to the result
				Set<XEventClass> cutClasses = flatten(difference(cut, taus));
				if (cutClasses.size() > 0 && cutClasses.size() < flatten(realNodes).size()) {
					result.add(new Pair<Set<XEventClass>, Set<XEventClass>>(
							cutClasses, 
							complement(cutClasses, flatten(realNodes))));	
				}
			}
		}
		
		//dirty heuristics trick: add all nodes except the outgoing nodes
		Set<Set<XEventClass>> heuristicCut = difference(realNodes, endNodes);
		Set<XEventClass> cutClasses = flatten(difference(heuristicCut, taus));
		if (cutClasses.size() > 0 && cutClasses.size() < flatten(realNodes).size()) {
			result.add(new Pair<Set<XEventClass>, Set<XEventClass>>(
					cutClasses, 
					complement(cutClasses, flatten(realNodes))));	
		}
		
		return result;
	}
	
	private static Set<Set<XEventClass>> walkBack(
			DefaultDirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph,
			Set<XEventClass> node) {
		
		Set<Set<XEventClass>> result = new HashSet<Set<XEventClass>>();
		
		List<DefaultEdge> queue = new ArrayList<DefaultEdge>();
		queue.addAll(condensedGraph.incomingEdgesOf(node));
		while (queue.size() > 0) {
			DefaultEdge edge = queue.remove(0);
			Set<XEventClass> source = condensedGraph.getEdgeSource(edge);
			
			result.add(source);
			queue.addAll(condensedGraph.incomingEdgesOf(source));
		}
		
		return result;
	}
	
	private static Set<Set<XEventClass>> walkForward(
			DefaultDirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph,
			Set<XEventClass> node) {
		
		Set<Set<XEventClass>> result = new HashSet<Set<XEventClass>>();
		
		List<DefaultEdge> queue = new ArrayList<DefaultEdge>();
		queue.addAll(condensedGraph.outgoingEdgesOf(node));
		while (queue.size() > 0) {
			DefaultEdge edge = queue.remove(0);
			Set<XEventClass> source = condensedGraph.getEdgeTarget(edge);
			
			result.add(source);
			queue.addAll(condensedGraph.outgoingEdgesOf(source));
		}
		
		return result;
	}
	
	private static Set<Set<XEventClass>> extend(Set<Set<XEventClass>> base, Set<XEventClass> node) {
		Set<Set<XEventClass>> result = new HashSet<Set<XEventClass>>(base);
		result.add(node);
		return result;
	}
	
	private static Set<Set<XEventClass>> difference(Set<Set<XEventClass>> a, Set<Set<XEventClass>> b) {
		Set<Set<XEventClass>> result = new HashSet<Set<XEventClass>>(a);
		result.removeAll(b);
		return result;
	}
	
	private static Set<Set<XEventClass>> intersection(Set<Set<XEventClass>> a, Set<Set<XEventClass>> b) {
		Set<Set<XEventClass>> result = new HashSet<Set<XEventClass>>(a);
		result.retainAll(b);
		return result;
	}
	
	private static Set<XEventClass> flatten(Set<Set<XEventClass>> set) {
		Set<XEventClass> result = new HashSet<XEventClass>();
		for (Set<XEventClass> node : set) {
			result.addAll(node);
		}
		return result;
	}
	
	private static Set<XEventClass> complement(Set<XEventClass> set, Set<XEventClass> universe) {
		Set<XEventClass> result = new HashSet<XEventClass>(universe);
		result.removeAll(set);
		return result;
	}

	
	private static void debug(String s) {
		System.out.println(s);
	}
	
	public static String implode(Set<XEventClass> input, String glueString) {
		String output = "";
		boolean first = true;
		if (input.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (XEventClass e : input) {
				if (first) {
					first = false;
				} else {
					sb.append(glueString);
				}
				sb.append(e.toString());
			}
			output = sb.toString();
		}
		return output;
	}
	
	public static String implode2(Set<Set<XEventClass>> input, String glueString) {
		String output = "";
		if (input.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Set<XEventClass> e : input) {
				sb.append("{");
				sb.append(implode(e, glueString));
				sb.append("}");
			}
			output = sb.toString();
		}
		return output;
	}
}
