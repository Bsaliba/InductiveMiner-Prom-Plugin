package org.processmining.plugins.InductiveMiner.mining.cuts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;

public class SequenceCutReachability<V,E> {
	
	private HashMap<V, Set<V>> reachableTo;
	private HashMap<V, Set<V>> reachableFrom;
	private DirectedGraph<V, E> condensedGraph;

	public SequenceCutReachability(DirectedGraph<V,E> graph) {
		reachableTo = new HashMap<V, Set<V>>();
		reachableFrom = new HashMap<V, Set<V>>();
		this.condensedGraph = graph;
	}
	
	public Set<V> getReachableFromTo(V node) {
		Set<V> r = new HashSet<V>(findReachableTo(node));
		r.addAll(findReachableFrom(node));
		return r;
	}
	
	public Set<V> getReachableFrom(V node) {
		return findReachableFrom(node);
	}
	
	private Set<V> findReachableTo(V from) {
		if (!reachableTo.containsKey(from)) {
			Set<V> reached = new HashSet<V>();
			
			for (E edge : condensedGraph.outgoingEdgesOf(from)) {
				V target = condensedGraph.getEdgeTarget(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableTo(target));
			}
			
			reachableTo.put(from, reached);
		}
		return reachableTo.get(from);
	}
	
	private Set<V> findReachableFrom(V to) {
		if (!reachableFrom.containsKey(to)) {
			Set<V> reached = new HashSet<V>();
			
			for (E edge : condensedGraph.incomingEdgesOf(to)) {
				V target = condensedGraph.getEdgeSource(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableFrom(target));
			}
			
			reachableFrom.put(to, reached);
		}
		return reachableFrom.get(to);
	}
}
