package org.processmining.plugins.InductiveMiner.graphs;

public class GraphFactory {
	
	public static <V> Graph<V> create(Class<?> clazz, int initialSize) {
		return new GraphImplLinearEdge(clazz);
	}
}
