package org.processmining.plugins.InductiveMiner.mining.cuts.IM;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.processmining.plugins.InductiveMiner.graphs.ConnectedComponents;
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
		//add vertices (= components)
		for (Set<XEventClass> SCC : SCCs) {
			condensedGraph1.addVertex(SCC);
		}
		//add edges
		for (long edge : graph.getEdges()) {
			//find the connected components belonging to these nodes
			XEventClass u = graph.getEdgeSource(edge);
			Set<XEventClass> SCCu = Sets.findComponentWith(SCCs, u);
			XEventClass v = graph.getEdgeTarget(edge);
			Set<XEventClass> SCCv = Sets.findComponentWith(SCCs, v);

			//add an edge if it is not internal
			if (SCCv != SCCu) {
				condensedGraph1.addEdge(SCCu, SCCv, 1); //this returns null if the edge was already present
			}
		}

		//debug("nodes in condensed graph 1 " + condensedGraph1.vertexSet().toString());

		//condense the pairwise unreachable nodes
		Graph<Set<XEventClass>> xorGraph = GraphFactory.create(Set.class, condensedGraph1.getNumberOfVertices());
		xorGraph.addVertices(condensedGraph1.getVertices());

		CutFinderIMSequenceReachability<Set<XEventClass>> scr1 = new CutFinderIMSequenceReachability<>(condensedGraph1);
		for (Set<XEventClass> node : condensedGraph1.getVertices()) {
			Set<Set<XEventClass>> reachableFromTo = scr1.getReachableFromTo(node);

			//debug("nodes pairwise reachable from/to " + node.toString() + ": " + reachableFromTo.toString());

			Set<Set<XEventClass>> notReachable = Sets.difference(ArrayUtilities.toSet(condensedGraph1.getVertices()),
					reachableFromTo);

			//remove the node itself
			notReachable.remove(node);

			//add edges to the xor graph
			for (Set<XEventClass> node2 : notReachable) {
				xorGraph.addEdge(node, node2, 1);
			}
		}

		//find the connected components to find the condensed xor nodes
		Set<Set<Set<XEventClass>>> xorCondensedNodes = ConnectedComponents.compute(xorGraph);
		//debug("sccs voor xormerge " + xorCondensedNodes.toString());

		//make a new condensed graph
		Graph<Set<XEventClass>> condensedGraph2 = GraphFactory.create(Set.class, xorCondensedNodes.size());
		for (Set<Set<XEventClass>> node : xorCondensedNodes) {

			//we need to flatten this s to get a new list of nodes
			condensedGraph2.addVertex(Sets.flatten(node));
		}

		//debug("sccs na xormerge " + condensedGraph2.vertexSet().toString());

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
			for (int i = 0; i <= subCutWithMinimumTaus ; i ++) {
				result1.addAll(result.get(i));
			}
			for (int i = subCutWithMinimumTaus + 1; i < result.size() ; i ++) {
				result2.addAll(result.get(i));
			}
			result.clear();
			result.add(result1);
			result.add(result2);
		}

		return new Cut(Operator.sequence, result);
	}
}
