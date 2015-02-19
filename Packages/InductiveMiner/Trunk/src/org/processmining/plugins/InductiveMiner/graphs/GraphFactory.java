package org.processmining.plugins.InductiveMiner.graphs;

import org.deckfour.xes.classification.XEventClass;

public class GraphFactory {
	
	public static <V> Graph<V> create(Class<?> clazz, int initialSize) {
		return new GraphImplLinearEdge<V>(clazz);
	}

	public static Graph<XEventClass> createTimeOptimised(Class<XEventClass> clazz, int initialSize) {
		return new GraphImplLinearEdgeImportOptimised<XEventClass>(clazz);
	}
}
