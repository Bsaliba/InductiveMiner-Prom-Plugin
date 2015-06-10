package org.processmining.plugins.InductiveMiner.mining.cuts.IMlc;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.graphs.Graph;
import org.processmining.plugins.InductiveMiner.graphs.GraphFactory;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.MinerState;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut;
import org.processmining.plugins.InductiveMiner.mining.cuts.Cut.Operator;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;

public class CutFinderIMlcInterleaved implements CutFinder {

	public Cut findCut(IMLog log, IMLogInfo logInfo, MinerState minerState) {
		return findCut(logInfo.getDirectlyFollowsGraph(), logInfo.getConcurrencyGraph(), logInfo.getStartActivities(),
				logInfo.getEndActivities());
	}

	public static Cut findCut(Graph<XEventClass> dfg, Graph<XEventClass> concurrencyGraph,
			MultiSet<XEventClass> startActivities, MultiSet<XEventClass> endActivities) {

		//set up clusters
		TObjectIntMap<XEventClass> clusters = new TObjectIntHashMap<>();

		//consider start activities
		int i = 0;
		for (XEventClass startActivity : startActivities) {
			//start a new cluster
			clusters.put(startActivity, i);

			//process all outgoing nodes
			Queue<XEventClass> q = new LinkedList<>();
			q.add(startActivity);
			while (!q.isEmpty()) {
				for (long e : dfg.getOutgoingEdgesOf(q.poll())) {
					XEventClass c = dfg.getEdgeTarget(e);
					if (!startActivities.contains(c) && !clusters.containsKey(c)) {
						//this is not a start activity; merge the two clusters
						mergeClusters(clusters, startActivity, c);

						//process further
						q.add(c);
					}
				}
			}
			i++;
		}

		//merge clusters that have at least one concurrent connection
		for (long edge : concurrencyGraph.getEdges()) {
			mergeClusters(clusters, concurrencyGraph.getEdgeSource(edge), concurrencyGraph.getEdgeTarget(edge));
		}

		//merge all clusters that are not fully connected, i.e. xor and sequence clusters
		Graph<Long> clusterGraph = GraphFactory.create(Long.class, i);
		for (TIntIterator it = clusters.valueCollection().iterator(); it.hasNext();) {
			clusterGraph.addVertex((long) it.next());
		}
		for (long edge : dfg.getEdges()) {
			int cluster1 = clusters.get(dfg.getEdgeSource(edge));
			int cluster2 = clusters.get(dfg.getEdgeTarget(edge));
			clusterGraph.addEdge((long) cluster1, (long) cluster2, 1);
		}
		for (long cluster1 : clusterGraph.getVertices()) {
			for (long cluster2 : clusterGraph.getVertices()) {
				if (!clusterGraph.containsEdge(cluster1, cluster2) || !clusterGraph.containsEdge(cluster2, cluster1)) {
					mergeClusters(clusters, (int) cluster1, (int) cluster2);
				}
			}
		}

		Collection<Set<XEventClass>> partition = getPartition(clusters);

		if (partition.size() > 1) {
			return new Cut(Operator.maybeInterleaved, partition);
		} else {
			return null;
		}
	}

	public static void mergeClusters(TObjectIntMap<XEventClass> clusters, int c1, int c2) {
		for (XEventClass e3 : clusters.keySet()) {
			if (clusters.get(e3) == c2) {
				clusters.put(e3, c1);
			}
		}
	}

	public static void mergeClusters(TObjectIntMap<XEventClass> clusters, XEventClass e1, XEventClass e2) {
		int target = clusters.get(e1);
		if (clusters.containsKey(e2)) {
			int oldCluster = clusters.get(e2);
			for (XEventClass e3 : clusters.keySet()) {
				if (clusters.get(e3) == oldCluster) {
					clusters.put(e3, target);
				}
			}
		} else {
			clusters.put(e2, target);
		}
	}

	public static Collection<Set<XEventClass>> getPartition(TObjectIntMap<XEventClass> clusters) {
		TIntObjectHashMap<Set<XEventClass>> map = new TIntObjectHashMap<>();

		for (XEventClass a : clusters.keySet()) {
			int cluster = clusters.get(a);
			map.putIfAbsent(cluster, new THashSet<XEventClass>());
			map.get(cluster).add(a);
		}

		return map.valueCollection();
	}
}
