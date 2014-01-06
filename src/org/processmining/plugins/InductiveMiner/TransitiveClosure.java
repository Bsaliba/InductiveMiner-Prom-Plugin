package org.processmining.plugins.InductiveMiner;

import java.util.HashMap;
import java.util.Iterator;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

public class TransitiveClosure {
	/*
	 * compute transitive closure of a graph, using Floyd-Warshall algorithm
	 */

	public static <V> DefaultDirectedGraph<V, DefaultEdge> transitiveClosure(DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> graph) {
		int countNodes = graph.vertexSet().size();
		boolean dist[][] = new boolean[countNodes][countNodes];
		HashMap<V, Integer> node2index = new HashMap<V, Integer>();
		HashMap<Integer, V> index2node = new HashMap<Integer, V>();

		//initialise
		{
			int i = 0;
			for (Iterator<V> it = graph.vertexSet().iterator(); it.hasNext(); i++) {
				V v = it.next();
				node2index.put(v, i);
				index2node.put(i, v);
			}
		}

		{
			for (int i = 0; i < countNodes; i++) {
				for (int j = 0; j < countNodes; j++) {
					dist[i][j] = false;
				}
			}
		}

		{
			for (DefaultWeightedEdge e : graph.edgeSet()) {
				int u = node2index.get(graph.getEdgeSource(e));
				int v = node2index.get(graph.getEdgeTarget(e));
				dist[u][v] = true;
			}
		}

		{
			for (int k = 0; k < countNodes; k++) {
				for (int i = 0; i < countNodes; i++) {
					for (int j = 0; j < countNodes; j++) {
						dist[i][j] = dist[i][j] || (dist[i][k] && dist[k][j]);
					}
				}
			}
		}

		//extract a graph from the distances
		DefaultDirectedGraph<V, DefaultEdge> transitiveClosure = new DefaultDirectedGraph<V, DefaultEdge>(
				DefaultEdge.class);
		for (int i = 0; i < countNodes; i++) {
			transitiveClosure.addVertex(index2node.get(i));
		}
		for (int i = 0; i < countNodes; i++) {
			for (int j = 0; j < countNodes; j++) {
				if (dist[i][j]) {
					V u = index2node.get(i);
					V v = index2node.get(j);
					transitiveClosure.addEdge(u, v);
				}

			}
		}

		return transitiveClosure;

	}
}
