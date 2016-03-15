package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.processmining.plugins.InductiveMiner.graphs.Graph;

public class CutFinderIMSequenceReachability {

	private TIntObjectMap<TIntSet> reachableTo;
	private TIntObjectMap<TIntSet> reachableFrom;
	private Graph<?> condensedGraph;

	public CutFinderIMSequenceReachability(Graph<?> graph) {
		reachableTo = new TIntObjectHashMap<>();
		reachableFrom = new TIntObjectHashMap<>();
		this.condensedGraph = graph;
	}

	public TIntSet getReachableFromTo(int node) {
		System.out.println("  graph " + condensedGraph + ", node " + node);
		TIntSet r = new TIntHashSet(findReachableTo(node));
		System.out.println("   reachable by outgoing edges " + r);
		r.addAll(findReachableFrom(node));
		System.out.println("   reachability " + r);
		return r;
	}

	public TIntSet getReachableFrom(int node) {
		return findReachableFrom(node);
	}

	private TIntSet findReachableTo(int from) {
		System.out.println("     add outgoing edges of " + from);
		if (!reachableTo.containsKey(from)) {
			TIntSet reached = new TIntHashSet();

			reachableTo.put(from, reached);

			for (long edge : condensedGraph.getOutgoingEdgesOf(from)) {
				int target = condensedGraph.getEdgeTargetIndex(edge);
				reached.add(target);
				System.out.println("   add " + target);

				//recurse
				reached.addAll(findReachableTo(target));
			}
		}
		System.out.println("     exit recursion of " + from + " with " + reachableTo.get(from));
		return reachableTo.get(from);
	}

	private TIntSet findReachableFrom(int to) {
		if (!reachableFrom.containsKey(to)) {
			TIntSet reached = new TIntHashSet();

			reachableFrom.put(to, reached);

			for (long edge : condensedGraph.getIncomingEdgesOf(to)) {
				int source = condensedGraph.getEdgeSourceIndex(edge);
				reached.add(source);

				//recurse
				reached.addAll(findReachableFrom(source));
			}
		}
		return reachableFrom.get(to);
	}
}
