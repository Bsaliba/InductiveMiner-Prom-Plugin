package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class CutFinderIMSequenceReachability<V> {
	
	private Map<V, Set<V>> reachableTo;
	private Map<V, Set<V>> reachableFrom;
	private Graph<V> condensedGraph;

	public CutFinderIMSequenceReachability(Graph<V> graph) {
		reachableTo = new THashMap<V, Set<V>>();
		reachableFrom = new THashMap<V, Set<V>>();
		this.condensedGraph = graph;
	}
	
	public Set<V> getReachableFromTo(V node) {
		Set<V> r = new THashSet<V>(findReachableTo(node));
		r.addAll(findReachableFrom(node));
		return r;
	}
	
	public Set<V> getReachableFrom(V node) {
		return findReachableFrom(node);
	}
	
	private Set<V> findReachableTo(V from) {
		if (!reachableTo.containsKey(from)) {
			Set<V> reached = new THashSet<V>();
			
			reachableTo.put(from, reached);
			
			for (long edge : condensedGraph.getOutgoingEdgesOf(from)) {
				V target = condensedGraph.getEdgeTarget(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableTo(target));
			}
		}
		return reachableTo.get(from);
	}
	
	private Set<V> findReachableFrom(V to) {
		if (!reachableFrom.containsKey(to)) {
			Set<V> reached = new THashSet<V>();
			
			reachableFrom.put(to, reached);
			
			for (long edge : condensedGraph.getIncomingEdgesOf(to)) {
				V target = condensedGraph.getEdgeSource(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableFrom(target));
			}
		}
		return reachableFrom.get(to);
	}
}
