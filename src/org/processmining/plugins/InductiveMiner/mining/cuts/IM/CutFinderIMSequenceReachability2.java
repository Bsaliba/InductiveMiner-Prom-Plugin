package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import java.util.BitSet;

import org.processmining.plugins.InductiveMiner.graphs.Graph;

/**
 * Second implementation of the reachability check. Use a more stupid, but
 * hopefully faster approach.
 * 
 * @author sleemans
 *
 */
public class CutFinderIMSequenceReachability2 {

	private BitSet[] reachableTo;
	private BitSet[] reachableFrom;
	private Graph<?> condensedGraph;

	public CutFinderIMSequenceReachability2(Graph<?> graph) {
		reachableTo = new BitSet[graph.getNumberOfVertices()];
		reachableFrom = new BitSet[graph.getNumberOfVertices()];
		this.condensedGraph = graph;
	}

	public BitSet getReachableFromTo(int node) {
		BitSet r = (BitSet) findReachableTo(node, new BitSet(condensedGraph.getNumberOfVertices())).clone();
		r.or(findReachableFrom(node));
		return r;
	}

	public BitSet getReachableFrom(int node) {
		return findReachableFrom(node);
	}

	private BitSet findReachableTo(int from, BitSet result) {
//		TIntStack nodesToProcess = new TIntArrayStack();
//		nodesToProcess.push(from);
//		while (nodesToProcess.)
//			result.set(from);
//
//			for (long edge : condensedGraph.getOutgoingEdgesOf(from)) {
//				int target = condensedGraph.getEdgeTargetIndex(edge);
//				findReachableTo(target, result);
//			}
//		}
//		return reachableTo[from];
		return null;
	}

	private BitSet findReachableFrom(int to) {
		if (reachableFrom[to] == null) {
			reachableFrom[to] = new BitSet(condensedGraph.getNumberOfVertices());

			for (long edge : condensedGraph.getIncomingEdgesOf(to)) {
				int source = condensedGraph.getEdgeSourceIndex(edge);
				reachableFrom[to].set(source);

				//recurse
				reachableFrom[to].or(findReachableFrom(source));
			}
		}
		return reachableFrom[to];
	}
}
