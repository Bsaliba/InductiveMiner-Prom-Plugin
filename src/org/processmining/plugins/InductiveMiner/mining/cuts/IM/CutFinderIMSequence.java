package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.ArrayUtilities;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Sets;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMinerState;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinder;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.graphs.StronglyConnectedComponents;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMSequence implements CutFinder, DfgCutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getStartActivities(), logInfo.getEndActivities(), logInfo.getDirectlyFollowsGraph());
	}

	public Cut findCut(Dfg dfg, DfgMinerState minerState) {
		return findCut(dfg.getStartActivities(), dfg.getEndActivities(), dfg.getDirectlyFollowsGraph());
	}

	public static Cut findCut(MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities,
			Graph<XEventClass> graph) {
		//compute the strongly connected components of the directly-follows graph
		Set<Set<XEventClass>> SCCs = StronglyConnectedComponents.compute(graph);

		//condense the strongly connected components
		Graph<Set<XEventClass>> condensedGraph1 = GraphFactory.create(Set.class, SCCs.size());
		{

			//15-3-2016: optimisation to look up strongly connected components faster
			THashMap<XEventClass, Set<XEventClass>> node2scc = new THashMap<>();
			for (Set<XEventClass> scc : SCCs) {
				for (XEventClass e : scc) {
					node2scc.put(e, scc);
				}
			}

			//add vertices (= components)
			for (Set<XEventClass> SCC : SCCs) {
				condensedGraph1.addVertex(SCC);
			}
			//add edges
			for (long edge : graph.getEdges()) {
				if (graph.getEdgeWeight(edge) >= 0) {
					//find the connected components belonging to these nodes
					XEventClass u = graph.getEdgeSource(edge);
					Set<XEventClass> SCCu = node2scc.get(u);
					XEventClass v = graph.getEdgeTarget(edge);
					Set<XEventClass> SCCv = node2scc.get(v);

					//add an edge if it is not internal
					if (SCCv != SCCu) {
						condensedGraph1.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
					}
				}
			}
		}

		//debug("  nodes in condensed graph 1 " + condensedGraph1.getVertices());

		//condense the pairwise unreachable nodes
		Collection<Set<Set<XEventClass>>> xorCondensedNodes;
		{
			Components<Set<XEventClass>> components = new Components<Set<XEventClass>>(condensedGraph1.getVertices());
			CutFinderIMSequenceReachability<Set<XEventClass>> scr1 = new CutFinderIMSequenceReachability<>(
					condensedGraph1);
			for (Set<XEventClass> node : condensedGraph1.getVertices()) {
				Set<Set<XEventClass>> reachableFromTo = scr1.getReachableFromTo(node);

				//debug("nodes pairwise reachable from/to " + node.toString() + ": " + reachableFromTo.toString());

				Set<Set<XEventClass>> notReachable = Sets.difference(
						ArrayUtilities.toSet(condensedGraph1.getVertices()), reachableFromTo);

				//remove the node itself
				notReachable.remove(node);

				//merge unreachable sets
				for (Set<XEventClass> node2 : notReachable) {
					components.mergeComponentsOf(node, node2);
				}
			}
			
			//find the connected components to find the condensed xor nodes
			xorCondensedNodes = components.getComponents();
		}
		
		//debug("sccs voor xormerge " + xorCondensedNodes.toString());

		//make a new condensed graph
		Graph<Set<XEventClass>> condensedGraph2 = GraphFactory.create(Set.class, xorCondensedNodes.size());
		for (Set<Set<XEventClass>> node : xorCondensedNodes) {

			//we need to flatten this s to get a new list of nodes
			condensedGraph2.addVertex(Sets.flatten(node));
		}

		//debug("sccs na xormerge " + condensedGraph2.getVertices().toString());

		//add the edges
		for (long edge : condensedGraph1.getEdges()) {
			//find the condensed node belonging to this activity
			Set<XEventClass> u = condensedGraph1.getEdgeSource(edge);
			Set<XEventClass> SCCu = Sets.findComponentWith(ArrayUtilities.toSet(condensedGraph2.getVertices()), u
					.iterator().next());
			Set<XEventClass> v = condensedGraph1.getEdgeTarget(edge);
			Set<XEventClass> SCCv = Sets.findComponentWith(ArrayUtilities.toSet(condensedGraph2.getVertices()), v
					.iterator().next());

			//add an edge if it is not internal
			if (SCCv != SCCu) {
				condensedGraph2.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
				//debug ("nodes in condensed graph 2 " + Sets.implode(condensedGraph2.vertexSet(), ", "));
			}
		}

		//now we have a condensed graph. we need to return a sorted list of condensed nodes.
		final CutFinderIMSequenceReachability<Set<XEventClass>> scr2 = new CutFinderIMSequenceReachability<>(
				condensedGraph2);
		List<Set<XEventClass>> result = new ArrayList<Set<XEventClass>>();
		result.addAll(Arrays.asList(condensedGraph2.getVertices()));
		Collections.sort(result, new Comparator<Set<XEventClass>>() {

			public int compare(Set<XEventClass> arg0, Set<XEventClass> arg1) {
				if (scr2.getReachableFrom(arg0).contains(arg1)) {
					return 1;
				} else {
					return -1;
				}
			}

		});

		if (result.size() <= 1) {
			return null;
		}

		/**
		 * Optimisation 4-8-2015: do not greedily use the maximal cut, but
		 * choose the one that minimises the introduction of taus.
		 * 
		 * This solves the case {<a, b, c>, <c>}, where choosing the cut {a,
		 * b}{c} increases precision over choosing the cut {a}{b}{c}.
		 */
		{
			//make a mapping node -> subCut
			//initialise counting of taus
			TObjectIntMap<XEventClass> node2subCut = new TObjectIntHashMap<>();
			long[] skippingTaus = new long[result.size() - 1];
			for (int subCut = 0; subCut < result.size(); subCut++) {
				for (XEventClass e : result.get(subCut)) {
					node2subCut.put(e, subCut);
				}
			}

			//count the number of taus that will be introduced by each edge
			for (long edge : graph.getEdges()) {
				XEventClass source = graph.getEdgeSource(edge);
				XEventClass target = graph.getEdgeTarget(edge);
				long cardinality = graph.getEdgeWeight(edge);
				for (int c = node2subCut.get(source) + 1; c < node2subCut.get(target) - 1; c++) {
					skippingTaus[c] += cardinality;
				}
			}

			//count the number of taus that will be introduced by each start activity
			for (XEventClass e : startActivities) {
				for (int c = 0; c < node2subCut.get(e) - 1; c++) {
					skippingTaus[c] += startActivities.getCardinalityOf(e);
				}
			}

			//count the number of taus that will be introduced by each end activity
			for (XEventClass e : endActivities) {
				for (int c = node2subCut.get(e) + 1; c < result.size() - 1; c++) {
					skippingTaus[c] += endActivities.getCardinalityOf(e);
				}
			}

			//find the sub cut that introduces the least taus
			int subCutWithMinimumTaus = -1;
			{
				long minimumTaus = Long.MAX_VALUE;
				for (int i = 0; i < skippingTaus.length; i++) {
					if (skippingTaus[i] < minimumTaus) {
						subCutWithMinimumTaus = i;
						minimumTaus = skippingTaus[i];
					}
				}
			}

			//make a new cut
			Set<XEventClass> result1 = new THashSet<>();
			Set<XEventClass> result2 = new THashSet<>();
			for (int i = 0; i <= subCutWithMinimumTaus; i++) {
				result1.addAll(result.get(i));
			}
			for (int i = subCutWithMinimumTaus + 1; i < result.size(); i++) {
				result2.addAll(result.get(i));
			}
			result.clear();
			result.add(result1);
			result.add(result2);
		}

		return new Cut(Operator.sequence, result);
	}

	public static class Components<V> {

		private int[] components;
		private int numberOfComponents;
		private TObjectIntHashMap<V> node2index;

		public Components(V[] nodes) {
			components = new int[nodes.length];
			numberOfComponents = nodes.length;
			for (int i = 0; i < components.length; i++) {
				components[i] = i;
			}

			node2index = new TObjectIntHashMap<V>();
			{
				int i = 0;
				for (V node : nodes) {
					node2index.put(node, i);
					i++;
				}
			}
		}

		public void mergeComponentsOf(int indexA, int indexB) {
			int source = components[indexA];
			int target = components[indexB];

			if (source != target) {
				numberOfComponents--;
				for (int i = 0; i < components.length; i++) {
					if (components[i] == source) {
						components[i] = target;
					}
				}
			}
		}

		public void mergeComponentsOf(V nodeA, V nodeB) {
			mergeComponentsOf(node2index.get(nodeA), node2index.get(nodeB));
		}

		public int getNumberOfComponents() {
			return numberOfComponents;
		}

		public List<Set<V>> getComponents() {
			final List<Set<V>> result = new ArrayList<Set<V>>(numberOfComponents);

			//prepare a hashmap of components
			final TIntIntHashMap component2componentIndex = new TIntIntHashMap();
			int highestComponentIndex = 0;
			for (int node = 0; node < components.length; node++) {
				int component = components[node];
				if (!component2componentIndex.contains(component)) {
					component2componentIndex.put(component, highestComponentIndex);
					highestComponentIndex++;
					result.add(new THashSet<V>());
				}
			}

			//put each node in its component
			node2index.forEachEntry(new TObjectIntProcedure<V>() {
				public boolean execute(V node, int nodeIndex) {
					int component = components[nodeIndex];
					int componentIndex = component2componentIndex.get(component);
					result.get(componentIndex).add(node);
					return true;
				}
			});

			return result;
		}
	}
	
	private static void debug(String s) {
		//System.out.println(s);
	}
}
