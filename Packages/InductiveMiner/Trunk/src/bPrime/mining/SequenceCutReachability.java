package bPrime.mining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class SequenceCutReachability {
	
	private HashMap<Set<XEventClass>, Set<Set<XEventClass>>> reachableTo;
	private HashMap<Set<XEventClass>, Set<Set<XEventClass>>> reachableFrom;
	private DirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph;

	public SequenceCutReachability(DirectedGraph<Set<XEventClass>, DefaultEdge> condensedGraph1) {
		reachableTo = new HashMap<Set<XEventClass>, Set<Set<XEventClass>>>();
		reachableFrom = new HashMap<Set<XEventClass>, Set<Set<XEventClass>>>();
		condensedGraph = condensedGraph1;
	}
	
	public Set<Set<XEventClass>> getReachableFromTo(Set<XEventClass> node) {
		Set<Set<XEventClass>> r = new HashSet<Set<XEventClass>>(findReachableTo(node));
		r.addAll(findReachableFrom(node));
		return r;
	}
	
	public Set<Set<XEventClass>> getReachableFrom(Set<XEventClass> node) {
		return findReachableFrom(node);
	}
	
	private Set<Set<XEventClass>> findReachableTo(Set<XEventClass> from) {
		if (!reachableTo.containsKey(from)) {
			Set<Set<XEventClass>> reached = new HashSet<Set<XEventClass>>();
			
			for (DefaultEdge edge : condensedGraph.outgoingEdgesOf(from)) {
				Set<XEventClass> target = condensedGraph.getEdgeTarget(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableTo(target));
			}
			
			reachableTo.put(from, reached);
		}
		return reachableTo.get(from);
	}
	
	private Set<Set<XEventClass>> findReachableFrom(Set<XEventClass> to) {
		if (!reachableFrom.containsKey(to)) {
			Set<Set<XEventClass>> reached = new HashSet<Set<XEventClass>>();
			
			for (DefaultEdge edge : condensedGraph.incomingEdgesOf(to)) {
				Set<XEventClass> target = condensedGraph.getEdgeSource(edge);
				reached.add(target);
				
				//recurse
				reached.addAll(findReachableFrom(target));
			}
			
			reachableFrom.put(to, reached);
		}
		return reachableFrom.get(to);
	}
}
